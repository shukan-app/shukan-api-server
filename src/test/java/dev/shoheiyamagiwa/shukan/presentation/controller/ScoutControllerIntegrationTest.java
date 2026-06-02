package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.Main;
import dev.shoheiyamagiwa.shukan.infra.repository.ScoutRepository;
import dev.shoheiyamagiwa.shukan.middleware.AuthMiddlewareProvider;
import dev.shoheiyamagiwa.shukan.service.ScoutService;
import io.javalin.Javalin;
import org.flywaydb.core.Flyway;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public final class ScoutControllerIntegrationTest {
	private static final String TEST_AUTH_ID = "scout-integration-test-uid";
	private static final Pattern ID_PATTERN = Pattern.compile("\"id\":\"([0-9a-fA-F-]+)\"");
	
	@Container
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");
	
	private static Javalin app;
	
	@BeforeAll
	static void setup() throws SQLException {
		Flyway.configure()
			.dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
			.load()
			.migrate();
		
		try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		     PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (auth_id) VALUES (?)")) {
			stmt.setString(1, TEST_AUTH_ID);
			stmt.executeUpdate();
		}
		
		ScoutRepository scoutRepository =
			new ScoutRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		ScoutService scoutService = new ScoutService(scoutRepository);
		ScoutController scoutController = new ScoutController(scoutService);
		
		app =
			Main.createApplication(
					new TestAuthMiddlewareProvider(), routes -> scoutController.registerRoutes(routes))
				.start(0);
	}
	
	@AfterAll
	static void tearDown() {
		if (app != null) {
			app.stop();
		}
	}
	
	@Test
	public void testListScoutsRejectsMissingAuthorization() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/scouts", null, false);
		
		assertEquals(401, response.statusCode());
		assertEquals("{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Unauthorized\",\"instance\":\"/users/me/scouts\"}", response.body());
	}
	
	@Test
	public void testRegisterScoutReturnsCreatedScout() throws IOException, InterruptedException {
		HttpResponse<String> response =
			send(
				"POST",
				"/users/me/scouts",
				"""
					{"title":"Scout invitation","companyName":"Scout API Company","status":"unread",\
					"platform":"offerBox","detailsUrl":"https://example.com/scout","creationSourceUrl":null}""",
				true);
		
		assertEquals(201, response.statusCode());
		assertTrue(response.body().contains("\"title\":\"Scout invitation\""));
		assertTrue(response.body().contains("\"companyName\":\"Scout API Company\""));
		assertTrue(response.body().contains("\"status\":\"unread\""));
		assertTrue(response.body().contains("\"platform\":\"offerBox\""));
		assertNotNull(extractId(response.body()));
	}
	
	@Test
	public void testRegisterScoutReturnsNotFoundWhenUserDoesNotExist()
		throws IOException, InterruptedException {
		HttpResponse<String> response =
			send(
				"POST",
				"/users/me/scouts",
				"""
					{"title":"Missing user scout","companyName":"Missing User Company","status":"unread",\
					"platform":"mynavi","detailsUrl":null,"creationSourceUrl":null}""",
				"non-existing-user");
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().contains("\"detail\":\"User not found\""));
	}
	
	@Test
	public void testRegisterScoutRejectsInvalidTitle() throws IOException, InterruptedException {
		HttpResponse<String> response =
			send(
				"POST",
				"/users/me/scouts",
				"""
					{"title":"x","companyName":"Scout API Invalid Title Company","status":"unread",\
					"platform":"mynavi","detailsUrl":null,"creationSourceUrl":null}""",
				true);
		
		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("title"));
	}
	
	@Test
	public void testGetScoutsReturnsList() throws IOException, InterruptedException {
		createScout("Scout for list", "unresponded", "mynavi");
		
		HttpResponse<String> response = send("GET", "/users/me/scouts?page=0&pageSize=50", null, true);
		
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"pagination\""));
		assertTrue(response.body().contains("\"data\""));
		assertTrue(response.body().contains("\"title\":\"Scout for list\""));
	}
	
	@Test
	public void testGetScoutsRejectsInvalidFilter() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/scouts?status=invalid", null, true);
		
		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("Invalid status"));
	}
	
	@Test
	public void testGetScoutsReturnsNotFoundWhenUserDoesNotExist()
		throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/scouts", null, "non-existing-user");
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().toLowerCase(Locale.ROOT).contains("not found"));
	}
	
	@Test
	public void testUpdateScoutReturnsUpdatedScout() throws IOException, InterruptedException {
		String scoutId = createScout("Scout before update", "unread", "mynavi");
		
		HttpResponse<String> response =
			send(
				"PUT",
				"/users/me/scouts/" + scoutId,
				"""
					{"title":"Scout after update","companyName":"Updated Scout Company","status":"accepted",\
					"platform":"trackJob","detailsUrl":null,"creationSourceUrl":"https://example.com/source"}""",
				true);
		
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"title\":\"Scout after update\""));
		assertTrue(response.body().contains("\"companyName\":\"Updated Scout Company\""));
		assertTrue(response.body().contains("\"status\":\"accepted\""));
		assertTrue(response.body().contains("\"platform\":\"trackJob\""));
	}
	
	@Test
	public void testUpdateScoutReturnsNotFoundForUnknownId()
		throws IOException, InterruptedException {
		HttpResponse<String> response =
			send(
				"PUT",
				"/users/me/scouts/" + UUID.randomUUID(),
				"""
					{"title":"Scout update","companyName":"Scout Update Company","status":"declined",\
					"platform":"mynavi","detailsUrl":null,"creationSourceUrl":null}""",
				true);
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().toLowerCase(Locale.ROOT).contains("not found"));
	}
	
	@Test
	public void testDeleteScoutReturnsNoContent() throws IOException, InterruptedException {
		String scoutId = createScout("Scout to delete", "unread", "mynavi");
		
		HttpResponse<String> response = send("DELETE", "/users/me/scouts/" + scoutId, null, true);
		
		assertEquals(204, response.statusCode());
	}
	
	@Test
	public void testDeleteScoutReturnsNotFoundForUnknownId()
		throws IOException, InterruptedException {
		HttpResponse<String> response =
			send("DELETE", "/users/me/scouts/" + UUID.randomUUID(), null, true);
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().toLowerCase(Locale.ROOT).contains("not found"));
	}
	
	private String createScout(String title, String status, String platform)
		throws IOException, InterruptedException {
		HttpResponse<String> response =
			send(
				"POST",
				"/users/me/scouts",
				"""
					{"title":"%s","companyName":"Scout API Company","status":"%s",\
					"platform":"%s","detailsUrl":null,"creationSourceUrl":null}"""
					.formatted(title, status, platform),
				true);
		assertEquals(201, response.statusCode());
		return extractId(response.body());
	}
	
	private String extractId(String body) {
		Matcher matcher = ID_PATTERN.matcher(body);
		assertTrue(matcher.find());
		return matcher.group(1);
	}
	
	private HttpResponse<String> send(
		String method, String path, @Nullable String body, boolean authorized)
		throws IOException, InterruptedException {
		return send(method, path, body, authorized ? TEST_AUTH_ID : null);
	}
	
	private HttpResponse<String> send(
		String method, String path, @Nullable String body, @Nullable String bearerToken)
		throws IOException, InterruptedException {
		HttpRequest.Builder builder =
			HttpRequest.newBuilder().uri(URI.create("http://localhost:" + app.port() + path));
		if (bearerToken != null) {
			builder.header("Authorization", "Bearer " + bearerToken);
		}
		if (body != null) {
			builder.header("Content-Type", "application/json");
			builder.method(method, HttpRequest.BodyPublishers.ofString(body));
		} else {
			builder.method(method, HttpRequest.BodyPublishers.noBody());
		}
		
		return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}
	
	private record TestAuthMiddlewareProvider() implements AuthMiddlewareProvider {
		@Override
		public Optional<String> verifyBearerToken(String bearerToken) {
			return Optional.of(bearerToken);
		}
	}
}

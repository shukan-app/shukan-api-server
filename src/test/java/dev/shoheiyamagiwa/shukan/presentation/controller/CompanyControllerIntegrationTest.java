package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.Main;
import dev.shoheiyamagiwa.shukan.infra.repository.CompanyRepository;
import dev.shoheiyamagiwa.shukan.middleware.AuthMiddlewareProvider;
import dev.shoheiyamagiwa.shukan.service.CompanyService;
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
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public final class CompanyControllerIntegrationTest {
	private static final String TEST_AUTH_ID = "integration-test-uid";
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
		
		CompanyRepository repository = new CompanyRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		CompanyService companyService = new CompanyService(repository);
		CompanyController companyController = new CompanyController(companyService);
		
		app = Main.createApplication(new TestAuthMiddlewareProvider(), companyController::registerRoutes).start(0);
	}
	
	@AfterAll
	static void tearDown() {
		if (app != null) {
			app.stop();
		}
	}
	
	@Test
	public void testListCompaniesRejectsMissingAuthorization() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/companies", null, false);
		
		assertEquals(401, response.statusCode());
		assertEquals("{\"message\":\"Unauthorized\"}", response.body());
	}
	
	@Test
	public void testRegisterCompanyReturnsCreatedCompany() throws IOException, InterruptedException {
		HttpResponse<String> response = send(
			"POST",
			"/users/me/companies",
			"""
				{"name":"Acme Corp","appliedRole":"Engineer","status":"bookmarked",\
				"applicationRoute":"mynavi","contactType":"email"}""",
			true);
		
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"name\":\"Acme Corp\""));
		assertTrue(response.body().contains("\"appliedRole\":\"Engineer\""));
		assertTrue(response.body().contains("\"status\":\"bookmarked\""));
		assertNotNull(extractId(response.body()));
	}
	
	@Test
	public void testRegisterCompanyRejectsInvalidName() throws IOException, InterruptedException {
		HttpResponse<String> response = send(
			"POST",
			"/users/me/companies",
			"""
				{"name":"A","appliedRole":"Engineer","status":"bookmarked",\
				"applicationRoute":"mynavi","contactType":"email"}""",
			true);
		
		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("name must be between 2 and 32 characters"));
	}
	
	@Test
	public void testRegisterCompanyRejectsInvalidStatus() throws IOException, InterruptedException {
		HttpResponse<String> response = send(
			"POST",
			"/users/me/companies",
			"""
				{"name":"Acme Corp","appliedRole":"Engineer","status":"unknown",\
				"applicationRoute":"mynavi","contactType":"email"}""",
			true);
		
		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("Invalid status"));
	}
	
	@Test
	public void testGetCompaniesReturnsList() throws IOException, InterruptedException {
		createCompany("List Corp");
		
		HttpResponse<String> response = send("GET", "/users/me/companies", null, true);
		
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"pagination\"") || response.body().contains("page"));
		assertTrue(response.body().contains("List Corp"));
	}

	@Test
	public void testGetCompaniesRejectsInvalidPageQuery() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/companies?page=abc", null, true);

		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("Invalid page: abc"));
	}

	@Test
	public void testGetCompaniesRejectsInvalidPageSizeQuery() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/companies?pageSize=abc", null, true);

		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("Invalid pageSize: abc"));
	}
	
	@Test
	public void testGetCompanyReturnsDetail() throws IOException, InterruptedException {
		String id = createCompany("Detail Corp");
		
		HttpResponse<String> response = send("GET", "/users/me/companies/" + id, null, true);
		
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("Detail Corp"));
		assertTrue(response.body().contains("\"id\":\"" + id + "\""));
	}
	
	@Test
	public void testGetCompanyRejectsInvalidId() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/companies/not-a-uuid", null, true);
		
		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("Invalid company ID"));
	}
	
	@Test
	public void testGetCompanyReturnsNotFoundForUnknownId() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/companies/" + UUID.randomUUID(), null, true);
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().contains("Not Found"));
	}
	
	@Test
	public void testUpdateCompanyReturnsUpdatedCompany() throws IOException, InterruptedException {
		String id = createCompany("Update Corp");
		
		HttpResponse<String> response = send(
			"PUT",
			"/users/me/companies/" + id,
			"""
				{"name":"Updated Corp","appliedRole":"Manager","status":"preentry",\
				"applicationRoute":"agent","contactType":"other"}""",
			true);
		
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"name\":\"Updated Corp\""));
		assertTrue(response.body().contains("\"status\":\"preentry\""));
	}
	
	@Test
	public void testUpdateCompanyReturnsNotFoundForUnknownId() throws IOException, InterruptedException {
		HttpResponse<String> response = send(
			"PUT",
			"/users/me/companies/" + UUID.randomUUID(),
			"""
				{"name":"Updated Corp","appliedRole":"Manager","status":"preentry",\
				"applicationRoute":"agent","contactType":"other"}""",
			true);
		assertEquals(404, response.statusCode());
		assertTrue(response.body().contains("Not Found"));
	}
	
	@Test
	public void testDeleteCompanyReturnsNoContent() throws IOException, InterruptedException {
		String id = createCompany("Delete Corp");
		
		HttpResponse<String> response = send("DELETE", "/users/me/companies/" + id, null, true);
		assertEquals(204, response.statusCode());
		
		HttpResponse<String> getResponse = send("GET", "/users/me/companies/" + id, null, true);
		assertEquals(404, getResponse.statusCode());
	}
	
	@Test
	public void testDeleteCompanyReturnsNotFoundForUnknownId() throws IOException, InterruptedException {
		HttpResponse<String> response = send("DELETE", "/users/me/companies/" + UUID.randomUUID(), null, true);
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().contains("Not Found"));
	}
	
	private static String createCompany(String name) throws IOException, InterruptedException {
		HttpResponse<String> response = send(
			"POST",
			"/users/me/companies",
			"{\"name\":\""
				+ name
				+ "\",\"appliedRole\":\"Engineer\",\"status\":\"bookmarked\","
				+ "\"applicationRoute\":\"mynavi\",\"contactType\":\"email\"}",
			true);
		assertEquals(200, response.statusCode());
		
		String id = extractId(response.body());
		assertNotNull(id);
		
		return id;
	}
	
	private static String extractId(String body) {
		Matcher matcher = ID_PATTERN.matcher(body);
		if (matcher.find()) {
			return matcher.group(1);
		}
		throw new IllegalStateException("No id found in response body: " + body);
	}
	
	private static HttpResponse<String> send(String method, String path, @Nullable String body, boolean authorized) throws IOException, InterruptedException {
		HttpRequest.BodyPublisher publisher = body == null
			? HttpRequest.BodyPublishers.noBody()
			: HttpRequest.BodyPublishers.ofString(body);
		
		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + app.port() + path))
			.header("Content-Type", "application/json")
			.method(method, publisher);
		
		if (authorized) {
			builder.header("Authorization", "Bearer " + TEST_AUTH_ID);
		}
		
		return HttpClient.newHttpClient()
			.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}
	
	private record TestAuthMiddlewareProvider() implements AuthMiddlewareProvider {
		@Override
		public Optional<String> verifyBearerToken(String bearerToken) {
			return Optional.of(bearerToken);
		}
	}
}

package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.Main;
import dev.shoheiyamagiwa.shukan.infra.repository.CompanyRepository;
import dev.shoheiyamagiwa.shukan.infra.repository.EventRepository;
import dev.shoheiyamagiwa.shukan.middleware.AuthMiddlewareProvider;
import dev.shoheiyamagiwa.shukan.service.CompanyService;
import dev.shoheiyamagiwa.shukan.service.EventService;
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
public final class EventControllerIntegrationTest {
	private static final String TEST_AUTH_ID = "event-integration-test-uid";
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

		CompanyRepository companyRepository = new CompanyRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		EventRepository eventRepository = new EventRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		CompanyService companyService = new CompanyService(companyRepository);
		EventService eventService = new EventService(eventRepository);
		CompanyController companyController = new CompanyController(companyService);
		EventController eventController = new EventController(eventService);

		app = Main.createApplication(
				new TestAuthMiddlewareProvider(),
				routes -> {
					companyController.registerRoutes(routes);
					eventController.registerRoutes(routes);
				})
			.start(0);
	}

	@AfterAll
	static void tearDown() {
		if (app != null) {
			app.stop();
		}
	}

	@Test
	public void testListEventsRejectsMissingAuthorization() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/events", null, false);

		assertEquals(401, response.statusCode());
		assertEquals("{\"message\":\"Unauthorized\"}", response.body());
	}

	@Test
	public void testRegisterEventReturnsCreatedEvent() throws IOException, InterruptedException {
		String companyId = createCompany("Event API Company");

		HttpResponse<String> response = send(
			"POST",
			"/users/me/events",
			"""
				{"title":"Company briefing","companyId":"%s","type":"infoSession",\
				"status":"scheduled","formatType":"online","location":"Tokyo",\
				"creationSourceUrl":"https://example.com/event",\
				"beginAt":"2026-07-20T10:00:00+09:00","endAt":"2026-07-20T11:00:00+09:00"}"""
				.formatted(companyId),
			true);

		assertEquals(201, response.statusCode());
		assertTrue(response.body().contains("\"title\":\"Company briefing\""));
		assertTrue(response.body().contains("\"type\":\"infoSession\""));
		assertTrue(response.body().contains("\"status\":\"scheduled\""));
		assertTrue(response.body().contains("\"formatType\":\"online\""));
		assertTrue(response.body().contains("\"company\""));
		assertNotNull(extractId(response.body()));
	}

	@Test
	public void testRegisterEventReturnsNotFoundWhenUserDoesNotExist() throws IOException, InterruptedException {
		String companyId = createCompany("Event API Missing User Company");

		HttpResponse<String> response = send(
			"POST",
			"/users/me/events",
			eventJson(companyId, "Event by missing user"),
			"non-existing-user");

		assertEquals(404, response.statusCode());
		assertTrue(response.body().contains("found") || response.body().contains("Found"));
	}

	@Test
	public void testRegisterEventRejectsInvalidTitle() throws IOException, InterruptedException {
		String companyId = createCompany("Event API Invalid Title Company");

		HttpResponse<String> response = send(
			"POST",
			"/users/me/events",
			eventJson(companyId, "x"),
			true);

		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("title"));
	}

	@Test
	public void testRegisterEventRejectsMissingBeginAt() throws IOException, InterruptedException {
		String companyId = createCompany("Event API Missing Begin Company");

		HttpResponse<String> response = send(
			"POST",
			"/users/me/events",
			"""
				{"title":"Missing begin","companyId":"%s","type":"interview",\
				"status":"scheduled","formatType":"offline","location":null,\
				"creationSourceUrl":null,"endAt":"2026-07-20T11:00:00+09:00"}"""
				.formatted(companyId),
			true);

		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("beginAt"));
	}

	@Test
	public void testGetEventsReturnsList() throws IOException, InterruptedException {
		String companyId = createCompany("Event API List Company");
		createEvent(companyId, "Event for list");

		HttpResponse<String> response = send("GET", "/users/me/events?page=0&pageSize=50", null, true);

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"pagination\""));
		assertTrue(response.body().contains("\"data\""));
	}

	@Test
	public void testGetEventsSupportsFilters() throws IOException, InterruptedException {
		String companyId = createCompany("Event API Filter Company");
		createEvent(companyId, "Filtered event");

		HttpResponse<String> response = send(
			"GET",
			"/users/me/events?type=interview&status=scheduled",
			null,
			true);

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"type\":\"interview\""));
		assertTrue(response.body().contains("\"status\":\"scheduled\""));
	}

	@Test
	public void testGetEventsRejectsInvalidPageQuery() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/events?page=invalid", null, true);

		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("Invalid page"));
	}

	@Test
	public void testGetEventsRejectsInvalidPageSizeQuery() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/events?pageSize=0", null, true);

		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("pageSize"));
	}

	@Test
	public void testGetEventsRejectsInvalidTypeQuery() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/events?type=invalid", null, true);

		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("Invalid type"));
	}

	@Test
	public void testGetEventsRejectsInvalidStatusQuery() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/events?status=invalid", null, true);

		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("Invalid status"));
	}

	@Test
	public void testGetEventsReturnsNotFoundWhenUserDoesNotExist() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/events", null, "non-existing-user");

		assertEquals(404, response.statusCode());
		assertTrue(response.body().contains("found") || response.body().contains("Found"));
	}

	@Test
	public void testUpdateEventReturnsUpdatedEvent() throws IOException, InterruptedException {
		String companyId = createCompany("Event API Update Company");
		String eventId = createEvent(companyId, "Event before update");

		HttpResponse<String> response = send(
			"PUT",
			"/users/me/events/" + eventId,
			"""
				{"title":"Event after update","companyId":"%s","type":"groupWork",\
				"status":"reschedulingRequired","formatType":"hybrid","location":"Osaka",\
				"creationSourceUrl":"https://example.com/updated",\
				"beginAt":"2026-08-20T10:00:00+09:00","endAt":"2026-08-20T11:00:00+09:00"}"""
				.formatted(companyId),
			true);

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"title\":\"Event after update\""));
		assertTrue(response.body().contains("\"type\":\"groupWork\""));
		assertTrue(response.body().contains("\"status\":\"reschedulingRequired\""));
		assertTrue(response.body().contains("\"formatType\":\"hybrid\""));
	}

	@Test
	public void testUpdateEventReturnsNotFoundForUnknownId() throws IOException, InterruptedException {
		String companyId = createCompany("Event API Unknown Update Company");

		HttpResponse<String> response = send(
			"PUT",
			"/users/me/events/" + UUID.randomUUID(),
			eventJson(companyId, "Event update"),
			true);

		assertEquals(404, response.statusCode());
		assertTrue(response.body().contains("found") || response.body().contains("Found"));
	}

	@Test
	public void testDeleteEventReturnsNoContent() throws IOException, InterruptedException {
		String companyId = createCompany("Event API Delete Company");
		String eventId = createEvent(companyId, "Event to delete");

		HttpResponse<String> response = send("DELETE", "/users/me/events/" + eventId, null, true);

		assertEquals(204, response.statusCode());
	}

	@Test
	public void testDeleteEventReturnsNotFoundForUnknownId() throws IOException, InterruptedException {
		HttpResponse<String> response = send("DELETE", "/users/me/events/" + UUID.randomUUID(), null, true);

		assertEquals(404, response.statusCode());
		assertTrue(response.body().contains("found") || response.body().contains("Found"));
	}

	private String createCompany(String name) throws IOException, InterruptedException {
		HttpResponse<String> response = send(
			"POST",
			"/users/me/companies",
			"""
				{"name":"%s","appliedRole":"Engineer","status":"bookmarked",\
				"applicationRoute":"mynavi","contactType":"email","creationSourceUrl":null}"""
				.formatted(name),
			true);
		assertEquals(200, response.statusCode());
		return extractId(response.body());
	}

	private String createEvent(String companyId, String title) throws IOException, InterruptedException {
		HttpResponse<String> response = send(
			"POST",
			"/users/me/events",
			eventJson(companyId, title),
			true);
		assertEquals(201, response.statusCode());
		return extractId(response.body());
	}

	private static String eventJson(String companyId, String title) {
		return """
			{"title":"%s","companyId":"%s","type":"interview",\
			"status":"scheduled","formatType":"offline","location":null,\
			"creationSourceUrl":null,"beginAt":"2026-07-20T10:00:00+09:00",\
			"endAt":"2026-07-20T11:00:00+09:00"}"""
			.formatted(title, companyId);
	}

	private String extractId(String body) {
		Matcher matcher = ID_PATTERN.matcher(body);
		assertTrue(matcher.find());
		return matcher.group(1);
	}

	private HttpResponse<String> send(String method, String path, @Nullable String body, boolean authorized) throws IOException, InterruptedException {
		return send(method, path, body, authorized ? TEST_AUTH_ID : null);
	}

	private HttpResponse<String> send(String method, String path, @Nullable String body, @Nullable String bearerToken) throws IOException, InterruptedException {
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

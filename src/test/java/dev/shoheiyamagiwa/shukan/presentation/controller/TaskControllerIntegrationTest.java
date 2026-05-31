package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.Main;
import dev.shoheiyamagiwa.shukan.infra.repository.CompanyRepository;
import dev.shoheiyamagiwa.shukan.infra.repository.TaskRepository;
import dev.shoheiyamagiwa.shukan.middleware.AuthMiddlewareProvider;
import dev.shoheiyamagiwa.shukan.service.CompanyService;
import dev.shoheiyamagiwa.shukan.service.TaskService;
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
public final class TaskControllerIntegrationTest {
	private static final String TEST_AUTH_ID = "task-integration-test-uid";
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
		TaskRepository taskRepository = new TaskRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		CompanyService companyService = new CompanyService(companyRepository);
		TaskService taskService = new TaskService(taskRepository);
		CompanyController companyController = new CompanyController(companyService);
		TaskController taskController = new TaskController(taskService);
		
		app = Main.createApplication(
				new TestAuthMiddlewareProvider(),
				routes -> {
					companyController.registerRoutes(routes);
					taskController.registerRoutes(routes);
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
	public void testListTasksRejectsMissingAuthorization() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/tasks", null, false);
		
		assertEquals(401, response.statusCode());
		assertEquals("{\"message\":\"Unauthorized\"}", response.body());
	}
	
	@Test
	public void testRegisterTaskReturnsCreatedTask() throws IOException, InterruptedException {
		String companyId = createCompany("Task API Company");
		
		HttpResponse<String> response = send(
			"POST",
			"/users/me/tasks",
			"""
				{"title":"Prepare interview","companyId":"%s","type":"preparation",\
				"status":"incomplete","creationSourceUrl":"https://example.com/task","deadline":"2026-07-20T10:00:00+09:00"}"""
				.formatted(companyId),
			true);
		
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"title\":\"Prepare interview\""));
		assertTrue(response.body().contains("\"type\":\"preparation\""));
		assertTrue(response.body().contains("\"status\":\"incomplete\""));
		assertTrue(response.body().contains("\"createdBy\":\"user\""));
		assertTrue(response.body().contains("\"company\""));
		assertNotNull(extractId(response.body()));
	}
	
	@Test
	public void testRegisterTaskReturnsNotFoundWhenUserDoesNotExist() throws IOException, InterruptedException {
		String companyId = createCompany("Task API Missing User Company");
		
		HttpResponse<String> response = send(
			"POST",
			"/users/me/tasks",
			"""
				{"title":"Task by missing user","companyId":"%s","type":"assessment",\
				"status":"incomplete","creationSourceUrl":null,"deadline":null}"""
				.formatted(companyId),
			"non-existing-user");
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().toLowerCase().contains("not found"));
	}
	
	@Test
	public void testRegisterTaskRejectsInvalidTitle() throws IOException, InterruptedException {
		String companyId = createCompany("Task API Invalid Title Company");
		
		HttpResponse<String> response = send(
			"POST",
			"/users/me/tasks",
			"""
				{"title":"x","companyId":"%s","type":"assessment",\
				"status":"incomplete","creationSourceUrl":null,"deadline":null}"""
				.formatted(companyId),
			true);
		
		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("title"));
	}
	
	@Test
	public void testGetTasksReturnsList() throws IOException, InterruptedException {
		String companyId = createCompany("Task API List Company");
		createTask(companyId, "Task for list");
		
		HttpResponse<String> response = send("GET", "/users/me/tasks?page=0&pageSize=20", null, true);
		
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"pagination\""));
		assertTrue(response.body().contains("\"data\""));
	}
	
	@Test
	public void testGetTasksRejectsInvalidPageQuery() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/tasks?page=invalid", null, true);
		
		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("Invalid page"));
	}
	
	@Test
	public void testGetTasksRejectsInvalidPageSizeQuery() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/tasks?pageSize=0", null, true);
		
		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("pageSize"));
	}
	
	@Test
	public void testGetTasksReturnsNotFoundWhenUserDoesNotExist() throws IOException, InterruptedException {
		HttpResponse<String> response = send("GET", "/users/me/tasks", null, "non-existing-user");
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().toLowerCase().contains("not found"));
	}
	
	@Test
	public void testUpdateTaskReturnsUpdatedTask() throws IOException, InterruptedException {
		String companyId = createCompany("Task API Update Company");
		String taskId = createTask(companyId, "Task before update");
		
		HttpResponse<String> response = send(
			"PUT",
			"/users/me/tasks/" + taskId,
			"""
				{"title":"Task after update","companyId":"%s","type":"replyRequired",\
				"status":"complete","creationSourceUrl":"https://example.com/updated","deadline":null}"""
				.formatted(companyId),
			true);
		
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"title\":\"Task after update\""));
		assertTrue(response.body().contains("\"type\":\"replyRequired\""));
		assertTrue(response.body().contains("\"status\":\"complete\""));
	}
	
	@Test
	public void testUpdateTaskReturnsNotFoundForUnknownId() throws IOException, InterruptedException {
		String companyId = createCompany("Task API Unknown Update Company");
		
		HttpResponse<String> response = send(
			"PUT",
			"/users/me/tasks/" + UUID.randomUUID(),
			"""
				{"title":"Task update","companyId":"%s","type":"assessment",\
				"status":"incomplete","creationSourceUrl":null,"deadline":null}"""
				.formatted(companyId),
			true);
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().toLowerCase().contains("not found"));
	}
	
	@Test
	public void testDeleteTaskReturnsNoContent() throws IOException, InterruptedException {
		String companyId = createCompany("Task API Delete Company");
		String taskId = createTask(companyId, "Task to delete");
		
		HttpResponse<String> response = send("DELETE", "/users/me/tasks/" + taskId, null, true);
		
		assertEquals(204, response.statusCode());
	}
	
	@Test
	public void testDeleteTaskReturnsNotFoundForUnknownId() throws IOException, InterruptedException {
		HttpResponse<String> response = send("DELETE", "/users/me/tasks/" + UUID.randomUUID(), null, true);
		
		assertEquals(404, response.statusCode());
		assertTrue(response.body().toLowerCase().contains("not found"));
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
	
	private String createTask(String companyId, String title) throws IOException, InterruptedException {
		HttpResponse<String> response = send(
			"POST",
			"/users/me/tasks",
			"""
				{"title":"%s","companyId":"%s","type":"assessment",\
				"status":"incomplete","creationSourceUrl":null,"deadline":null}"""
				.formatted(title, companyId),
			true);
		assertEquals(200, response.statusCode());
		return extractId(response.body());
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

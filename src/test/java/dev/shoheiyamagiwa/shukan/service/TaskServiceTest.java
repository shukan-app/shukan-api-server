package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.entity.Task;
import dev.shoheiyamagiwa.shukan.domain.vo.*;
import dev.shoheiyamagiwa.shukan.infra.repository.CompanyRepository;
import dev.shoheiyamagiwa.shukan.infra.repository.TaskRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public final class TaskServiceTest {
	private static final String TEST_AUTH_ID = "task-service-test-firebase-uid";
	
	@Container
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");
	private static CompanyService companyService;
	private static TaskService taskService;
	
	@BeforeAll
	static void setup() throws SQLException {
		Flyway.configure()
			.dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
			.load()
			.migrate();
		
		CompanyRepository companyRepository = new CompanyRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		TaskRepository taskRepository = new TaskRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		companyService = new CompanyService(companyRepository);
		taskService = new TaskService(taskRepository);
		
		try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		     PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (auth_id) VALUES (?)")) {
			stmt.setString(1, TEST_AUTH_ID);
			stmt.executeUpdate();
		}
	}
	
	@Test
	public void testRegisterTask() {
		UUID companyId = createCompany("Task Register Corp");
		
		Optional<Task> task = taskService.registerTask(
			TEST_AUTH_ID,
			"Submit portfolio",
			companyId,
			TaskType.DOCUMENT_SUBMISSION,
			TaskStatus.INCOMPLETE,
			null,
			OffsetDateTime.parse("2026-07-01T09:00:00+09:00"));
		
		assertTrue(task.isPresent());
		Task registered = task.get();
		assertNotNull(registered.id());
		assertEquals("Submit portfolio", registered.title());
		assertEquals(TaskType.DOCUMENT_SUBMISSION, registered.type());
		assertEquals(TaskStatus.INCOMPLETE, registered.status());
		assertEquals(TaskCreationSource.USER, registered.createdBy());
		assertEquals(companyId, registered.company().id());
		assertNull(registered.creationSourceUrl());
		assertNotNull(registered.deadline());
	}
	
	@Test
	public void testFindTask() {
		UUID companyId = createCompany("Task Find Corp");
		
		Optional<Task> created = taskService.registerTask(
			TEST_AUTH_ID,
			"Find task",
			companyId,
			TaskType.ASSESSMENT,
			TaskStatus.INCOMPLETE,
			null,
			null);
		assertTrue(created.isPresent());
		
		Optional<Task> found = taskService.findTask(TEST_AUTH_ID, created.get().id());
		
		assertTrue(found.isPresent());
		assertEquals(created.get().id(), found.get().id());
		assertEquals("Find task", found.get().title());
	}
	
	@Test
	public void testFindTaskNotFound() {
		Optional<Task> found = taskService.findTask(TEST_AUTH_ID, UUID.randomUUID());
		
		assertFalse(found.isPresent());
	}
	
	@Test
	public void testGetTasks() {
		UUID companyId = createCompany("Task List Corp");
		taskService.registerTask(
			TEST_AUTH_ID,
			"List task",
			companyId,
			TaskType.SCHEDULING,
			TaskStatus.INCOMPLETE,
			null,
			null);
		
		TasksPage page = taskService.getTasks(TEST_AUTH_ID, 0, 20, null);
		
		assertNotNull(page);
		assertFalse(page.tasks().isEmpty());
		assertEquals(0, page.page());
		assertEquals(20, page.pageSize());
	}
	
	@Test
	public void testGetTasksWithTypeFilter() {
		UUID companyId = createCompany("Task Filter Corp");
		taskService.registerTask(
			TEST_AUTH_ID,
			"Assessment task",
			companyId,
			TaskType.ASSESSMENT,
			TaskStatus.INCOMPLETE,
			null,
			null);
		taskService.registerTask(
			TEST_AUTH_ID,
			"Preparation task",
			companyId,
			TaskType.PREPARATION,
			TaskStatus.INCOMPLETE,
			null,
			null);
		
		TasksPage page = taskService.getTasks(TEST_AUTH_ID, 0, 50, TaskType.ASSESSMENT);
		
		List<Task> filtered = page.tasks();
		assertTrue(filtered.stream().allMatch(task -> task.type() == TaskType.ASSESSMENT));
	}
	
	@Test
	public void testGetTasksSortByDeadlineAscending() {
		UUID companyId = createCompany("Task Sort Corp");
		
		Optional<Task> later = taskService.registerTask(
			TEST_AUTH_ID,
			"Later deadline",
			companyId,
			TaskType.OTHER,
			TaskStatus.INCOMPLETE,
			null,
			OffsetDateTime.parse("2026-09-10T10:00:00+09:00"));
		Optional<Task> earlier = taskService.registerTask(
			TEST_AUTH_ID,
			"Earlier deadline",
			companyId,
			TaskType.OTHER,
			TaskStatus.INCOMPLETE,
			null,
			OffsetDateTime.parse("2026-08-10T10:00:00+09:00"));
		
		assertTrue(later.isPresent());
		assertTrue(earlier.isPresent());
		
		TasksPage page = taskService.getTasks(TEST_AUTH_ID, 0, 100, null);
		List<UUID> ids = page.tasks().stream().map(Task::id).toList();
		
		int earlierIndex = ids.indexOf(earlier.get().id());
		int laterIndex = ids.indexOf(later.get().id());
		
		assertTrue(earlierIndex >= 0);
		assertTrue(laterIndex >= 0);
		assertTrue(earlierIndex < laterIndex);
	}
	
	@Test
	public void testUpdateTask() {
		UUID companyId = createCompany("Task Update Corp");
		UUID newCompanyId = createCompany("Task Update Target Corp");
		
		Optional<Task> created = taskService.registerTask(
			TEST_AUTH_ID,
			"Update target",
			companyId,
			TaskType.REPLY_REQUIRED,
			TaskStatus.INCOMPLETE,
			null,
			null);
		assertTrue(created.isPresent());
		
		Optional<Task> updated = taskService.updateTask(
			TEST_AUTH_ID,
			created.get().id(),
			"Updated task",
			newCompanyId,
			TaskType.PREPARATION,
			TaskStatus.COMPLETE,
			"https://example.com/task",
			OffsetDateTime.parse("2026-10-10T12:00:00+09:00"));
		
		assertTrue(updated.isPresent());
		assertEquals("Updated task", updated.get().title());
		assertEquals(newCompanyId, updated.get().company().id());
		assertEquals(TaskType.PREPARATION, updated.get().type());
		assertEquals(TaskStatus.COMPLETE, updated.get().status());
		assertEquals("https://example.com/task", updated.get().creationSourceUrl());
		assertNotNull(updated.get().deadline());
	}
	
	@Test
	public void testDeleteTask() {
		UUID companyId = createCompany("Task Delete Corp");
		
		Optional<Task> created = taskService.registerTask(
			TEST_AUTH_ID,
			"Delete target",
			companyId,
			TaskType.OTHER,
			TaskStatus.INCOMPLETE,
			null,
			null);
		assertTrue(created.isPresent());
		
		boolean deleted = taskService.deleteTask(TEST_AUTH_ID, created.get().id());
		
		assertTrue(deleted);
		assertFalse(taskService.findTask(TEST_AUTH_ID, created.get().id()).isPresent());
	}
	
	@Test
	public void testDeleteTaskNotFound() {
		boolean deleted = taskService.deleteTask(TEST_AUTH_ID, UUID.randomUUID());
		
		assertFalse(deleted);
	}
	
	private static UUID createCompany(String name) {
		Optional<Company> company = companyService.registerCompany(
			TEST_AUTH_ID,
			name,
			"Engineer",
			CompanyStatus.BOOKMARKED,
			RecruitingPlatform.MYNAVI,
			ContactType.EMAIL,
			null);
		if (company.isEmpty()) {
			throw new IllegalStateException("Failed to create company for test");
		}
		return company.get().id();
	}
}

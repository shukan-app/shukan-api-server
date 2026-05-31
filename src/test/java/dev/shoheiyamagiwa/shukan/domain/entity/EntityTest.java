package dev.shoheiyamagiwa.shukan.domain.entity;

import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskCreationSource;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class EntityTest {

	private static Company buildCompany() {
		return new Company(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Example Corp",
				"Engineer",
				CompanyStatus.BOOKMARKED,
				RecruitingPlatform.MYNAVI,
				ContactType.EMAIL,
				"https://example.com",
				OffsetDateTime.parse("2024-01-01T00:00:00+09:00"),
				OffsetDateTime.parse("2024-01-02T00:00:00+09:00"),
				null);
	}

	@Test
	public void testCompanyFieldAccess() {
		UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
		OffsetDateTime createdAt = OffsetDateTime.parse("2024-01-01T00:00:00+09:00");
		OffsetDateTime updatedAt = OffsetDateTime.parse("2024-01-02T00:00:00+09:00");

		Company company = new Company(
				id,
				"Example Corp",
				"Engineer",
				CompanyStatus.BOOKMARKED,
				RecruitingPlatform.MYNAVI,
				ContactType.EMAIL,
				"https://example.com",
				createdAt,
				updatedAt,
				null);

		assertEquals(id, company.id());
		assertEquals("Example Corp", company.name());
		assertEquals("Engineer", company.appliedRole());
		assertEquals(CompanyStatus.BOOKMARKED, company.status());
		assertEquals(RecruitingPlatform.MYNAVI, company.applicationRoute());
		assertEquals(ContactType.EMAIL, company.contactType());
		assertEquals("https://example.com", company.creationSourceUrl());
		assertEquals(createdAt, company.createdAt());
		assertEquals(updatedAt, company.updatedAt());
		assertNull(company.deletedAt());
	}

	@Test
	public void testCompanyNullableFieldsAcceptNull() {
		Company company = new Company(
				UUID.randomUUID(),
				"Corp",
				"Role",
				CompanyStatus.REJECTED,
				RecruitingPlatform.OTHER,
				ContactType.OTHER,
				null,
				OffsetDateTime.now(),
				OffsetDateTime.now(),
				null);

		assertNull(company.creationSourceUrl());
		assertNull(company.deletedAt());
	}

	@Test
	public void testCompanyEquality() {
		UUID id = UUID.fromString("00000000-0000-0000-0000-000000000002");
		OffsetDateTime now = OffsetDateTime.parse("2024-06-01T12:00:00+00:00");

		Company a = new Company(id, "Corp A", "Dev", CompanyStatus.PREENTRY,
				RecruitingPlatform.RIKUNABI, ContactType.PHONE, null, now, now, null);
		Company b = new Company(id, "Corp A", "Dev", CompanyStatus.PREENTRY,
				RecruitingPlatform.RIKUNABI, ContactType.PHONE, null, now, now, null);
		Company c = new Company(UUID.randomUUID(), "Corp B", "Dev", CompanyStatus.PREENTRY,
				RecruitingPlatform.RIKUNABI, ContactType.PHONE, null, now, now, null);

		assertEquals(a, b);
		assertNotEquals(a, c);
	}

	@Test
	public void testTaskFieldAccess() {
		UUID taskId = UUID.fromString("00000000-0000-0000-0000-000000000010");
		Company company = buildCompany();
		OffsetDateTime createdAt = OffsetDateTime.parse("2024-01-01T00:00:00+09:00");
		OffsetDateTime updatedAt = OffsetDateTime.parse("2024-01-02T00:00:00+09:00");
		OffsetDateTime deadline = OffsetDateTime.parse("2024-02-01T00:00:00+09:00");

		Task task = new Task(
				taskId,
				"Submit ES",
				company,
				TaskType.DOCUMENT_SUBMISSION,
				TaskStatus.INCOMPLETE,
				TaskCreationSource.USER,
				"https://source.example.com",
				deadline,
				createdAt,
				updatedAt,
				null);

		assertEquals(taskId, task.id());
		assertEquals("Submit ES", task.title());
		assertEquals(company, task.company());
		assertEquals(TaskType.DOCUMENT_SUBMISSION, task.type());
		assertEquals(TaskStatus.INCOMPLETE, task.status());
		assertEquals(TaskCreationSource.USER, task.createdBy());
		assertEquals("https://source.example.com", task.creationSourceUrl());
		assertEquals(deadline, task.deadline());
		assertEquals(createdAt, task.createdAt());
		assertEquals(updatedAt, task.updatedAt());
		assertNull(task.deletedAt());
	}

	@Test
	public void testTaskNullableFieldsAcceptNull() {
		Company company = buildCompany();
		OffsetDateTime now = OffsetDateTime.now();

		Task task = new Task(
				UUID.randomUUID(),
				"Some Task",
				company,
				TaskType.OTHER,
				TaskStatus.COMPLETE,
				TaskCreationSource.AI,
				null,
				null,
				now,
				now,
				null);

		assertNull(task.creationSourceUrl());
		assertNull(task.deadline());
		assertNull(task.deletedAt());
	}

	@Test
	public void testTaskEquality() {
		UUID taskId = UUID.fromString("00000000-0000-0000-0000-000000000020");
		Company company = buildCompany();
		OffsetDateTime now = OffsetDateTime.parse("2024-06-01T12:00:00+00:00");

		Task a = new Task(taskId, "Task A", company, TaskType.ASSESSMENT,
				TaskStatus.INCOMPLETE, TaskCreationSource.USER, null, null, now, now, null);
		Task b = new Task(taskId, "Task A", company, TaskType.ASSESSMENT,
				TaskStatus.INCOMPLETE, TaskCreationSource.USER, null, null, now, now, null);
		Task c = new Task(UUID.randomUUID(), "Task C", company, TaskType.ASSESSMENT,
				TaskStatus.INCOMPLETE, TaskCreationSource.USER, null, null, now, now, null);

		assertEquals(a, b);
		assertNotEquals(a, c);
	}
}
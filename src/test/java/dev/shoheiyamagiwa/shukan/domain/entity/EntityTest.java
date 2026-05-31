package dev.shoheiyamagiwa.shukan.domain.entity;

import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskCreationSource;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class EntityTest {

	private static Company buildCompany() {
		return new Company(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"Acme Corp",
				"Software Engineer",
				CompanyStatus.BOOKMARKED,
				RecruitingPlatform.MYNAVI,
				ContactType.EMAIL,
				"https://example.com",
				OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
				OffsetDateTime.of(2024, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC),
				null);
	}

	@Test
	public void testCompanyRecordAccessors() {
		Company company = buildCompany();

		assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), company.id());
		assertEquals("Acme Corp", company.name());
		assertEquals("Software Engineer", company.appliedRole());
		assertEquals(CompanyStatus.BOOKMARKED, company.status());
		assertEquals(RecruitingPlatform.MYNAVI, company.applicationRoute());
		assertEquals(ContactType.EMAIL, company.contactType());
		assertEquals("https://example.com", company.creationSourceUrl());
		assertEquals(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), company.createdAt());
		assertEquals(OffsetDateTime.of(2024, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC), company.updatedAt());
		assertNull(company.deletedAt());
	}

	@Test
	public void testCompanyRecordEquality() {
		Company company1 = buildCompany();
		Company company2 = buildCompany();

		assertEquals(company1, company2);
		assertEquals(company1.hashCode(), company2.hashCode());
	}

	@Test
	public void testCompanyRecordInequalityOnDifferentId() {
		Company company1 = buildCompany();
		Company company2 =
				new Company(
						UUID.fromString("22222222-2222-2222-2222-222222222222"),
						company1.name(),
						company1.appliedRole(),
						company1.status(),
						company1.applicationRoute(),
						company1.contactType(),
						company1.creationSourceUrl(),
						company1.createdAt(),
						company1.updatedAt(),
						company1.deletedAt());

		assertNotEquals(company1, company2);
	}

	@Test
	public void testCompanyRecordWithNullableCreationSourceUrl() {
		Company company =
				new Company(
						UUID.randomUUID(),
						"Corp",
						"Engineer",
						CompanyStatus.REJECTED,
						RecruitingPlatform.OTHER,
						ContactType.OTHER,
						null,
						OffsetDateTime.now(ZoneOffset.UTC),
						OffsetDateTime.now(ZoneOffset.UTC),
						null);

		assertNull(company.creationSourceUrl());
		assertNull(company.deletedAt());
	}

	@Test
	public void testCompanyRecordWithDeletedAt() {
		OffsetDateTime deletedAt = OffsetDateTime.of(2024, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC);
		Company company =
				new Company(
						UUID.randomUUID(),
						"Corp",
						"Engineer",
						CompanyStatus.REJECTED,
						RecruitingPlatform.OTHER,
						ContactType.OTHER,
						null,
						OffsetDateTime.now(ZoneOffset.UTC),
						OffsetDateTime.now(ZoneOffset.UTC),
						deletedAt);

		assertEquals(deletedAt, company.deletedAt());
	}

	@Test
	public void testTaskRecordAccessors() {
		Company company = buildCompany();
		OffsetDateTime now = OffsetDateTime.of(2024, 3, 15, 10, 0, 0, 0, ZoneOffset.UTC);
		OffsetDateTime deadline = OffsetDateTime.of(2024, 3, 20, 23, 59, 0, 0, ZoneOffset.UTC);

		Task task =
				new Task(
						UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
						"Submit resume",
						company,
						TaskType.DOCUMENT_SUBMISSION,
						TaskStatus.INCOMPLETE,
						TaskCreationSource.USER,
						"https://example.com/mail",
						deadline,
						now,
						now,
						null);

		assertEquals(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), task.id());
		assertEquals("Submit resume", task.title());
		assertEquals(company, task.company());
		assertEquals(TaskType.DOCUMENT_SUBMISSION, task.type());
		assertEquals(TaskStatus.INCOMPLETE, task.status());
		assertEquals(TaskCreationSource.USER, task.createdBy());
		assertEquals("https://example.com/mail", task.creationSourceUrl());
		assertEquals(deadline, task.deadline());
		assertEquals(now, task.createdAt());
		assertEquals(now, task.updatedAt());
		assertNull(task.deletedAt());
	}

	@Test
	public void testTaskRecordEquality() {
		Company company = buildCompany();
		OffsetDateTime now = OffsetDateTime.of(2024, 3, 15, 10, 0, 0, 0, ZoneOffset.UTC);
		UUID id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

		Task task1 =
				new Task(
						id,
						"Title",
						company,
						TaskType.ASSESSMENT,
						TaskStatus.COMPLETE,
						TaskCreationSource.AI,
						null,
						null,
						now,
						now,
						null);
		Task task2 =
				new Task(
						id,
						"Title",
						company,
						TaskType.ASSESSMENT,
						TaskStatus.COMPLETE,
						TaskCreationSource.AI,
						null,
						null,
						now,
						now,
						null);

		assertEquals(task1, task2);
		assertEquals(task1.hashCode(), task2.hashCode());
	}

	@Test
	public void testTaskRecordWithNullableFields() {
		Company company = buildCompany();
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

		Task task =
				new Task(
						UUID.randomUUID(),
						"Prepare notes",
						company,
						TaskType.PREPARATION,
						TaskStatus.INCOMPLETE,
						TaskCreationSource.USER,
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
	public void testTaskRecordInequalityOnDifferentTitle() {
		Company company = buildCompany();
		OffsetDateTime now = OffsetDateTime.of(2024, 3, 15, 10, 0, 0, 0, ZoneOffset.UTC);
		UUID id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

		Task task1 =
				new Task(
						id,
						"Title A",
						company,
						TaskType.OTHER,
						TaskStatus.INCOMPLETE,
						TaskCreationSource.USER,
						null,
						null,
						now,
						now,
						null);
		Task task2 =
				new Task(
						id,
						"Title B",
						company,
						TaskType.OTHER,
						TaskStatus.INCOMPLETE,
						TaskCreationSource.USER,
						null,
						null,
						now,
						now,
						null);

		assertNotEquals(task1, task2);
	}
}
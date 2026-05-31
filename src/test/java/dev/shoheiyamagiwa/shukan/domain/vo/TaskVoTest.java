package dev.shoheiyamagiwa.shukan.domain.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class TaskVoTest {

	@Test
	public void testTaskCreationSourceFromValue() {
		assertEquals(TaskCreationSource.USER, TaskCreationSource.fromValue("user"));
		assertEquals(TaskCreationSource.AI, TaskCreationSource.fromValue("ai"));
	}

	@Test
	public void testTaskCreationSourceFromValueThrowsOnUnknown() {
		assertThrows(IllegalArgumentException.class, () -> TaskCreationSource.fromValue("unknown"));
	}

	@Test
	public void testTaskCreationSourceGetValue() {
		assertEquals("user", TaskCreationSource.USER.getValue());
		assertEquals("ai", TaskCreationSource.AI.getValue());
	}

	@Test
	public void testTaskCreationSourceFromDatabaseValueAndToDatabaseValue() {
		assertEquals(TaskCreationSource.USER, TaskCreationSource.fromDatabaseValue("user"));
		assertEquals(TaskCreationSource.AI, TaskCreationSource.fromDatabaseValue("ai"));
		assertEquals("user", TaskCreationSource.USER.toDatabaseValue());
		assertEquals("ai", TaskCreationSource.AI.toDatabaseValue());
	}

	@Test
	public void testTaskCreationSourceFromDatabaseValueThrowsOnUnknown() {
		assertThrows(IllegalArgumentException.class, () -> TaskCreationSource.fromDatabaseValue("unknown"));
	}

	@Test
	public void testTaskStatusFromValue() {
		assertEquals(TaskStatus.COMPLETE, TaskStatus.fromValue("complete"));
		assertEquals(TaskStatus.INCOMPLETE, TaskStatus.fromValue("incomplete"));
	}

	@Test
	public void testTaskStatusFromValueThrowsOnUnknown() {
		assertThrows(IllegalArgumentException.class, () -> TaskStatus.fromValue("unknown"));
	}

	@Test
	public void testTaskStatusGetValue() {
		assertEquals("complete", TaskStatus.COMPLETE.getValue());
		assertEquals("incomplete", TaskStatus.INCOMPLETE.getValue());
	}

	@Test
	public void testTaskStatusFromDatabaseValueAndToDatabaseValue() {
		assertEquals(TaskStatus.COMPLETE, TaskStatus.fromDatabaseValue("complete"));
		assertEquals(TaskStatus.INCOMPLETE, TaskStatus.fromDatabaseValue("incomplete"));
		assertEquals("complete", TaskStatus.COMPLETE.toDatabaseValue());
		assertEquals("incomplete", TaskStatus.INCOMPLETE.toDatabaseValue());
	}

	@Test
	public void testTaskStatusFromDatabaseValueThrowsOnUnknown() {
		assertThrows(IllegalArgumentException.class, () -> TaskStatus.fromDatabaseValue("unknown"));
	}

	@Test
	public void testTaskTypeFromValue() {
		assertEquals(TaskType.DOCUMENT_SUBMISSION, TaskType.fromValue("documentSubmission"));
		assertEquals(TaskType.ASSESSMENT, TaskType.fromValue("assessment"));
		assertEquals(TaskType.SCHEDULING, TaskType.fromValue("scheduling"));
		assertEquals(TaskType.REPLY_REQUIRED, TaskType.fromValue("replyRequired"));
		assertEquals(TaskType.PREPARATION, TaskType.fromValue("preparation"));
		assertEquals(TaskType.OTHER, TaskType.fromValue("other"));
	}

	@Test
	public void testTaskTypeFromValueThrowsOnUnknown() {
		assertThrows(IllegalArgumentException.class, () -> TaskType.fromValue("unknown"));
	}

	@Test
	public void testTaskTypeGetValue() {
		assertEquals("documentSubmission", TaskType.DOCUMENT_SUBMISSION.getValue());
		assertEquals("replyRequired", TaskType.REPLY_REQUIRED.getValue());
		assertEquals("other", TaskType.OTHER.getValue());
	}

	@Test
	public void testTaskTypeFromDatabaseValueAndToDatabaseValue() {
		assertEquals(TaskType.DOCUMENT_SUBMISSION, TaskType.fromDatabaseValue("document_submission"));
		assertEquals(TaskType.REPLY_REQUIRED, TaskType.fromDatabaseValue("reply_required"));
		assertEquals("document_submission", TaskType.DOCUMENT_SUBMISSION.toDatabaseValue());
		assertEquals("reply_required", TaskType.REPLY_REQUIRED.toDatabaseValue());
	}

	@Test
	public void testTaskTypeFromDatabaseValueThrowsOnUnknown() {
		assertThrows(IllegalArgumentException.class, () -> TaskType.fromDatabaseValue("unknown"));
	}
}

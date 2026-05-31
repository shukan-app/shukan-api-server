package dev.shoheiyamagiwa.shukan.domain.entity;

import dev.shoheiyamagiwa.shukan.domain.vo.TaskCreationSource;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskType;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Task(
		UUID id,
		String title,
		Company company,
		TaskType type,
		TaskStatus status,
		TaskCreationSource createdBy,
		@Nullable String creationSourceUrl,
		@Nullable OffsetDateTime deadline,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		@Nullable OffsetDateTime deletedAt) {
}

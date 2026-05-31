package dev.shoheiyamagiwa.shukan.infra.dto;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskDto(
		UUID id,
		String title,
		UUID companyId,
		String companyName,
		String companyAppliedRole,
		String companyStatus,
		String companyApplicationRoute,
		String companyContactType,
		@Nullable String companyCreationSourceUrl,
		OffsetDateTime companyCreatedAt,
		OffsetDateTime companyUpdatedAt,
		@Nullable OffsetDateTime companyDeletedAt,
		String type,
		String status,
		String createdBy,
		@Nullable String creationSourceUrl,
		@Nullable OffsetDateTime deadline,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		@Nullable OffsetDateTime deletedAt) {
}

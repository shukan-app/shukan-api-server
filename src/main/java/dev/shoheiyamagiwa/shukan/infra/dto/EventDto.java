package dev.shoheiyamagiwa.shukan.infra.dto;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventDto(
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
	String formatType,
	@Nullable String location,
	@Nullable String creationSourceUrl,
	OffsetDateTime beginAt,
	OffsetDateTime endAt,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	@Nullable OffsetDateTime deletedAt) {
}

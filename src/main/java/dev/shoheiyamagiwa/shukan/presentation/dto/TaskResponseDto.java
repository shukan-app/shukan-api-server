package dev.shoheiyamagiwa.shukan.presentation.dto;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponseDto(
	UUID id,
	String title,
	CompanyResponseDto company,
	String type,
	String status,
	String createdBy,
	@Nullable String creationSourceUrl,
	@Nullable OffsetDateTime deadline,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	@Nullable OffsetDateTime deletedAt) {
}

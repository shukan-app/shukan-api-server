package dev.shoheiyamagiwa.shukan.presentation.dto;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventResponseDto(
		UUID id,
		String title,
		CompanyResponseDto company,
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

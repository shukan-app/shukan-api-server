package dev.shoheiyamagiwa.shukan.presentation.dto;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventUpdateRequestDto(
		@Nullable String title,
		@Nullable UUID companyId,
		@Nullable String type,
		@Nullable String status,
		@Nullable String formatType,
		@Nullable String location,
		@Nullable String creationSourceUrl,
		@Nullable OffsetDateTime beginAt,
		@Nullable OffsetDateTime endAt) {
}

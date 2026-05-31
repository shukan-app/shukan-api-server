package dev.shoheiyamagiwa.shukan.presentation.dto;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskUpdateRequestDto(
		@Nullable String title,
		@Nullable UUID companyId,
		@Nullable String type,
		@Nullable String status,
		@Nullable String creationSourceUrl,
		@Nullable OffsetDateTime deadline) {
}

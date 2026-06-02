package dev.shoheiyamagiwa.shukan.presentation.dto;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CompanyResponseDto(
	UUID id,
	String name,
	String appliedRole,
	String status,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	@Nullable OffsetDateTime deletedAt) {
}

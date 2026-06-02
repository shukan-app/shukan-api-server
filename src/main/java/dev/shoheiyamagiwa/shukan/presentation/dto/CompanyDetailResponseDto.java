package dev.shoheiyamagiwa.shukan.presentation.dto;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CompanyDetailResponseDto(
	UUID id,
	String name,
	String appliedRole,
	String status,
	String applicationRoute,
	String contactType,
	@Nullable String creationSourceUrl,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	@Nullable OffsetDateTime deletedAt) {
}

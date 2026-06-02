package dev.shoheiyamagiwa.shukan.infra.dto;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScoutDto(
	UUID id,
	String title,
	String companyName,
	String status,
	String platform,
	@Nullable String detailsUrl,
	@Nullable String creationSourceUrl,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	@Nullable OffsetDateTime deletedAt) {
}

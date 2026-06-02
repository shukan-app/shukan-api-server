package dev.shoheiyamagiwa.shukan.presentation.dto;

import org.jspecify.annotations.Nullable;

public record ScoutUpdateRequestDto(
	@Nullable String title,
	@Nullable String companyName,
	@Nullable String status,
	@Nullable String platform,
	@Nullable String detailsUrl,
	@Nullable String creationSourceUrl) {
}

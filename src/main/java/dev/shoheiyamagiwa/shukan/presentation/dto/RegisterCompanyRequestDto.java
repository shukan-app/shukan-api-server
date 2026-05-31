package dev.shoheiyamagiwa.shukan.presentation.dto;

import org.jspecify.annotations.Nullable;

public record RegisterCompanyRequestDto(
		@Nullable String name,
		@Nullable String appliedRole,
		@Nullable String status,
		@Nullable String applicationRoute,
		@Nullable String contactType,
		@Nullable String creationSourceUrl) {
}

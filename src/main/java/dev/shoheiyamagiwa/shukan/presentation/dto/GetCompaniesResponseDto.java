package dev.shoheiyamagiwa.shukan.presentation.dto;

import java.util.List;

public record GetCompaniesResponseDto(
	PaginationResponseDto pagination, List<CompanyResponseDto> data) {
}

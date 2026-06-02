package dev.shoheiyamagiwa.shukan.presentation.dto;

import java.util.List;

public record GetScoutsResponseDto(PaginationResponseDto pagination, List<ScoutResponseDto> data) {
}

package dev.shoheiyamagiwa.shukan.presentation.dto;

import java.util.List;

public record GetEventsResponseDto(PaginationResponseDto pagination, List<EventResponseDto> data) {
}

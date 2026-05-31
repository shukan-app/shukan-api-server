package dev.shoheiyamagiwa.shukan.presentation.dto;

import java.util.List;

public record GetTasksResponseDto(PaginationResponseDto pagination, List<TaskResponseDto> data) {
}

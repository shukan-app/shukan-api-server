package dev.shoheiyamagiwa.shukan.presentation.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Task;
import dev.shoheiyamagiwa.shukan.presentation.dto.TaskResponseDto;

public final class TaskPresentationMapper {
	
	public static TaskResponseDto toTaskResponse(Task task) {
		return new TaskResponseDto(
			task.id(),
			task.title(),
			CompanyPresentationMapper.toCompanyResponse(task.company()),
			task.type().getValue(),
			task.status().getValue(),
			task.createdBy().getValue(),
			task.creationSourceUrl(),
			task.deadline(),
			task.createdAt(),
			task.updatedAt(),
			task.deletedAt());
	}
}

package dev.shoheiyamagiwa.shukan.infra.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.entity.Task;
import dev.shoheiyamagiwa.shukan.domain.vo.*;
import dev.shoheiyamagiwa.shukan.infra.dto.TaskDto;

public final class TaskMapper {
	public static Task toEntity(TaskDto dto) {
		Company company = new Company(
			dto.companyId(),
			dto.companyName(),
			dto.companyAppliedRole(),
			CompanyStatus.fromDatabaseValue(dto.companyStatus()),
			RecruitingPlatform.fromDatabaseValue(dto.companyApplicationRoute()),
			ContactType.fromDatabaseValue(dto.companyContactType()),
			dto.companyCreationSourceUrl(),
			dto.companyCreatedAt(),
			dto.companyUpdatedAt(),
			dto.companyDeletedAt());

		return new Task(
			dto.id(),
			dto.title(),
			company,
			TaskType.fromDatabaseValue(dto.type()),
			TaskStatus.fromDatabaseValue(dto.status()),
			TaskCreationSource.fromDatabaseValue(dto.createdBy()),
			dto.creationSourceUrl(),
			dto.deadline(),
			dto.createdAt(),
			dto.updatedAt(),
			dto.deletedAt());
	}
}

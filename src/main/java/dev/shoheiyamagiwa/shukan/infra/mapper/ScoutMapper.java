package dev.shoheiyamagiwa.shukan.infra.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.ScoutStatus;
import dev.shoheiyamagiwa.shukan.infra.dto.ScoutDto;

public final class ScoutMapper {
	public static Scout toEntity(ScoutDto dto) {
		return new Scout(
			dto.id(),
			dto.title(),
			dto.companyName(),
			ScoutStatus.fromDatabaseValue(dto.status()),
			RecruitingPlatform.fromDatabaseValue(dto.platform()),
			dto.detailsUrl(),
			dto.creationSourceUrl(),
			dto.createdAt(),
			dto.updatedAt(),
			dto.deletedAt());
	}
}

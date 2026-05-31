package dev.shoheiyamagiwa.shukan.infra.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.entity.Event;
import dev.shoheiyamagiwa.shukan.domain.vo.*;
import dev.shoheiyamagiwa.shukan.infra.dto.EventDto;

public final class EventMapper {
	public static Event toEntity(EventDto dto) {
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

		return new Event(
			dto.id(),
			dto.title(),
			company,
			EventType.fromDatabaseValue(dto.type()),
			EventStatus.fromDatabaseValue(dto.status()),
			EventFormatType.fromDatabaseValue(dto.formatType()),
			dto.location(),
			dto.creationSourceUrl(),
			dto.beginAt(),
			dto.endAt(),
			dto.createdAt(),
			dto.updatedAt(),
			dto.deletedAt());
	}
}

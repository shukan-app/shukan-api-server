package dev.shoheiyamagiwa.shukan.infra.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.infra.dto.CompanyDto;

public final class CompanyMapper {
	
	public static Company toEntity(CompanyDto dto) {
		return new Company(
				dto.id(),
				dto.name(),
				dto.appliedRole(),
				CompanyStatus.fromValue(dto.status()),
				RecruitingPlatform.fromValue(dto.applicationRoute()),
				ContactType.fromValue(dto.contactType()),
				dto.createdAt(),
				dto.updatedAt(),
				dto.deletedAt());
	}
	
	public static CompanyDto toDto(Company entity) {
		return new CompanyDto(
				entity.id(),
				entity.name(),
				entity.appliedRole(),
				entity.status().getValue(),
				entity.applicationRoute().getValue(),
				entity.contactType().getValue(),
				entity.createdAt(),
				entity.updatedAt(),
				entity.deletedAt());
	}
}

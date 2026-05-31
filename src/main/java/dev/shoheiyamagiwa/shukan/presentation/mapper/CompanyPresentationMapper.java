package dev.shoheiyamagiwa.shukan.presentation.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.presentation.dto.CompanyDetailResponseDto;
import dev.shoheiyamagiwa.shukan.presentation.dto.CompanyResponseDto;

public final class CompanyPresentationMapper {
	
	public static CompanyResponseDto toCompanyResponse(Company company) {
		return new CompanyResponseDto(
				company.id(),
				company.name(),
				company.appliedRole(),
				company.status().getValue(),
				company.createdAt(),
				company.updatedAt(),
				company.deletedAt());
	}
	
	public static CompanyDetailResponseDto toCompanyDetailResponse(Company company) {
		return new CompanyDetailResponseDto(
				company.id(),
				company.name(),
				company.appliedRole(),
				company.status().getValue(),
				company.applicationRoute().getValue(),
				company.contactType().getValue(),
				company.creationSourceUrl(),
				company.createdAt(),
				company.updatedAt(),
				company.deletedAt());
	}
}

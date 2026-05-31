package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.infra.repository.CompanyRepository;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CompanyService {
	private final CompanyRepository companyRepository;
	
	public CompanyService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}
	
	public CompaniesPage getCompanies(
		String authId,
		int page,
		int pageSize,
		@Nullable String q,
		@Nullable CompanyStatus status,
		@Nullable String sort,
		@Nullable String order) {
		List<Company> companies = companyRepository.findAll(authId, page, pageSize, q, status, sort, order);
		int total = companyRepository.count(authId, q, status);
		int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
		return new CompaniesPage(companies, page, pageSize, totalPages);
	}
	
	public Optional<Company> findCompany(String authId, UUID companyId) {
		return companyRepository.findById(authId, companyId);
	}
	
	public Optional<Company> registerCompany(
		String authId,
		String name,
		String appliedRole,
		CompanyStatus status,
		RecruitingPlatform applicationRoute,
		ContactType contactType,
		@Nullable String creationSourceUrl) {
		return companyRepository.create(
			authId, name, appliedRole, status, applicationRoute, contactType, creationSourceUrl);
	}
	
	public Optional<Company> updateCompany(
		String authId,
		UUID companyId,
		String name,
		String appliedRole,
		CompanyStatus status,
		RecruitingPlatform applicationRoute,
		ContactType contactType,
		@Nullable String creationSourceUrl) {
		return companyRepository.update(
			authId, companyId, name, appliedRole, status, applicationRoute, contactType, creationSourceUrl);
	}
	
	public boolean deleteCompany(String authId, UUID companyId) {
		return companyRepository.softDelete(authId, companyId);
	}
}

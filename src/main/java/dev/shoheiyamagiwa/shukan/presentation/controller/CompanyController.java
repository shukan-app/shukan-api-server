package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.middleware.AuthenticatedRequestContext;
import dev.shoheiyamagiwa.shukan.presentation.ErrorResponses;
import dev.shoheiyamagiwa.shukan.presentation.dto.*;
import dev.shoheiyamagiwa.shukan.presentation.mapper.CompanyPresentationMapper;
import dev.shoheiyamagiwa.shukan.service.CompaniesPage;
import dev.shoheiyamagiwa.shukan.service.CompanyService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.router.JavalinDefaultRoutingApi;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class CompanyController {
	private final CompanyService companyService;
	
	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}
	
	private static String requireAuthId(Context ctx) {
		AuthenticatedRequestContext auth = ctx.attribute(AuthenticatedRequestContext.ATTRIBUTE_NAME);
		if (auth == null) {
			throw new IllegalStateException("Missing authenticated request context");
		}
		
		return auth.userId();
	}
	
	@Nullable
	private static UUID parseCompanyId(Context ctx) {
		try {
			return UUID.fromString(ctx.pathParam("id"));
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid company ID");
			return null;
		}
	}
	
	@Nullable
	private static Integer parseIntParam(Context ctx, String parameterName, @Nullable String value, int defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid " + parameterName + ": " + value);
			return null;
		}
	}
	
	@Nullable
	private static CompanyStatus parseCompanyStatus(Context ctx, @Nullable String value) {
		if (value == null) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "status is required");
			return null;
		}
		try {
			return CompanyStatus.fromValue(value);
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid status: " + value);
			return null;
		}
	}
	
	@Nullable
	private static RecruitingPlatform parseRecruitingPlatform(Context ctx, @Nullable String value) {
		if (value == null) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "applicationRoute is required");
			return null;
		}
		try {
			return RecruitingPlatform.fromValue(value);
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid applicationRoute: " + value);
			return null;
		}
	}
	
	@Nullable
	private static ContactType parseContactType(Context ctx, @Nullable String value) {
		if (value == null) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "contactType is required");
			return null;
		}
		try {
			return ContactType.fromValue(value);
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid contactType: " + value);
			return null;
		}
	}
	
	public void registerRoutes(JavalinDefaultRoutingApi routes) {
		routes.get("/users/me/companies", this::getCompanies);
		routes.post("/users/me/companies", this::registerCompany);
		routes.get("/users/me/companies/{id}", this::getCompany);
		routes.put("/users/me/companies/{id}", this::updateCompany);
		routes.delete("/users/me/companies/{id}", this::deleteCompany);
	}
	
	private void getCompanies(Context ctx) {
		String authId = requireAuthId(ctx);
		
		Integer page = parseIntParam(ctx, "page", ctx.queryParam("page"), 0);
		if (page == null) {
			return;
		}

		Integer pageSize = parseIntParam(ctx, "pageSize", ctx.queryParam("pageSize"), 50);
		if (pageSize == null) {
			return;
		}
		
		if (page < 0 || page > 99) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "page must be between 0 and 99");
			return;
		}
		
		if (pageSize < 1 || pageSize > 100) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "pageSize must be between 1 and 100");
			return;
		}
		
		String q = ctx.queryParam("q");
		if (q != null && (q.isEmpty() || q.length() > 32)) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "q must be between 1 and 32 characters");
			return;
		}
		
		CompanyStatus status = null;
		String statusParam = ctx.queryParam("status");
		if (statusParam != null) {
			try {
				status = CompanyStatus.fromValue(statusParam);
			} catch (IllegalArgumentException e) {
				ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid status: " + statusParam);
				return;
			}
		}
		
		String sort = ctx.queryParam("sort");
		if (sort != null && !sort.equals("taskDate") && !sort.equals("eventDate")) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid sort: " + sort);
			return;
		}
		
		String order = ctx.queryParam("order");
		if (order != null && !order.equals("ascend") && !order.equals("descend")) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid order: " + order);
			return;
		}
		
		CompaniesPage result = companyService.getCompanies(authId, page, pageSize, q, status, sort, order);
		
		GetCompaniesResponseDto response = new GetCompaniesResponseDto(
				new PaginationResponseDto(result.page(), result.pageSize(), result.totalPages()),
				result.companies().stream().map(CompanyPresentationMapper::toCompanyResponse).toList());
		ctx.json(response);
	}
	
	private void registerCompany(Context ctx) {
		String authId = requireAuthId(ctx);
		RegisterCompanyRequestDto body = ctx.bodyAsClass(RegisterCompanyRequestDto.class);
		
		String name = body.name();
		String appliedRole = body.appliedRole();
		String statusValue = body.status();
		String applicationRouteValue = body.applicationRoute();
		String contactTypeValue = body.contactType();
		String creationSourceUrl = body.creationSourceUrl();
		
		if (name == null || name.length() < 2 || name.length() > 32) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "name must be between 2 and 32 characters");
			return;
		}
		
		if (appliedRole == null || appliedRole.length() < 2 || appliedRole.length() > 32) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "appliedRole must be between 2 and 32 characters");
			return;
		}
		
		CompanyStatus status = parseCompanyStatus(ctx, statusValue);
		if (status == null) {
			return;
		}
		
		RecruitingPlatform applicationRoute = parseRecruitingPlatform(ctx, applicationRouteValue);
		if (applicationRoute == null) {
			return;
		}
		
		ContactType contactType = parseContactType(ctx, contactTypeValue);
		if (contactType == null) {
			return;
		}
		
		Optional<Company> company = companyService.registerCompany(authId, name, appliedRole, status, applicationRoute, contactType, creationSourceUrl);
		if (company.isEmpty()) {
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "User not found");
			return;
		}

		ctx.json(CompanyPresentationMapper.toCompanyDetailResponse(company.get()));
	}
	
	private void getCompany(Context ctx) {
		String authId = requireAuthId(ctx);
		UUID companyId = parseCompanyId(ctx);
		if (companyId == null) {
			return;
		}
		
		Optional<Company> company = companyService.findCompany(authId, companyId);
		if (company.isEmpty()) {
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "Company not found");
			return;
		}
		
		ctx.json(CompanyPresentationMapper.toCompanyDetailResponse(company.get()));
	}
	
	private void updateCompany(Context ctx) {
		String authId = requireAuthId(ctx);
		UUID companyId = parseCompanyId(ctx);
		if (companyId == null) {
			return;
		}
		
		UpdateCompanyRequestDto body = ctx.bodyAsClass(UpdateCompanyRequestDto.class);
		
		String name = body.name();
		String appliedRole = body.appliedRole();
		String statusValue = body.status();
		String applicationRouteValue = body.applicationRoute();
		String contactTypeValue = body.contactType();
		String creationSourceUrl = body.creationSourceUrl();
		
		if (name == null || name.length() < 2 || name.length() > 32) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "name must be between 2 and 32 characters");
			return;
		}
		if (appliedRole == null || appliedRole.length() < 2 || appliedRole.length() > 32) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "appliedRole must be between 2 and 32 characters");
			return;
		}
		
		CompanyStatus status = parseCompanyStatus(ctx, statusValue);
		if (status == null) {
			return;
		}
		
		RecruitingPlatform applicationRoute = parseRecruitingPlatform(ctx, applicationRouteValue);
		if (applicationRoute == null) {
			return;
		}
		
		ContactType contactType = parseContactType(ctx, contactTypeValue);
		if (contactType == null) {
			return;
		}
		
		Optional<Company> updated = companyService.updateCompany(authId, companyId, name, appliedRole, status, applicationRoute, contactType, creationSourceUrl);
		if (updated.isEmpty()) {
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "Company not found");
			return;
		}
		
		ctx.json(CompanyPresentationMapper.toCompanyDetailResponse(updated.get()));
	}
	
	private void deleteCompany(Context ctx) {
		String authId = requireAuthId(ctx);
		UUID companyId = parseCompanyId(ctx);
		if (companyId == null) {
			return;
		}
		
		boolean deleted = companyService.deleteCompany(authId, companyId);
		if (!deleted) {
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "Company not found");
			return;
		}
		ctx.status(HttpStatus.NO_CONTENT);
	}
}

package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecrutingPlatform;
import dev.shoheiyamagiwa.shukan.middleware.AuthenticatedRequestContext;
import dev.shoheiyamagiwa.shukan.presentation.dto.*;
import dev.shoheiyamagiwa.shukan.presentation.mapper.CompanyPresentationMapper;
import dev.shoheiyamagiwa.shukan.service.CompaniesPage;
import dev.shoheiyamagiwa.shukan.service.CompanyService;
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
	
	private static String requireAuthId(io.javalin.http.Context ctx) {
		AuthenticatedRequestContext auth = ctx.attribute(AuthenticatedRequestContext.ATTRIBUTE_NAME);
		if (auth == null) {
			throw new IllegalStateException("Missing authenticated request context");
		}
		return auth.userId();
	}
	
	private static UUID parseCompanyId(io.javalin.http.Context ctx) {
		try {
			return UUID.fromString(ctx.pathParam("id"));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid company ID");
		}
	}
	
	private static int parseIntParam(@Nullable String value, int defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	@Nullable
	private static CompanyStatus parseCompanyStatus(
			io.javalin.http.Context ctx, @Nullable String value) {
		if (value == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("status is required"));
			return null;
		}
		try {
			return CompanyStatus.fromValue(value);
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid status: " + value));
			return null;
		}
	}
	
	@Nullable
	private static RecrutingPlatform parseRecrutingPlatform(
			io.javalin.http.Context ctx, @Nullable String value) {
		if (value == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("applicationRoute is required"));
			return null;
		}
		try {
			return RecrutingPlatform.fromValue(value);
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST)
					.json(new ErrorResponseDto("Invalid applicationRoute: " + value));
			return null;
		}
	}
	
	@Nullable
	private static ContactType parseContactType(io.javalin.http.Context ctx, @Nullable String value) {
		if (value == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("contactType is required"));
			return null;
		}
		try {
			return ContactType.fromValue(value);
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST)
					.json(new ErrorResponseDto("Invalid contactType: " + value));
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
	
	private void getCompanies(io.javalin.http.Context ctx) {
		String authId = requireAuthId(ctx);
		
		int page = parseIntParam(ctx.queryParam("page"), 0);
		int pageSize = parseIntParam(ctx.queryParam("pageSize"), 50);
		
		if (page < 0 || page > 99) {
			ctx.status(HttpStatus.BAD_REQUEST)
					.json(new ErrorResponseDto("page must be between 0 and 99"));
			return;
		}
		if (pageSize < 1 || pageSize > 100) {
			ctx.status(HttpStatus.BAD_REQUEST)
					.json(new ErrorResponseDto("pageSize must be between 1 and 100"));
			return;
		}
		
		@Nullable String q = ctx.queryParam("q");
		if (q != null && (q.length() < 1 || q.length() > 32)) {
			ctx.status(HttpStatus.BAD_REQUEST)
					.json(new ErrorResponseDto("q must be between 1 and 32 characters"));
			return;
		}
		
		@Nullable CompanyStatus status = null;
		@Nullable String statusParam = ctx.queryParam("status");
		if (statusParam != null) {
			try {
				status = CompanyStatus.fromValue(statusParam);
			} catch (IllegalArgumentException e) {
				ctx.status(HttpStatus.BAD_REQUEST)
						.json(new ErrorResponseDto("Invalid status: " + statusParam));
				return;
			}
		}
		
		@Nullable String sort = ctx.queryParam("sort");
		if (sort != null && !sort.equals("taskDate") && !sort.equals("eventDate")) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid sort: " + sort));
			return;
		}
		
		@Nullable String order = ctx.queryParam("order");
		if (order != null && !order.equals("ascend") && !order.equals("descend")) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid order: " + order));
			return;
		}
		
		CompaniesPage result =
				companyService.getCompanies(authId, page, pageSize, q, status, sort, order);
		
		GetCompaniesResponseDto response =
				new GetCompaniesResponseDto(
						new PaginationResponseDto(result.page(), result.pageSize(), result.totalPages()),
						result.companies().stream().map(CompanyPresentationMapper::toCompanyResponse).toList());
		ctx.json(response);
	}
	
	private void registerCompany(io.javalin.http.Context ctx) {
		String authId = requireAuthId(ctx);
		RegisterCompanyRequestDto body = ctx.bodyAsClass(RegisterCompanyRequestDto.class);
		
		@Nullable String name = body.name();
		@Nullable String appliedRole = body.appliedRole();
		@Nullable String statusValue = body.status();
		@Nullable String applicationRouteValue = body.applicationRoute();
		@Nullable String contactTypeValue = body.contactType();
		
		if (name == null || name.length() < 2 || name.length() > 32) {
			ctx.status(HttpStatus.BAD_REQUEST)
					.json(new ErrorResponseDto("name must be between 2 and 32 characters"));
			return;
		}
		if (appliedRole == null || appliedRole.length() < 2 || appliedRole.length() > 32) {
			ctx.status(HttpStatus.BAD_REQUEST)
					.json(new ErrorResponseDto("appliedRole must be between 2 and 32 characters"));
			return;
		}
		
		CompanyStatus status = parseCompanyStatus(ctx, statusValue);
		if (status == null) {
			return;
		}
		RecrutingPlatform applicationRoute = parseRecrutingPlatform(ctx, applicationRouteValue);
		if (applicationRoute == null) {
			return;
		}
		ContactType contactType = parseContactType(ctx, contactTypeValue);
		if (contactType == null) {
			return;
		}
		
		Company company =
				companyService.registerCompany(
						authId, name, appliedRole, status, applicationRoute, contactType);
		ctx.json(CompanyPresentationMapper.toCompanyDetailResponse(company));
	}
	
	private void getCompany(io.javalin.http.Context ctx) {
		String authId = requireAuthId(ctx);
		UUID companyId = parseCompanyId(ctx);
		
		Optional<Company> company = companyService.findCompany(authId, companyId);
		if (company.isEmpty()) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Company not found"));
			return;
		}
		ctx.json(CompanyPresentationMapper.toCompanyDetailResponse(company.get()));
	}
	
	private void updateCompany(io.javalin.http.Context ctx) {
		String authId = requireAuthId(ctx);
		UUID companyId = parseCompanyId(ctx);
		UpdateCompanyRequestDto body = ctx.bodyAsClass(UpdateCompanyRequestDto.class);
		
		@Nullable String name = body.name();
		@Nullable String appliedRole = body.appliedRole();
		@Nullable String statusValue = body.status();
		@Nullable String applicationRouteValue = body.applicationRoute();
		@Nullable String contactTypeValue = body.contactType();
		
		if (name == null || name.length() < 2 || name.length() > 32) {
			ctx.status(HttpStatus.BAD_REQUEST)
					.json(new ErrorResponseDto("name must be between 2 and 32 characters"));
			return;
		}
		if (appliedRole == null || appliedRole.length() < 2 || appliedRole.length() > 32) {
			ctx.status(HttpStatus.BAD_REQUEST)
					.json(new ErrorResponseDto("appliedRole must be between 2 and 32 characters"));
			return;
		}
		
		CompanyStatus status = parseCompanyStatus(ctx, statusValue);
		if (status == null) {
			return;
		}
		RecrutingPlatform applicationRoute = parseRecrutingPlatform(ctx, applicationRouteValue);
		if (applicationRoute == null) {
			return;
		}
		ContactType contactType = parseContactType(ctx, contactTypeValue);
		if (contactType == null) {
			return;
		}
		
		Optional<Company> updated =
				companyService.updateCompany(
						authId, companyId, name, appliedRole, status, applicationRoute, contactType);
		if (updated.isEmpty()) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Company not found"));
			return;
		}
		ctx.json(CompanyPresentationMapper.toCompanyDetailResponse(updated.get()));
	}
	
	private void deleteCompany(io.javalin.http.Context ctx) {
		String authId = requireAuthId(ctx);
		UUID companyId = parseCompanyId(ctx);
		
		boolean deleted = companyService.deleteCompany(authId, companyId);
		if (!deleted) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Company not found"));
			return;
		}
		ctx.status(HttpStatus.NO_CONTENT);
	}
}

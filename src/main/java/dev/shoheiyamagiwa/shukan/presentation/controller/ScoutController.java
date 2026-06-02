package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.ScoutStatus;
import dev.shoheiyamagiwa.shukan.middleware.AuthenticatedRequestContext;
import dev.shoheiyamagiwa.shukan.presentation.ErrorResponses;
import dev.shoheiyamagiwa.shukan.presentation.dto.GetScoutsResponseDto;
import dev.shoheiyamagiwa.shukan.presentation.dto.PaginationResponseDto;
import dev.shoheiyamagiwa.shukan.presentation.dto.ScoutUpdateRequestDto;
import dev.shoheiyamagiwa.shukan.presentation.mapper.ScoutPresentationMapper;
import dev.shoheiyamagiwa.shukan.service.ScoutService;
import dev.shoheiyamagiwa.shukan.service.ScoutsPage;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.router.JavalinDefaultRoutingApi;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ScoutController {
	private final ScoutService scoutService;
	
	public ScoutController(ScoutService scoutService) {
		this.scoutService = scoutService;
	}
	
	@Nullable
	private static String requireAuthId(Context ctx) {
		AuthenticatedRequestContext auth = ctx.attribute(AuthenticatedRequestContext.ATTRIBUTE_NAME);
		if (auth == null) {
			ErrorResponses.respond(ctx, HttpStatus.UNAUTHORIZED, "Unauthorized");
			return null;
		}
		
		return auth.userId();
	}
	
	@Nullable
	private static UUID parseScoutId(Context ctx) {
		try {
			return UUID.fromString(ctx.pathParam("id"));
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid scout ID");
			return null;
		}
	}
	
	@Nullable
	private static Integer parseIntParam(
		Context ctx, String parameterName, @Nullable String value, int defaultValue) {
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
	private static ScoutStatus parseScoutStatus(Context ctx, @Nullable String value) {
		if (value == null) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "status is required");
			return null;
		}
		try {
			return ScoutStatus.fromValue(value);
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid status: " + value);
			return null;
		}
	}
	
	@Nullable
	private static RecruitingPlatform parseRecruitingPlatform(Context ctx, @Nullable String value) {
		if (value == null) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "platform is required");
			return null;
		}
		try {
			return RecruitingPlatform.fromValue(value);
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid platform: " + value);
			return null;
		}
	}
	
	private static boolean validateTitle(Context ctx, @Nullable String title) {
		if (title == null || title.length() < 2 || title.length() > 50) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "title must be between 2 and 50 characters");
			return false;
		}
		return true;
	}
	
	private static boolean validateCompanyName(Context ctx, @Nullable String companyName) {
		if (companyName == null || companyName.length() < 2 || companyName.length() > 50) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "companyName must be between 2 and 50 characters");
			return false;
		}
		return true;
	}
	
	private static boolean validateUrl(Context ctx, String fieldName, @Nullable String value) {
		if (value != null && (value.length() < 7 || value.length() > 2048)) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, fieldName + " must be between 7 and 2048 characters");
			return false;
		}
		return true;
	}
	
	private static boolean validateScoutInput(
		Context ctx,
		@Nullable String title,
		@Nullable String companyName,
		@Nullable ScoutStatus status,
		@Nullable RecruitingPlatform platform,
		@Nullable String detailsUrl,
		@Nullable String creationSourceUrl) {
		if (!validateTitle(ctx, title)) {
			return false;
		}
		
		if (!validateCompanyName(ctx, companyName)) {
			return false;
		}
		
		if (status == null) {
			return false;
		}
		
		if (platform == null) {
			return false;
		}
		
		if (!validateUrl(ctx, "detailsUrl", detailsUrl)) {
			return false;
		}
		
		if (!validateUrl(ctx, "creationSourceUrl", creationSourceUrl)) {
			return false;
		}
		
		return true;
	}
	
	private boolean ensureUserExists(Context ctx, String authId) {
		if (!scoutService.userExists(authId)) {
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "User not found");
			return false;
		}
		return true;
	}
	
	public void registerRoutes(JavalinDefaultRoutingApi routes) {
		routes.get("/users/me/scouts", this::getScouts);
		routes.post("/users/me/scouts", this::registerScout);
		routes.put("/users/me/scouts/{id}", this::updateScout);
		routes.delete("/users/me/scouts/{id}", this::deleteScout);
	}
	
	private void getScouts(Context ctx) {
		String authId = requireAuthId(ctx);
		if (authId == null) {
			return;
		}
		if (!ensureUserExists(ctx, authId)) {
			return;
		}
		
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
		
		ScoutStatus status = null;
		String statusParam = ctx.queryParam("status");
		if (statusParam != null) {
			try {
				status = ScoutStatus.fromValue(statusParam);
			} catch (IllegalArgumentException e) {
				ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid status: " + statusParam);
				return;
			}
		}
		
		RecruitingPlatform platform = null;
		String platformParam = ctx.queryParam("platform");
		if (platformParam != null) {
			try {
				platform = RecruitingPlatform.fromValue(platformParam);
			} catch (IllegalArgumentException e) {
				ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid platform: " + platformParam);
				return;
			}
		}
		
		ScoutsPage result = scoutService.getScouts(authId, page, pageSize, status, platform);
		
		GetScoutsResponseDto response =
			new GetScoutsResponseDto(
				new PaginationResponseDto(result.page(), result.pageSize(), result.totalPages()),
				result.scouts().stream().map(ScoutPresentationMapper::toScoutResponse).toList());
		ctx.json(response);
	}
	
	private void registerScout(Context ctx) {
		String authId = requireAuthId(ctx);
		if (authId == null) {
			return;
		}
		if (!ensureUserExists(ctx, authId)) {
			return;
		}
		
		ScoutUpdateRequestDto body = ctx.bodyAsClass(ScoutUpdateRequestDto.class);
		String title = body.title();
		String companyName = body.companyName();
		ScoutStatus status = parseScoutStatus(ctx, body.status());
		RecruitingPlatform platform = parseRecruitingPlatform(ctx, body.platform());
		if (!validateScoutInput(
			ctx, title, companyName, status, platform, body.detailsUrl(), body.creationSourceUrl())) {
			return;
		}
		
		Optional<Scout> scout =
			scoutService.registerScout(
				authId,
				Objects.requireNonNull(title),
				Objects.requireNonNull(companyName),
				Objects.requireNonNull(status),
				Objects.requireNonNull(platform),
				body.detailsUrl(),
				body.creationSourceUrl());
		if (scout.isEmpty()) {
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "User not found");
			return;
		}
		
		ctx.status(HttpStatus.CREATED).json(ScoutPresentationMapper.toScoutResponse(scout.get()));
	}
	
	private void updateScout(Context ctx) {
		String authId = requireAuthId(ctx);
		if (authId == null) {
			return;
		}
		if (!ensureUserExists(ctx, authId)) {
			return;
		}
		
		UUID scoutId = parseScoutId(ctx);
		if (scoutId == null) {
			return;
		}
		
		ScoutUpdateRequestDto body = ctx.bodyAsClass(ScoutUpdateRequestDto.class);
		String title = body.title();
		String companyName = body.companyName();
		ScoutStatus status = parseScoutStatus(ctx, body.status());
		RecruitingPlatform platform = parseRecruitingPlatform(ctx, body.platform());
		if (!validateScoutInput(
			ctx, title, companyName, status, platform, body.detailsUrl(), body.creationSourceUrl())) {
			return;
		}
		
		Optional<Scout> updated =
			scoutService.updateScout(
				authId,
				scoutId,
				Objects.requireNonNull(title),
				Objects.requireNonNull(companyName),
				Objects.requireNonNull(status),
				Objects.requireNonNull(platform),
				body.detailsUrl(),
				body.creationSourceUrl());
		if (updated.isEmpty()) {
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "Scout not found");
			return;
		}
		
		ctx.json(ScoutPresentationMapper.toScoutResponse(updated.get()));
	}
	
	private void deleteScout(Context ctx) {
		String authId = requireAuthId(ctx);
		if (authId == null) {
			return;
		}
		if (!ensureUserExists(ctx, authId)) {
			return;
		}
		
		UUID scoutId = parseScoutId(ctx);
		if (scoutId == null) {
			return;
		}
		
		boolean deleted = scoutService.deleteScout(authId, scoutId);
		if (!deleted) {
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "Scout not found");
			return;
		}
		ctx.status(HttpStatus.NO_CONTENT);
	}
}

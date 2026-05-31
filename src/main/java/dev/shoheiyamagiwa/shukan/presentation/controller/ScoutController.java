package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.ScoutStatus;
import dev.shoheiyamagiwa.shukan.middleware.AuthenticatedRequestContext;
import dev.shoheiyamagiwa.shukan.presentation.dto.*;
import dev.shoheiyamagiwa.shukan.presentation.mapper.ScoutPresentationMapper;
import dev.shoheiyamagiwa.shukan.service.ScoutService;
import dev.shoheiyamagiwa.shukan.service.ScoutsPage;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.router.JavalinDefaultRoutingApi;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class ScoutController {
  private final ScoutService scoutService;

  public ScoutController(ScoutService scoutService) {
    this.scoutService = scoutService;
  }

  private static String requireAuthId(Context ctx) {
    AuthenticatedRequestContext auth = ctx.attribute(AuthenticatedRequestContext.ATTRIBUTE_NAME);
    if (auth == null) {
      throw new IllegalStateException("Missing authenticated request context");
    }

    return auth.userId();
  }

  private boolean ensureUserExists(Context ctx, String authId) {
    if (!scoutService.userExists(authId)) {
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("User not found"));
      return false;
    }
    return true;
  }

  @Nullable
  private static UUID parseScoutId(Context ctx) {
    try {
      return UUID.fromString(ctx.pathParam("id"));
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid scout ID"));
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
      ctx.status(HttpStatus.BAD_REQUEST)
          .json(new ErrorResponseDto("Invalid " + parameterName + ": " + value));
      return null;
    }
  }

  @Nullable
  private static ScoutStatus parseScoutStatus(Context ctx, @Nullable String value) {
    if (value == null) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("status is required"));
      return null;
    }
    try {
      return ScoutStatus.fromValue(value);
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid status: " + value));
      return null;
    }
  }

  @Nullable
  private static RecruitingPlatform parseRecruitingPlatform(Context ctx, @Nullable String value) {
    if (value == null) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("platform is required"));
      return null;
    }
    try {
      return RecruitingPlatform.fromValue(value);
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid platform: " + value));
      return null;
    }
  }

  private static boolean validateTitle(Context ctx, @Nullable String title) {
    if (title == null || title.length() < 2 || title.length() > 50) {
      ctx.status(HttpStatus.BAD_REQUEST)
          .json(new ErrorResponseDto("title must be between 2 and 50 characters"));
      return false;
    }
    return true;
  }

  private static boolean validateCompanyName(Context ctx, @Nullable String companyName) {
    if (companyName == null || companyName.length() < 2 || companyName.length() > 50) {
      ctx.status(HttpStatus.BAD_REQUEST)
          .json(new ErrorResponseDto("companyName must be between 2 and 50 characters"));
      return false;
    }
    return true;
  }

  private static boolean validateUrl(Context ctx, String fieldName, @Nullable String value) {
    if (value != null && (value.length() < 7 || value.length() > 2048)) {
      ctx.status(HttpStatus.BAD_REQUEST)
          .json(new ErrorResponseDto(fieldName + " must be between 7 and 2048 characters"));
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
      ctx.status(HttpStatus.BAD_REQUEST)
          .json(new ErrorResponseDto("page must be between 0 and 99"));
      return;
    }

    if (pageSize < 1 || pageSize > 100) {
      ctx.status(HttpStatus.BAD_REQUEST)
          .json(new ErrorResponseDto("pageSize must be between 1 and 100"));
      return;
    }

    ScoutStatus status = null;
    String statusParam = ctx.queryParam("status");
    if (statusParam != null) {
      try {
        status = ScoutStatus.fromValue(statusParam);
      } catch (IllegalArgumentException e) {
        ctx.status(HttpStatus.BAD_REQUEST)
            .json(new ErrorResponseDto("Invalid status: " + statusParam));
        return;
      }
    }

    RecruitingPlatform platform = null;
    String platformParam = ctx.queryParam("platform");
    if (platformParam != null) {
      try {
        platform = RecruitingPlatform.fromValue(platformParam);
      } catch (IllegalArgumentException e) {
        ctx.status(HttpStatus.BAD_REQUEST)
            .json(new ErrorResponseDto("Invalid platform: " + platformParam));
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
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("User not found"));
      return;
    }

    ctx.status(HttpStatus.CREATED).json(ScoutPresentationMapper.toScoutResponse(scout.get()));
  }

  private void updateScout(Context ctx) {
    String authId = requireAuthId(ctx);
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
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Scout not found"));
      return;
    }

    ctx.json(ScoutPresentationMapper.toScoutResponse(updated.get()));
  }

  private void deleteScout(Context ctx) {
    String authId = requireAuthId(ctx);
    if (!ensureUserExists(ctx, authId)) {
      return;
    }

    UUID scoutId = parseScoutId(ctx);
    if (scoutId == null) {
      return;
    }

    boolean deleted = scoutService.deleteScout(authId, scoutId);
    if (!deleted) {
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Scout not found"));
      return;
    }
    ctx.status(HttpStatus.NO_CONTENT);
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
}

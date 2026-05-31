package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.domain.entity.Event;
import dev.shoheiyamagiwa.shukan.domain.vo.EventFormatType;
import dev.shoheiyamagiwa.shukan.domain.vo.EventStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.EventType;
import dev.shoheiyamagiwa.shukan.middleware.AuthenticatedRequestContext;
import dev.shoheiyamagiwa.shukan.presentation.dto.*;
import dev.shoheiyamagiwa.shukan.presentation.mapper.EventPresentationMapper;
import dev.shoheiyamagiwa.shukan.service.EventService;
import dev.shoheiyamagiwa.shukan.service.EventsPage;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.router.JavalinDefaultRoutingApi;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class EventController {
	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	private static String requireAuthId(Context ctx) {
		AuthenticatedRequestContext auth = ctx.attribute(AuthenticatedRequestContext.ATTRIBUTE_NAME);
		if (auth == null) {
			throw new IllegalStateException("Missing authenticated request context");
		}

		return auth.userId();
	}

	private boolean ensureUserExists(Context ctx, String authId) {
		if (!eventService.userExists(authId)) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("User not found"));
			return false;
		}
		return true;
	}

	@Nullable
	private static UUID parseEventId(Context ctx) {
		try {
			return UUID.fromString(ctx.pathParam("id"));
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid event ID"));
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
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid " + parameterName + ": " + value));
			return null;
		}
	}

	@Nullable
	private static EventType parseEventType(Context ctx, @Nullable String value) {
		if (value == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("type is required"));
			return null;
		}
		try {
			return EventType.fromValue(value);
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid type: " + value));
			return null;
		}
	}

	@Nullable
	private static EventStatus parseEventStatus(Context ctx, @Nullable String value) {
		if (value == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("status is required"));
			return null;
		}
		try {
			return EventStatus.fromValue(value);
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid status: " + value));
			return null;
		}
	}

	@Nullable
	private static EventFormatType parseEventFormatType(Context ctx, @Nullable String value) {
		if (value == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("formatType is required"));
			return null;
		}
		try {
			return EventFormatType.fromValue(value);
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid formatType: " + value));
			return null;
		}
	}

	private static boolean validateTitle(Context ctx, @Nullable String title) {
		if (title == null || title.length() < 2 || title.length() > 50) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("title must be between 2 and 50 characters"));
			return false;
		}
		return true;
	}

	private static boolean validateLocation(Context ctx, @Nullable String location) {
		if (location != null && (location.length() < 2 || location.length() > 50)) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("location must be between 2 and 50 characters"));
			return false;
		}
		return true;
	}

	private static boolean validateCreationSourceUrl(Context ctx, @Nullable String creationSourceUrl) {
		if (creationSourceUrl != null
			&& (creationSourceUrl.length() < 7 || creationSourceUrl.length() > 2048)) {
			ctx.status(HttpStatus.BAD_REQUEST)
				.json(new ErrorResponseDto("creationSourceUrl must be between 7 and 2048 characters"));
			return false;
		}
		return true;
	}

	public void registerRoutes(JavalinDefaultRoutingApi routes) {
		routes.get("/users/me/events", this::getEvents);
		routes.post("/users/me/events", this::registerEvent);
		routes.put("/users/me/events/{id}", this::updateEvent);
		routes.delete("/users/me/events/{id}", this::deleteEvent);
	}

	private void getEvents(Context ctx) {
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
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("page must be between 0 and 99"));
			return;
		}

		if (pageSize < 1 || pageSize > 100) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("pageSize must be between 1 and 100"));
			return;
		}

		EventType type = null;
		String typeParam = ctx.queryParam("type");
		if (typeParam != null) {
			try {
				type = EventType.fromValue(typeParam);
			} catch (IllegalArgumentException e) {
				ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid type: " + typeParam));
				return;
			}
		}

		EventStatus status = null;
		String statusParam = ctx.queryParam("status");
		if (statusParam != null) {
			try {
				status = EventStatus.fromValue(statusParam);
			} catch (IllegalArgumentException e) {
				ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid status: " + statusParam));
				return;
			}
		}

		EventsPage result = eventService.getEvents(authId, page, pageSize, type, status);

		GetEventsResponseDto response = new GetEventsResponseDto(
			new PaginationResponseDto(result.page(), result.pageSize(), result.totalPages()),
			result.events().stream().map(EventPresentationMapper::toEventResponse).toList());
		ctx.json(response);
	}

	private void registerEvent(Context ctx) {
		String authId = requireAuthId(ctx);
		if (!ensureUserExists(ctx, authId)) {
			return;
		}

		EventUpdateRequestDto body = ctx.bodyAsClass(EventUpdateRequestDto.class);
		String title = body.title();
		UUID companyId = body.companyId();
		EventType type = parseEventType(ctx, body.type());
		EventStatus status = parseEventStatus(ctx, body.status());
		EventFormatType formatType = parseEventFormatType(ctx, body.formatType());
		OffsetDateTime beginAt = body.beginAt();
		OffsetDateTime endAt = body.endAt();
		if (!validateEventInput(ctx, title, companyId, type, status, formatType, body.location(), body.creationSourceUrl(), beginAt, endAt)) {
			return;
		}

		Optional<Event> event = eventService.registerEvent(
			authId,
			Objects.requireNonNull(title),
			Objects.requireNonNull(companyId),
			Objects.requireNonNull(type),
			Objects.requireNonNull(status),
			Objects.requireNonNull(formatType),
			body.location(),
			body.creationSourceUrl(),
			Objects.requireNonNull(beginAt),
			Objects.requireNonNull(endAt));
		if (event.isEmpty()) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Company not found"));
			return;
		}

		ctx.status(HttpStatus.CREATED).json(EventPresentationMapper.toEventResponse(event.get()));
	}

	private void updateEvent(Context ctx) {
		String authId = requireAuthId(ctx);
		if (!ensureUserExists(ctx, authId)) {
			return;
		}

		UUID eventId = parseEventId(ctx);
		if (eventId == null) {
			return;
		}

		EventUpdateRequestDto body = ctx.bodyAsClass(EventUpdateRequestDto.class);
		String title = body.title();
		UUID companyId = body.companyId();
		EventType type = parseEventType(ctx, body.type());
		EventStatus status = parseEventStatus(ctx, body.status());
		EventFormatType formatType = parseEventFormatType(ctx, body.formatType());
		OffsetDateTime beginAt = body.beginAt();
		OffsetDateTime endAt = body.endAt();
		if (!validateEventInput(ctx, title, companyId, type, status, formatType, body.location(), body.creationSourceUrl(), beginAt, endAt)) {
			return;
		}

		Optional<Event> updated = eventService.updateEvent(
			authId,
			eventId,
			Objects.requireNonNull(title),
			Objects.requireNonNull(companyId),
			Objects.requireNonNull(type),
			Objects.requireNonNull(status),
			Objects.requireNonNull(formatType),
			body.location(),
			body.creationSourceUrl(),
			Objects.requireNonNull(beginAt),
			Objects.requireNonNull(endAt));
		if (updated.isEmpty()) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Event not found"));
			return;
		}

		ctx.json(EventPresentationMapper.toEventResponse(updated.get()));
	}

	private void deleteEvent(Context ctx) {
		String authId = requireAuthId(ctx);
		if (!ensureUserExists(ctx, authId)) {
			return;
		}

		UUID eventId = parseEventId(ctx);
		if (eventId == null) {
			return;
		}

		boolean deleted = eventService.deleteEvent(authId, eventId);
		if (!deleted) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Event not found"));
			return;
		}
		ctx.status(HttpStatus.NO_CONTENT);
	}

	private static boolean validateEventInput(
		Context ctx,
		@Nullable String title,
		@Nullable UUID companyId,
		@Nullable EventType type,
		@Nullable EventStatus status,
		@Nullable EventFormatType formatType,
		@Nullable String location,
		@Nullable String creationSourceUrl,
		@Nullable OffsetDateTime beginAt,
		@Nullable OffsetDateTime endAt) {
		if (!validateTitle(ctx, title)) {
			return false;
		}

		if (companyId == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("companyId is required"));
			return false;
		}

		if (type == null) {
			return false;
		}

		if (status == null) {
			return false;
		}

		if (formatType == null) {
			return false;
		}

		if (!validateLocation(ctx, location)) {
			return false;
		}

		if (!validateCreationSourceUrl(ctx, creationSourceUrl)) {
			return false;
		}

		if (beginAt == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("beginAt is required"));
			return false;
		}

		if (endAt == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("endAt is required"));
			return false;
		}
		
		if (!beginAt.isBefore(endAt)) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("beginAt must be before endAt"));
			return false;
		}

		return true;
	}
}

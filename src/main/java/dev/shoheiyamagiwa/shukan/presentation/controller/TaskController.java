package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.domain.entity.Task;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskType;
import dev.shoheiyamagiwa.shukan.middleware.AuthenticatedRequestContext;
import dev.shoheiyamagiwa.shukan.presentation.dto.*;
import dev.shoheiyamagiwa.shukan.presentation.mapper.TaskPresentationMapper;
import dev.shoheiyamagiwa.shukan.service.TaskService;
import dev.shoheiyamagiwa.shukan.service.TasksPage;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.router.JavalinDefaultRoutingApi;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class TaskController {
	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	private static String requireAuthId(Context ctx) {
		AuthenticatedRequestContext auth = ctx.attribute(AuthenticatedRequestContext.ATTRIBUTE_NAME);
		if (auth == null) {
			throw new IllegalStateException("Missing authenticated request context");
		}

		return auth.userId();
	}

	private boolean ensureUserExists(Context ctx, String authId) {
		if (!taskService.userExists(authId)) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("User not found"));
			return false;
		}
		return true;
	}

	@Nullable
	private static UUID parseTaskId(Context ctx) {
		try {
			return UUID.fromString(ctx.pathParam("id"));
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid task ID"));
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
	private static TaskType parseTaskType(Context ctx, @Nullable String value) {
		if (value == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("type is required"));
			return null;
		}
		try {
			return TaskType.fromValue(value);
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid type: " + value));
			return null;
		}
	}

	@Nullable
	private static TaskStatus parseTaskStatus(Context ctx, @Nullable String value) {
		if (value == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("status is required"));
			return null;
		}
		try {
			return TaskStatus.fromValue(value);
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid status: " + value));
			return null;
		}
	}

	private static boolean validateTitle(Context ctx, @Nullable String title) {
		if (title == null || title.length() < 2 || title.length() > 32) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("title must be between 2 and 32 characters"));
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
		routes.get("/users/me/tasks", this::getTasks);
		routes.post("/users/me/tasks", this::registerTask);
		routes.put("/users/me/tasks/{id}", this::updateTask);
		routes.delete("/users/me/tasks/{id}", this::deleteTask);
	}

	private void getTasks(Context ctx) {
		String authId = requireAuthId(ctx);
		if (!ensureUserExists(ctx, authId)) {
			return;
		}

		Integer page = parseIntParam(ctx, "page", ctx.queryParam("page"), 0);
		if (page == null) {
			return;
		}

		Integer pageSize = parseIntParam(ctx, "pageSize", ctx.queryParam("pageSize"), 20);
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

		TaskType type = null;
		String typeParam = ctx.queryParam("type");
		if (typeParam != null) {
			try {
				type = TaskType.fromValue(typeParam);
			} catch (IllegalArgumentException e) {
				ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("Invalid type: " + typeParam));
				return;
			}
		}

		TasksPage result = taskService.getTasks(authId, page, pageSize, type);

		GetTasksResponseDto response = new GetTasksResponseDto(
			new PaginationResponseDto(result.page(), result.pageSize(), result.totalPages()),
			result.tasks().stream().map(TaskPresentationMapper::toTaskResponse).toList());
		ctx.json(response);
	}

	private void registerTask(Context ctx) {
		String authId = requireAuthId(ctx);
		if (!ensureUserExists(ctx, authId)) {
			return;
		}

		TaskUpdateRequestDto body = ctx.bodyAsClass(TaskUpdateRequestDto.class);
		String title = body.title();
		UUID companyId = body.companyId();
		String typeValue = body.type();
		String statusValue = body.status();
		String creationSourceUrl = body.creationSourceUrl();

		if (!validateTitle(ctx, title)) {
			return;
		}

		if (companyId == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("companyId is required"));
			return;
		}

		TaskType type = parseTaskType(ctx, typeValue);
		if (type == null) {
			return;
		}

		TaskStatus status = parseTaskStatus(ctx, statusValue);
		if (status == null) {
			return;
		}

		if (!validateCreationSourceUrl(ctx, creationSourceUrl)) {
			return;
		}

		Optional<Task> task = taskService.registerTask(
			authId,
			title,
			companyId,
			type,
			status,
			creationSourceUrl,
			body.deadline());
		if (task.isEmpty()) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Company not found"));
			return;
		}

		ctx.status(HttpStatus.CREATED).json(TaskPresentationMapper.toTaskResponse(task.get()));
	}

	private void updateTask(Context ctx) {
		String authId = requireAuthId(ctx);
		if (!ensureUserExists(ctx, authId)) {
			return;
		}

		UUID taskId = parseTaskId(ctx);
		if (taskId == null) {
			return;
		}

		TaskUpdateRequestDto body = ctx.bodyAsClass(TaskUpdateRequestDto.class);
		String title = body.title();
		UUID companyId = body.companyId();
		String typeValue = body.type();
		String statusValue = body.status();
		String creationSourceUrl = body.creationSourceUrl();

		if (!validateTitle(ctx, title)) {
			return;
		}

		if (companyId == null) {
			ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseDto("companyId is required"));
			return;
		}

		TaskType type = parseTaskType(ctx, typeValue);
		if (type == null) {
			return;
		}

		TaskStatus status = parseTaskStatus(ctx, statusValue);
		if (status == null) {
			return;
		}

		if (!validateCreationSourceUrl(ctx, creationSourceUrl)) {
			return;
		}

		Optional<Task> updated = taskService.updateTask(
			authId,
			taskId,
			title,
			companyId,
			type,
			status,
			creationSourceUrl,
			body.deadline());
		if (updated.isEmpty()) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Task not found"));
			return;
		}

		ctx.json(TaskPresentationMapper.toTaskResponse(updated.get()));
	}

	private void deleteTask(Context ctx) {
		String authId = requireAuthId(ctx);
		if (!ensureUserExists(ctx, authId)) {
			return;
		}

		UUID taskId = parseTaskId(ctx);
		if (taskId == null) {
			return;
		}

		boolean deleted = taskService.deleteTask(authId, taskId);
		if (!deleted) {
			ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponseDto("Task not found"));
			return;
		}
		ctx.status(HttpStatus.NO_CONTENT);
	}
}

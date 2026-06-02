package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.domain.entity.Task;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskType;
import dev.shoheiyamagiwa.shukan.middleware.AuthenticatedRequestContext;
import dev.shoheiyamagiwa.shukan.presentation.ErrorResponses;
import dev.shoheiyamagiwa.shukan.presentation.dto.GetTasksResponseDto;
import dev.shoheiyamagiwa.shukan.presentation.dto.PaginationResponseDto;
import dev.shoheiyamagiwa.shukan.presentation.dto.TaskUpdateRequestDto;
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
	
	@Nullable
	private static UUID parseTaskId(Context ctx) {
		try {
			return UUID.fromString(ctx.pathParam("id"));
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid task ID");
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
	private static TaskType parseTaskType(Context ctx, @Nullable String value) {
		if (value == null) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "type is required");
			return null;
		}
		try {
			return TaskType.fromValue(value);
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid type: " + value);
			return null;
		}
	}
	
	@Nullable
	private static TaskStatus parseTaskStatus(Context ctx, @Nullable String value) {
		if (value == null) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "status is required");
			return null;
		}
		try {
			return TaskStatus.fromValue(value);
		} catch (IllegalArgumentException e) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid status: " + value);
			return null;
		}
	}
	
	private static boolean validateTitle(Context ctx, @Nullable String title) {
		if (title == null || title.length() < 2 || title.length() > 32) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "title must be between 2 and 32 characters");
			return false;
		}
		return true;
	}
	
	private static boolean validateCreationSourceUrl(Context ctx, @Nullable String creationSourceUrl) {
		if (creationSourceUrl != null
			&& (creationSourceUrl.length() < 7 || creationSourceUrl.length() > 2048)) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "creationSourceUrl must be between 7 and 2048 characters");
			return false;
		}
		return true;
	}
	
	private boolean ensureUserExists(Context ctx, String authId) {
		if (!taskService.userExists(authId)) {
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "User not found");
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
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "page must be between 0 and 99");
			return;
		}
		
		if (pageSize < 1 || pageSize > 100) {
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "pageSize must be between 1 and 100");
			return;
		}
		
		TaskType type = null;
		String typeParam = ctx.queryParam("type");
		if (typeParam != null) {
			try {
				type = TaskType.fromValue(typeParam);
			} catch (IllegalArgumentException e) {
				ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "Invalid type: " + typeParam);
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
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "companyId is required");
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
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "Company not found");
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
			ErrorResponses.respond(ctx, HttpStatus.BAD_REQUEST, "companyId is required");
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
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "Task not found");
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
			ErrorResponses.respond(ctx, HttpStatus.NOT_FOUND, "Task not found");
			return;
		}
		ctx.status(HttpStatus.NO_CONTENT);
	}
}

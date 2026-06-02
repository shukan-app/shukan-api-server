package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Task;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskType;
import dev.shoheiyamagiwa.shukan.infra.repository.TaskRepository;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TaskService {
	private final TaskRepository taskRepository;
	
	public TaskService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}
	
	public boolean userExists(String authId) {
		return taskRepository.userExists(authId);
	}
	
	public TasksPage getTasks(String authId, int page, int pageSize, @Nullable TaskType type) {
		List<Task> tasks = taskRepository.findAll(authId, page, pageSize, type);
		int total = taskRepository.count(authId, type);
		int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
		return new TasksPage(tasks, page, pageSize, totalPages);
	}
	
	public Optional<Task> findTask(String authId, UUID taskId) {
		return taskRepository.findById(authId, taskId);
	}
	
	public Optional<Task> registerTask(
		String authId,
		String title,
		UUID companyId,
		TaskType type,
		TaskStatus status,
		@Nullable String creationSourceUrl,
		@Nullable OffsetDateTime deadline) {
		return taskRepository.create(authId, title, companyId, type, status, creationSourceUrl, deadline);
	}
	
	public Optional<Task> updateTask(
		String authId,
		UUID taskId,
		String title,
		UUID companyId,
		TaskType type,
		TaskStatus status,
		@Nullable String creationSourceUrl,
		@Nullable OffsetDateTime deadline) {
		return taskRepository.update(authId, taskId, title, companyId, type, status, creationSourceUrl, deadline);
	}
	
	public boolean deleteTask(String authId, UUID taskId) {
		return taskRepository.softDelete(authId, taskId);
	}
}

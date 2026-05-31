package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Task;

import java.util.List;

public record TasksPage(List<Task> tasks, int page, int pageSize, int totalPages) {
}

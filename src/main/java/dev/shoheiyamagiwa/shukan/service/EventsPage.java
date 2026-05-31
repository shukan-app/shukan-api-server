package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Event;

import java.util.List;

public record EventsPage(List<Event> events, int page, int pageSize, int totalPages) {
}

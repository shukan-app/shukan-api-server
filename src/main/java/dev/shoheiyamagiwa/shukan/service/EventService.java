package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Event;
import dev.shoheiyamagiwa.shukan.domain.vo.EventFormatType;
import dev.shoheiyamagiwa.shukan.domain.vo.EventStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.EventType;
import dev.shoheiyamagiwa.shukan.infra.repository.EventRepository;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class EventService {
	private final EventRepository eventRepository;
	
	public EventService(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}
	
	public boolean userExists(String authId) {
		return eventRepository.userExists(authId);
	}
	
	public EventsPage getEvents(
		String authId,
		int page,
		int pageSize,
		@Nullable EventType type,
		@Nullable EventStatus status) {
		List<Event> events = eventRepository.findAll(authId, page, pageSize, type, status);
		int total = eventRepository.count(authId, type, status);
		int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
		return new EventsPage(events, page, pageSize, totalPages);
	}
	
	public Optional<Event> findEvent(String authId, UUID eventId) {
		return eventRepository.findById(authId, eventId);
	}
	
	public Optional<Event> registerEvent(
		String authId,
		String title,
		UUID companyId,
		EventType type,
		EventStatus status,
		EventFormatType formatType,
		@Nullable String location,
		@Nullable String creationSourceUrl,
		OffsetDateTime beginAt,
		OffsetDateTime endAt) {
		return eventRepository.create(
			authId,
			title,
			companyId,
			type,
			status,
			formatType,
			location,
			creationSourceUrl,
			beginAt,
			endAt);
	}
	
	public Optional<Event> updateEvent(
		String authId,
		UUID eventId,
		String title,
		UUID companyId,
		EventType type,
		EventStatus status,
		EventFormatType formatType,
		@Nullable String location,
		@Nullable String creationSourceUrl,
		OffsetDateTime beginAt,
		OffsetDateTime endAt) {
		return eventRepository.update(
			authId,
			eventId,
			title,
			companyId,
			type,
			status,
			formatType,
			location,
			creationSourceUrl,
			beginAt,
			endAt);
	}
	
	public boolean deleteEvent(String authId, UUID eventId) {
		return eventRepository.softDelete(authId, eventId);
	}
}

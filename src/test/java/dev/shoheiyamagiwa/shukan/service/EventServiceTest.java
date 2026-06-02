package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.entity.Event;
import dev.shoheiyamagiwa.shukan.domain.vo.*;
import dev.shoheiyamagiwa.shukan.infra.repository.CompanyRepository;
import dev.shoheiyamagiwa.shukan.infra.repository.EventRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public final class EventServiceTest {
	private static final String TEST_AUTH_ID = "event-service-test-firebase-uid";
	private static final OffsetDateTime BEGIN_AT = OffsetDateTime.parse("2026-07-20T10:00:00+09:00");
	private static final OffsetDateTime END_AT = OffsetDateTime.parse("2026-07-20T11:00:00+09:00");
	
	@Container
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");
	private static CompanyService companyService;
	private static EventService eventService;
	
	@BeforeAll
	static void setup() throws SQLException {
		Flyway.configure()
			.dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
			.load()
			.migrate();
		
		CompanyRepository companyRepository = new CompanyRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		EventRepository eventRepository = new EventRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		companyService = new CompanyService(companyRepository);
		eventService = new EventService(eventRepository);
		
		try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		     PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (auth_id) VALUES (?)")) {
			stmt.setString(1, TEST_AUTH_ID);
			stmt.executeUpdate();
		}
	}
	
	private static Optional<Event> createEvent(
		UUID companyId,
		String title,
		EventType type,
		EventStatus status) {
		return eventService.registerEvent(
			TEST_AUTH_ID,
			title,
			companyId,
			type,
			status,
			EventFormatType.ONLINE,
			null,
			null,
			BEGIN_AT,
			END_AT);
	}
	
	private static UUID createCompany(String name) {
		Optional<Company> company = companyService.registerCompany(
			TEST_AUTH_ID,
			name,
			"Engineer",
			CompanyStatus.BOOKMARKED,
			RecruitingPlatform.MYNAVI,
			ContactType.EMAIL,
			null);
		if (company.isEmpty()) {
			throw new IllegalStateException("Failed to create company for test");
		}
		return company.get().id();
	}
	
	@Test
	public void testRegisterEvent() {
		UUID companyId = createCompany("Event Register Corp");
		
		Optional<Event> event = eventService.registerEvent(
			TEST_AUTH_ID,
			"Company briefing",
			companyId,
			EventType.INFO_SESSION,
			EventStatus.SCHEDULED,
			EventFormatType.ONLINE,
			"Tokyo",
			"https://example.com/event",
			BEGIN_AT,
			END_AT);
		
		assertTrue(event.isPresent());
		Event registered = event.get();
		assertNotNull(registered.id());
		assertEquals("Company briefing", registered.title());
		assertEquals(EventType.INFO_SESSION, registered.type());
		assertEquals(EventStatus.SCHEDULED, registered.status());
		assertEquals(EventFormatType.ONLINE, registered.formatType());
		assertEquals(companyId, registered.company().id());
		assertEquals("Tokyo", registered.location());
		assertEquals("https://example.com/event", registered.creationSourceUrl());
		assertEquals(BEGIN_AT.toInstant(), registered.beginAt().toInstant());
		assertEquals(END_AT.toInstant(), registered.endAt().toInstant());
	}
	
	@Test
	public void testFindEvent() {
		UUID companyId = createCompany("Event Find Corp");
		Optional<Event> created = createEvent(companyId, "Find event", EventType.INTERVIEW, EventStatus.SCHEDULED);
		assertTrue(created.isPresent());
		
		Optional<Event> found = eventService.findEvent(TEST_AUTH_ID, created.get().id());
		
		assertTrue(found.isPresent());
		assertEquals(created.get().id(), found.get().id());
		assertEquals("Find event", found.get().title());
	}
	
	@Test
	public void testGetEventsWithFilters() {
		UUID companyId = createCompany("Event Filter Corp");
		createEvent(companyId, "Assessment event", EventType.ASSESSMENT, EventStatus.ATTENDED);
		createEvent(companyId, "Interview event", EventType.INTERVIEW, EventStatus.SCHEDULED);
		
		EventsPage page = eventService.getEvents(TEST_AUTH_ID, 0, 50, EventType.ASSESSMENT, EventStatus.ATTENDED);
		
		List<Event> filtered = page.events();
		assertFalse(filtered.isEmpty());
		assertTrue(filtered.stream().allMatch(event -> event.type() == EventType.ASSESSMENT));
		assertTrue(filtered.stream().allMatch(event -> event.status() == EventStatus.ATTENDED));
	}
	
	@Test
	public void testGetEventsSortByBeginAtAscending() {
		UUID companyId = createCompany("Event Sort Corp");
		Optional<Event> later = eventService.registerEvent(
			TEST_AUTH_ID,
			"Later event",
			companyId,
			EventType.OTHER,
			EventStatus.SCHEDULED,
			EventFormatType.OFFLINE,
			null,
			null,
			OffsetDateTime.parse("2026-09-10T10:00:00+09:00"),
			OffsetDateTime.parse("2026-09-10T11:00:00+09:00"));
		Optional<Event> earlier = eventService.registerEvent(
			TEST_AUTH_ID,
			"Earlier event",
			companyId,
			EventType.OTHER,
			EventStatus.SCHEDULED,
			EventFormatType.OFFLINE,
			null,
			null,
			OffsetDateTime.parse("2026-08-10T10:00:00+09:00"),
			OffsetDateTime.parse("2026-08-10T11:00:00+09:00"));
		assertTrue(later.isPresent());
		assertTrue(earlier.isPresent());
		
		EventsPage page = eventService.getEvents(TEST_AUTH_ID, 0, 100, null, null);
		List<UUID> ids = page.events().stream().map(Event::id).toList();
		
		int earlierIndex = ids.indexOf(earlier.get().id());
		int laterIndex = ids.indexOf(later.get().id());
		assertTrue(earlierIndex >= 0);
		assertTrue(laterIndex >= 0);
		assertTrue(earlierIndex < laterIndex);
	}
	
	@Test
	public void testUpdateEvent() {
		UUID companyId = createCompany("Event Update Corp");
		UUID newCompanyId = createCompany("Event Update Target Corp");
		Optional<Event> created = createEvent(companyId, "Update target", EventType.INTERVIEW, EventStatus.SCHEDULED);
		assertTrue(created.isPresent());
		
		Optional<Event> updated = eventService.updateEvent(
			TEST_AUTH_ID,
			created.get().id(),
			"Updated event",
			newCompanyId,
			EventType.GROUP_WORK,
			EventStatus.RESCHEDULING_REQUIRED,
			EventFormatType.HYBRID,
			"Osaka",
			"https://example.com/updated",
			OffsetDateTime.parse("2026-10-10T12:00:00+09:00"),
			OffsetDateTime.parse("2026-10-10T13:00:00+09:00"));
		
		assertTrue(updated.isPresent());
		assertEquals("Updated event", updated.get().title());
		assertEquals(newCompanyId, updated.get().company().id());
		assertEquals(EventType.GROUP_WORK, updated.get().type());
		assertEquals(EventStatus.RESCHEDULING_REQUIRED, updated.get().status());
		assertEquals(EventFormatType.HYBRID, updated.get().formatType());
		assertEquals("Osaka", updated.get().location());
	}
	
	@Test
	public void testDeleteEvent() {
		UUID companyId = createCompany("Event Delete Corp");
		Optional<Event> created = createEvent(companyId, "Delete target", EventType.OTHER, EventStatus.SCHEDULED);
		assertTrue(created.isPresent());
		
		boolean deleted = eventService.deleteEvent(TEST_AUTH_ID, created.get().id());
		
		assertTrue(deleted);
		assertFalse(eventService.findEvent(TEST_AUTH_ID, created.get().id()).isPresent());
	}
	
	@Test
	public void testDeleteEventNotFound() {
		boolean deleted = eventService.deleteEvent(TEST_AUTH_ID, UUID.randomUUID());
		
		assertFalse(deleted);
	}
}

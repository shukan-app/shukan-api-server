package dev.shoheiyamagiwa.shukan.presentation.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.entity.Event;
import dev.shoheiyamagiwa.shukan.domain.vo.*;
import dev.shoheiyamagiwa.shukan.presentation.dto.EventResponseDto;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class EventPresentationMapperTest {
	private static final UUID ID = UUID.randomUUID();
	private static final UUID COMPANY_ID = UUID.randomUUID();
	private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-07-01T09:00:00+09:00");
	private static final OffsetDateTime BEGIN_AT = OffsetDateTime.parse("2026-07-20T10:00:00+09:00");
	private static final OffsetDateTime END_AT = OffsetDateTime.parse("2026-07-20T11:00:00+09:00");
	
	@Test
	public void testToEventResponse() {
		Company company = new Company(
			COMPANY_ID,
			"Corp A",
			"Engineer",
			CompanyStatus.PREENTRY,
			RecruitingPlatform.ONE_CAREER,
			ContactType.PLATFORM_MESSAGE,
			null,
			CREATED_AT,
			CREATED_AT,
			null);
		Event event = new Event(
			ID,
			"Company briefing",
			company,
			EventType.GROUP_WORK,
			EventStatus.RESCHEDULING_REQUIRED,
			EventFormatType.ON_DEMAND,
			null,
			"https://example.com/event",
			BEGIN_AT,
			END_AT,
			CREATED_AT,
			CREATED_AT,
			null);
		
		EventResponseDto dto = EventPresentationMapper.toEventResponse(event);
		
		assertEquals(ID, dto.id());
		assertEquals("Company briefing", dto.title());
		assertEquals(COMPANY_ID, dto.company().id());
		assertEquals("groupWork", dto.type());
		assertEquals("reschedulingRequired", dto.status());
		assertEquals("onDemand", dto.formatType());
		assertNull(dto.location());
		assertEquals("https://example.com/event", dto.creationSourceUrl());
		assertEquals(BEGIN_AT, dto.beginAt());
		assertEquals(END_AT, dto.endAt());
		assertNull(dto.deletedAt());
	}
}

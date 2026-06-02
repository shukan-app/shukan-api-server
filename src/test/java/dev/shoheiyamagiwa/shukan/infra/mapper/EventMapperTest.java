package dev.shoheiyamagiwa.shukan.infra.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Event;
import dev.shoheiyamagiwa.shukan.domain.vo.*;
import dev.shoheiyamagiwa.shukan.infra.dto.EventDto;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class EventMapperTest {
	private static final UUID ID = UUID.randomUUID();
	private static final UUID COMPANY_ID = UUID.randomUUID();
	private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-07-01T09:00:00+09:00");
	private static final OffsetDateTime BEGIN_AT = OffsetDateTime.parse("2026-07-20T10:00:00+09:00");
	private static final OffsetDateTime END_AT = OffsetDateTime.parse("2026-07-20T11:00:00+09:00");
	
	@Test
	public void testToEntity() {
		EventDto dto = new EventDto(
			ID,
			"Company briefing",
			COMPANY_ID,
			"Corp A",
			"Engineer",
			"informal_contact",
			"one_career",
			"platform_message",
			"https://example.com/company",
			CREATED_AT,
			CREATED_AT,
			null,
			"info_session",
			"scheduled",
			"on_demand",
			null,
			"https://example.com/event",
			BEGIN_AT,
			END_AT,
			CREATED_AT,
			CREATED_AT,
			null);
		
		Event entity = EventMapper.toEntity(dto);
		
		assertEquals(ID, entity.id());
		assertEquals("Company briefing", entity.title());
		assertEquals(COMPANY_ID, entity.company().id());
		assertEquals(CompanyStatus.INFORMAL_CONTACT, entity.company().status());
		assertEquals(RecruitingPlatform.ONE_CAREER, entity.company().applicationRoute());
		assertEquals(ContactType.PLATFORM_MESSAGE, entity.company().contactType());
		assertEquals(EventType.INFO_SESSION, entity.type());
		assertEquals(EventStatus.SCHEDULED, entity.status());
		assertEquals(EventFormatType.ON_DEMAND, entity.formatType());
		assertNull(entity.location());
		assertEquals("https://example.com/event", entity.creationSourceUrl());
		assertEquals(BEGIN_AT, entity.beginAt());
		assertEquals(END_AT, entity.endAt());
		assertNull(entity.deletedAt());
	}
}

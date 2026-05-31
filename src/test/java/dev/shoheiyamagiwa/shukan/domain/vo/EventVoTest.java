package dev.shoheiyamagiwa.shukan.domain.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class EventVoTest {
	@Test
	public void testEventTypeMapping() {
		assertEquals(EventType.INFO_SESSION, EventType.fromValue("infoSession"));
		assertEquals(EventType.INFO_SESSION, EventType.fromDatabaseValue("info_session"));
		assertEquals("offerEvent", EventType.OFFER_EVENT.getValue());
		assertEquals("offer_event", EventType.OFFER_EVENT.toDatabaseValue());
	}

	@Test
	public void testEventStatusMapping() {
		assertEquals(EventStatus.RESCHEDULING_REQUIRED, EventStatus.fromValue("reschedulingRequired"));
		assertEquals(EventStatus.RESCHEDULING_REQUIRED, EventStatus.fromDatabaseValue("rescheduling_required"));
		assertEquals("canceled", EventStatus.CANCELED.getValue());
		assertEquals("canceled", EventStatus.CANCELED.toDatabaseValue());
	}

	@Test
	public void testEventFormatTypeMapping() {
		assertEquals(EventFormatType.ON_DEMAND, EventFormatType.fromValue("onDemand"));
		assertEquals(EventFormatType.ON_DEMAND, EventFormatType.fromDatabaseValue("on_demand"));
		assertEquals("hybrid", EventFormatType.HYBRID.getValue());
		assertEquals("hybrid", EventFormatType.HYBRID.toDatabaseValue());
	}

	@Test
	public void testInvalidValuesThrow() {
		assertThrows(IllegalArgumentException.class, () -> EventType.fromValue("invalid"));
		assertThrows(IllegalArgumentException.class, () -> EventStatus.fromDatabaseValue("invalid"));
		assertThrows(IllegalArgumentException.class, () -> EventFormatType.fromValue("invalid"));
	}
}

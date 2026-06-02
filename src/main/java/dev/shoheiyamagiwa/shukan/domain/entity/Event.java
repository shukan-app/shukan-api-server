package dev.shoheiyamagiwa.shukan.domain.entity;

import dev.shoheiyamagiwa.shukan.domain.vo.EventFormatType;
import dev.shoheiyamagiwa.shukan.domain.vo.EventStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.EventType;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Event(
	UUID id,
	String title,
	Company company,
	EventType type,
	EventStatus status,
	EventFormatType formatType,
	@Nullable String location,
	@Nullable String creationSourceUrl,
	OffsetDateTime beginAt,
	OffsetDateTime endAt,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	@Nullable OffsetDateTime deletedAt) {
}

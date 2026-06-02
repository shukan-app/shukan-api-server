package dev.shoheiyamagiwa.shukan.presentation.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Event;
import dev.shoheiyamagiwa.shukan.presentation.dto.EventResponseDto;

public final class EventPresentationMapper {
	
	public static EventResponseDto toEventResponse(Event event) {
		return new EventResponseDto(
			event.id(),
			event.title(),
			CompanyPresentationMapper.toCompanyResponse(event.company()),
			event.type().getValue(),
			event.status().getValue(),
			event.formatType().getValue(),
			event.location(),
			event.creationSourceUrl(),
			event.beginAt(),
			event.endAt(),
			event.createdAt(),
			event.updatedAt(),
			event.deletedAt());
	}
}

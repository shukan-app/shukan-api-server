package dev.shoheiyamagiwa.shukan.presentation.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;
import dev.shoheiyamagiwa.shukan.presentation.dto.ScoutResponseDto;

public final class ScoutPresentationMapper {
	
	public static ScoutResponseDto toScoutResponse(Scout scout) {
		return new ScoutResponseDto(
			scout.id(),
			scout.title(),
			scout.companyName(),
			scout.status().getValue(),
			scout.platform().getValue(),
			scout.detailsUrl(),
			scout.creationSourceUrl(),
			scout.createdAt(),
			scout.updatedAt(),
			scout.deletedAt());
	}
}

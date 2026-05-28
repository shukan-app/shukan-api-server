package dev.shoheiyamagiwa.shukan.domain.entity;

import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecrutingPlatform;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Company(
		UUID id,
		String name,
		String appliedRole,
		CompanyStatus status,
		RecrutingPlatform applicationRoute,
		ContactType contactType,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		@Nullable OffsetDateTime deletedAt) {
}

package dev.shoheiyamagiwa.shukan.domain.entity;

import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Company(
	UUID id,
	String name,
	String appliedRole,
	CompanyStatus status,
	RecruitingPlatform applicationRoute,
	ContactType contactType,
	@Nullable String creationSourceUrl,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	@Nullable OffsetDateTime deletedAt) {
}

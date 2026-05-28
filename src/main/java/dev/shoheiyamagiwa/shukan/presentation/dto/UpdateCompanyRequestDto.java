package dev.shoheiyamagiwa.shukan.presentation.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record UpdateCompanyRequestDto(
    @Nullable UUID id,
    @Nullable String name,
    @Nullable String appliedRole,
    @Nullable String status,
    @Nullable String applicationRoute,
    @Nullable String contactType,
    @Nullable OffsetDateTime createdAt,
    @Nullable OffsetDateTime updatedAt,
    @Nullable OffsetDateTime deletedAt) {}

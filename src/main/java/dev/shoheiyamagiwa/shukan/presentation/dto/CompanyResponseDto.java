package dev.shoheiyamagiwa.shukan.presentation.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record CompanyResponseDto(
    UUID id,
    String name,
    String appliedRole,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    @Nullable OffsetDateTime deletedAt) {}

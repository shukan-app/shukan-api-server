package dev.shoheiyamagiwa.shukan.infra.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record CompanyDto(
    UUID id,
    String name,
    String appliedRole,
    String status,
    String applicationRoute,
    String contactType,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    @Nullable OffsetDateTime deletedAt) {}

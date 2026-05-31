package dev.shoheiyamagiwa.shukan.presentation.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ScoutResponseDto(
    UUID id,
    String title,
    String companyName,
    String status,
    String platform,
    @Nullable String detailsUrl,
    @Nullable String creationSourceUrl,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    @Nullable OffsetDateTime deletedAt) {}

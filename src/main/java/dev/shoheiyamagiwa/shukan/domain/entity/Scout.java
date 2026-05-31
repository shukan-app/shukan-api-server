package dev.shoheiyamagiwa.shukan.domain.entity;

import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.ScoutStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record Scout(
    UUID id,
    String title,
    String companyName,
    ScoutStatus status,
    RecruitingPlatform platform,
    @Nullable String detailsUrl,
    @Nullable String creationSourceUrl,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    @Nullable OffsetDateTime deletedAt) {}

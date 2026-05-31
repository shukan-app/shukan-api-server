package dev.shoheiyamagiwa.shukan.infra.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.ScoutStatus;
import dev.shoheiyamagiwa.shukan.infra.dto.ScoutDto;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public final class ScoutMapperTest {

  private static final UUID ID = UUID.randomUUID();
  private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-05-31T10:00:00+09:00");

  @Test
  public void testToEntity() {
    ScoutDto dto =
        new ScoutDto(
            ID,
            "Scout title",
            "Scout Corp",
            "unresponded",
            "one_career",
            "https://example.com/details",
            "https://example.com/source",
            NOW,
            NOW,
            null);

    Scout entity = ScoutMapper.toEntity(dto);

    assertEquals(ID, entity.id());
    assertEquals("Scout title", entity.title());
    assertEquals("Scout Corp", entity.companyName());
    assertEquals(ScoutStatus.UNRESPONDED, entity.status());
    assertEquals(RecruitingPlatform.ONE_CAREER, entity.platform());
    assertEquals("https://example.com/details", entity.detailsUrl());
    assertEquals("https://example.com/source", entity.creationSourceUrl());
    assertEquals(NOW, entity.createdAt());
    assertEquals(NOW, entity.updatedAt());
    assertNull(entity.deletedAt());
  }
}

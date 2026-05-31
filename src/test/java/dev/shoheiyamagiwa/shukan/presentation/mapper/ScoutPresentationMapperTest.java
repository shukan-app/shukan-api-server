package dev.shoheiyamagiwa.shukan.presentation.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.ScoutStatus;
import dev.shoheiyamagiwa.shukan.presentation.dto.ScoutResponseDto;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public final class ScoutPresentationMapperTest {

  private static final UUID ID = UUID.randomUUID();
  private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-05-31T10:00:00+09:00");

  @Test
  public void testToScoutResponse() {
    Scout scout =
        new Scout(
            ID,
            "Scout title",
            "Scout Corp",
            ScoutStatus.ACCEPTED,
            RecruitingPlatform.OFFER_BOX,
            "https://example.com/details",
            null,
            NOW,
            NOW,
            null);

    ScoutResponseDto dto = ScoutPresentationMapper.toScoutResponse(scout);

    assertEquals(ID, dto.id());
    assertEquals("Scout title", dto.title());
    assertEquals("Scout Corp", dto.companyName());
    assertEquals("accepted", dto.status());
    assertEquals("offerBox", dto.platform());
    assertEquals("https://example.com/details", dto.detailsUrl());
    assertNull(dto.creationSourceUrl());
    assertEquals(NOW, dto.createdAt());
    assertEquals(NOW, dto.updatedAt());
    assertNull(dto.deletedAt());
  }
}

package dev.shoheiyamagiwa.shukan.domain.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public final class ScoutVoTest {

  @Test
  public void testScoutStatusFromValue() {
    assertEquals(ScoutStatus.UNREAD, ScoutStatus.fromValue("unread"));
    assertEquals(ScoutStatus.UNRESPONDED, ScoutStatus.fromValue("unresponded"));
    assertEquals(ScoutStatus.ACCEPTED, ScoutStatus.fromValue("accepted"));
    assertEquals(ScoutStatus.DECLINED, ScoutStatus.fromValue("declined"));
  }

  @Test
  public void testScoutStatusFromValueThrowsOnUnknown() {
    assertThrows(IllegalArgumentException.class, () -> ScoutStatus.fromValue("unknown"));
  }

  @Test
  public void testScoutStatusGetValue() {
    assertEquals("unread", ScoutStatus.UNREAD.getValue());
    assertEquals("unresponded", ScoutStatus.UNRESPONDED.getValue());
  }

  @Test
  public void testScoutStatusFromDatabaseValueAndToDatabaseValue() {
    assertEquals(ScoutStatus.ACCEPTED, ScoutStatus.fromDatabaseValue("accepted"));
    assertEquals("declined", ScoutStatus.DECLINED.toDatabaseValue());
  }
}

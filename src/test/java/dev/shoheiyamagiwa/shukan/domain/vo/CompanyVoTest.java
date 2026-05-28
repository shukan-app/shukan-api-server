package dev.shoheiyamagiwa.shukan.domain.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public final class CompanyVoTest {

  @Test
  public void testCompanyStatusFromValue() {
    assertEquals(CompanyStatus.BOOKMARKED, CompanyStatus.fromValue("bookmarked"));
    assertEquals(CompanyStatus.PREENTRY, CompanyStatus.fromValue("preentry"));
    assertEquals(CompanyStatus.REJECTED, CompanyStatus.fromValue("rejected"));
  }

  @Test
  public void testCompanyStatusFromValueThrowsOnUnknown() {
    assertThrows(IllegalArgumentException.class, () -> CompanyStatus.fromValue("unknown"));
  }

  @Test
  public void testCompanyStatusGetValue() {
    assertEquals("bookmarked", CompanyStatus.BOOKMARKED.getValue());
    assertEquals("informalContact", CompanyStatus.INFORMAL_CONTACT.getValue());
    assertEquals("offerAccepted", CompanyStatus.OFFER_ACCEPTED.getValue());
  }

  @Test
  public void testContactTypeFromValue() {
    assertEquals(ContactType.EMAIL, ContactType.fromValue("email"));
    assertEquals(ContactType.PLATFORM_MESSAGE, ContactType.fromValue("platformMessage"));
    assertEquals(ContactType.OTHER, ContactType.fromValue("other"));
  }

  @Test
  public void testContactTypeFromValueThrowsOnUnknown() {
    assertThrows(IllegalArgumentException.class, () -> ContactType.fromValue("unknown"));
  }

  @Test
  public void testContactTypeGetValue() {
    assertEquals("email", ContactType.EMAIL.getValue());
    assertEquals("platformMessage", ContactType.PLATFORM_MESSAGE.getValue());
  }

  @Test
  public void testRecrutingPlatformFromValue() {
    assertEquals(RecrutingPlatform.MYNAVI, RecrutingPlatform.fromValue("mynavi"));
    assertEquals(RecrutingPlatform.ONE_CAREER, RecrutingPlatform.fromValue("oneCareer"));
    assertEquals(RecrutingPlatform.OTHER, RecrutingPlatform.fromValue("other"));
  }

  @Test
  public void testRecrutingPlatformFromValueThrowsOnUnknown() {
    assertThrows(IllegalArgumentException.class, () -> RecrutingPlatform.fromValue("unknown"));
  }

  @Test
  public void testRecrutingPlatformGetValue() {
    assertEquals("mynavi", RecrutingPlatform.MYNAVI.getValue());
    assertEquals("oneCareer", RecrutingPlatform.ONE_CAREER.getValue());
  }
}

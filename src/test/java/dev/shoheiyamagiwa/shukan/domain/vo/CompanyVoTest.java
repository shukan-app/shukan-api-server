package dev.shoheiyamagiwa.shukan.domain.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
	public void testCompanyStatusFromDatabaseValueAndToDatabaseValue() {
		assertEquals(CompanyStatus.INFORMAL_CONTACT, CompanyStatus.fromDatabaseValue("informal_contact"));
		assertEquals("offer_accepted", CompanyStatus.OFFER_ACCEPTED.toDatabaseValue());
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
	public void testContactTypeFromDatabaseValueAndToDatabaseValue() {
		assertEquals(ContactType.PLATFORM_MESSAGE, ContactType.fromDatabaseValue("platform_message"));
		assertEquals("contact_form", ContactType.CONTACT_FORM.toDatabaseValue());
	}
	
	@Test
	public void testRecruitingPlatformFromValue() {
		assertEquals(RecruitingPlatform.MYNAVI, RecruitingPlatform.fromValue("mynavi"));
		assertEquals(RecruitingPlatform.ONE_CAREER, RecruitingPlatform.fromValue("oneCareer"));
		assertEquals(RecruitingPlatform.OTHER, RecruitingPlatform.fromValue("other"));
	}
	
	@Test
	public void testRecruitingPlatformFromValueThrowsOnUnknown() {
		assertThrows(IllegalArgumentException.class, () -> RecruitingPlatform.fromValue("unknown"));
	}
	
	@Test
	public void testRecruitingPlatformGetValue() {
		assertEquals("mynavi", RecruitingPlatform.MYNAVI.getValue());
		assertEquals("oneCareer", RecruitingPlatform.ONE_CAREER.getValue());
	}

	@Test
	public void testRecruitingPlatformFromDatabaseValueAndToDatabaseValue() {
		assertEquals(RecruitingPlatform.ONE_CAREER, RecruitingPlatform.fromDatabaseValue("one_career"));
		assertEquals("track_job", RecruitingPlatform.TRACK_JOB.toDatabaseValue());
	}

	@Test
	public void testCompanyStatusFromDatabaseValueThrowsOnUnknown() {
		assertThrows(IllegalArgumentException.class, () -> CompanyStatus.fromDatabaseValue("unknown"));
	}

	@Test
	public void testCompanyStatusAllEnumValuesDatabaseRoundTrip() {
		for (CompanyStatus status : CompanyStatus.values()) {
			assertEquals(status, CompanyStatus.fromDatabaseValue(status.toDatabaseValue()));
		}
	}

	@Test
	public void testCompanyStatusAllEnumValuesApiRoundTrip() {
		for (CompanyStatus status : CompanyStatus.values()) {
			assertEquals(status, CompanyStatus.fromValue(status.getValue()));
		}
	}

	@Test
	public void testContactTypeFromDatabaseValueThrowsOnUnknown() {
		assertThrows(IllegalArgumentException.class, () -> ContactType.fromDatabaseValue("unknown"));
	}

	@Test
	public void testRecruitingPlatformFromDatabaseValueThrowsOnUnknown() {
		assertThrows(
				IllegalArgumentException.class, () -> RecruitingPlatform.fromDatabaseValue("unknown"));
	}
}

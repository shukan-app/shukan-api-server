package dev.shoheiyamagiwa.shukan.infra.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.infra.dto.CompanyDto;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class CompanyMapperTest {
	
	private static final UUID ID = UUID.randomUUID();
	private static final OffsetDateTime NOW = OffsetDateTime.now();
	
	@Test
	public void testToEntity() {
		CompanyDto dto =
			new CompanyDto(
				ID,
				"Corp A",
				"Engineer",
				"informal_contact",
				"one_career",
				"platform_message",
				"https://example.com/source",
				NOW,
				NOW,
				null);
		
		Company entity = CompanyMapper.toEntity(dto);
		
		assertEquals(ID, entity.id());
		assertEquals("Corp A", entity.name());
		assertEquals("Engineer", entity.appliedRole());
		assertEquals(CompanyStatus.INFORMAL_CONTACT, entity.status());
		assertEquals(RecruitingPlatform.ONE_CAREER, entity.applicationRoute());
		assertEquals(ContactType.PLATFORM_MESSAGE, entity.contactType());
		assertEquals("https://example.com/source", entity.creationSourceUrl());
		assertEquals(NOW, entity.createdAt());
		assertEquals(NOW, entity.updatedAt());
		assertNull(entity.deletedAt());
	}
	
	@Test
	public void testToDto() {
		Company entity =
			new Company(
				ID,
				"Corp A",
				"Engineer",
				CompanyStatus.INTERVIEW_IN_PROGRESS,
				RecruitingPlatform.TRACK_JOB,
				ContactType.CONTACT_FORM,
				"https://example.com/source",
				NOW,
				NOW,
				null);
		
		CompanyDto dto = CompanyMapper.toDto(entity);
		
		assertEquals(ID, dto.id());
		assertEquals("Corp A", dto.name());
		assertEquals("Engineer", dto.appliedRole());
		assertEquals("interview_in_progress", dto.status());
		assertEquals("track_job", dto.applicationRoute());
		assertEquals("contact_form", dto.contactType());
		assertEquals("https://example.com/source", dto.creationSourceUrl());
		assertEquals(NOW, dto.createdAt());
		assertEquals(NOW, dto.updatedAt());
		assertNull(dto.deletedAt());
	}
}

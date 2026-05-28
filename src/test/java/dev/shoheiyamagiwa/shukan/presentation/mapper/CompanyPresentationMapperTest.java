package dev.shoheiyamagiwa.shukan.presentation.mapper;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecrutingPlatform;
import dev.shoheiyamagiwa.shukan.presentation.dto.CompanyDetailResponseDto;
import dev.shoheiyamagiwa.shukan.presentation.dto.CompanyResponseDto;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class CompanyPresentationMapperTest {
	
	private static final UUID ID = UUID.randomUUID();
	private static final OffsetDateTime NOW = OffsetDateTime.now();
	
	private static Company buildCompany() {
		return new Company(
				ID,
				"Corp A",
				"Engineer",
				CompanyStatus.PREENTRY,
				RecrutingPlatform.ONE_CAREER,
				ContactType.PLATFORM_MESSAGE,
				NOW,
				NOW,
				null);
	}
	
	@Test
	public void testToCompanyResponse() {
		CompanyResponseDto dto = CompanyPresentationMapper.toCompanyResponse(buildCompany());
		
		assertEquals(ID, dto.id());
		assertEquals("Corp A", dto.name());
		assertEquals("Engineer", dto.appliedRole());
		assertEquals("preentry", dto.status());
		assertEquals(NOW, dto.createdAt());
		assertEquals(NOW, dto.updatedAt());
		assertNull(dto.deletedAt());
	}
	
	@Test
	public void testToCompanyDetailResponse() {
		CompanyDetailResponseDto dto =
				CompanyPresentationMapper.toCompanyDetailResponse(buildCompany());
		
		assertEquals(ID, dto.id());
		assertEquals("Corp A", dto.name());
		assertEquals("Engineer", dto.appliedRole());
		assertEquals("preentry", dto.status());
		assertEquals("oneCareer", dto.applicationRoute());
		assertEquals("platformMessage", dto.contactType());
		assertEquals(NOW, dto.createdAt());
		assertEquals(NOW, dto.updatedAt());
		assertNull(dto.deletedAt());
	}
}

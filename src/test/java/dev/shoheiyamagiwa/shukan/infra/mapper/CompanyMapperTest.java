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
				new CompanyDto(ID, "Corp A", "Engineer", "bookmarked", "mynavi", "email", NOW, NOW, null);
		
		Company entity = CompanyMapper.toEntity(dto);
		
		assertEquals(ID, entity.id());
		assertEquals("Corp A", entity.name());
		assertEquals("Engineer", entity.appliedRole());
		assertEquals(CompanyStatus.BOOKMARKED, entity.status());
		assertEquals(RecruitingPlatform.MYNAVI, entity.applicationRoute());
		assertEquals(ContactType.EMAIL, entity.contactType());
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
						CompanyStatus.BOOKMARKED,
						RecruitingPlatform.MYNAVI,
						ContactType.EMAIL,
						NOW,
						NOW,
						null);
		
		CompanyDto dto = CompanyMapper.toDto(entity);
		
		assertEquals(ID, dto.id());
		assertEquals("Corp A", dto.name());
		assertEquals("Engineer", dto.appliedRole());
		assertEquals("bookmarked", dto.status());
		assertEquals("mynavi", dto.applicationRoute());
		assertEquals("email", dto.contactType());
		assertEquals(NOW, dto.createdAt());
		assertEquals(NOW, dto.updatedAt());
		assertNull(dto.deletedAt());
	}
}

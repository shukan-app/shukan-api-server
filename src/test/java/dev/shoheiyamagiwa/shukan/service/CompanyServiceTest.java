package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.infra.repository.CompanyRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public final class CompanyServiceTest {
	private static final String TEST_AUTH_ID = "test-firebase-uid";
	@Container
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");
	private static CompanyService companyService;
	
	@BeforeAll
	static void setUp() throws SQLException {
		Flyway.configure()
				.dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
				.load()
				.migrate();
		
		CompanyRepository repository =
				new CompanyRepository(
						postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		companyService = new CompanyService(repository);
		
		try (Connection conn =
					 DriverManager.getConnection(
							 postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		     PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (auth_id) VALUES (?)")) {
			stmt.setString(1, TEST_AUTH_ID);
			stmt.executeUpdate();
		}
	}
	
	@Test
	public void testRegisterCompany() {
		Optional<Company> company =
				companyService.registerCompany(
						TEST_AUTH_ID,
						"Test Corp",
						"Software Engineer",
						CompanyStatus.BOOKMARKED,
						RecruitingPlatform.MYNAVI,
						ContactType.EMAIL);

		assertTrue(company.isPresent());
		Company registered = company.get();
		
		assertNotNull(registered.id());
		assertEquals("Test Corp", registered.name());
		assertEquals("Software Engineer", registered.appliedRole());
		assertEquals(CompanyStatus.BOOKMARKED, registered.status());
		assertEquals(RecruitingPlatform.MYNAVI, registered.applicationRoute());
		assertEquals(ContactType.EMAIL, registered.contactType());
		assertNotNull(registered.createdAt());
		assertNotNull(registered.updatedAt());
	}
	
	@Test
	public void testFindCompany() {
		Optional<Company> created =
				companyService.registerCompany(
						TEST_AUTH_ID,
						"Find Corp",
						"Designer",
						CompanyStatus.PREENTRY,
						RecruitingPlatform.OTHER,
						ContactType.OTHER);
		assertTrue(created.isPresent());
		
		Optional<Company> found = companyService.findCompany(TEST_AUTH_ID, created.get().id());
		
		assertTrue(found.isPresent());
		assertEquals(created.get().id(), found.get().id());
		assertEquals("Find Corp", found.get().name());
	}
	
	@Test
	public void testFindCompanyNotFound() {
		Optional<Company> found = companyService.findCompany(TEST_AUTH_ID, java.util.UUID.randomUUID());
		
		assertFalse(found.isPresent());
	}
	
	@Test
	public void testGetCompanies() {
		companyService.registerCompany(
				TEST_AUTH_ID,
				"List Corp",
				"Manager",
				CompanyStatus.UNDER_SCREENING,
				RecruitingPlatform.WANTEDLY,
				ContactType.LINE);
		
		CompaniesPage page = companyService.getCompanies(TEST_AUTH_ID, 0, 50, null, null, null, null);
		
		assertNotNull(page);
		assertFalse(page.companies().isEmpty());
		assertEquals(0, page.page());
		assertEquals(50, page.pageSize());
	}
	
	@Test
	public void testGetCompaniesWithStatusFilter() {
		companyService.registerCompany(
				TEST_AUTH_ID,
				"Filtered Corp",
				"Analyst",
				CompanyStatus.OFFER_RECEIVED,
				RecruitingPlatform.PAIZA,
				ContactType.PHONE);
		
		CompaniesPage page =
				companyService.getCompanies(
						TEST_AUTH_ID, 0, 50, null, CompanyStatus.OFFER_RECEIVED, null, null);
		
		List<Company> filtered = page.companies();
		assertTrue(filtered.stream().allMatch(c -> c.status() == CompanyStatus.OFFER_RECEIVED));
	}
	
	@Test
	public void testUpdateCompany() {
		Optional<Company> created =
				companyService.registerCompany(
						TEST_AUTH_ID,
						"Update Corp",
						"Engineer",
						CompanyStatus.BOOKMARKED,
						RecruitingPlatform.RIKUNABI,
						ContactType.EMAIL);
		assertTrue(created.isPresent());
		
		Optional<Company> updated =
				companyService.updateCompany(
						TEST_AUTH_ID,
						created.get().id(),
						"Updated Corp",
						"Senior Engineer",
						CompanyStatus.PREENTRY,
						RecruitingPlatform.AGENT,
						ContactType.OTHER);
		
		assertTrue(updated.isPresent());
		assertEquals("Updated Corp", updated.get().name());
		assertEquals("Senior Engineer", updated.get().appliedRole());
		assertEquals(CompanyStatus.PREENTRY, updated.get().status());
	}
	
	@Test
	public void testDeleteCompany() {
		Optional<Company> created =
				companyService.registerCompany(
						TEST_AUTH_ID,
						"Delete Corp",
						"Intern",
						CompanyStatus.REJECTED,
						RecruitingPlatform.OTHER,
						ContactType.OTHER);
		assertTrue(created.isPresent());
		
		boolean deleted = companyService.deleteCompany(TEST_AUTH_ID, created.get().id());
		
		assertTrue(deleted);
		assertFalse(companyService.findCompany(TEST_AUTH_ID, created.get().id()).isPresent());
	}
	
	@Test
	public void testDeleteCompanyNotFound() {
		boolean deleted = companyService.deleteCompany(TEST_AUTH_ID, java.util.UUID.randomUUID());
		
		assertFalse(deleted);
	}
}

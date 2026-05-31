package dev.shoheiyamagiwa.shukan.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.ScoutStatus;
import dev.shoheiyamagiwa.shukan.infra.repository.ScoutRepository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
public final class ScoutServiceTest {
  private static final String TEST_AUTH_ID = "scout-service-test-firebase-uid";

  @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");
  private static ScoutService scoutService;

  @BeforeAll
  static void setup() throws SQLException {
    Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .load()
        .migrate();

    ScoutRepository scoutRepository =
        new ScoutRepository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    scoutService = new ScoutService(scoutRepository);

    try (Connection conn =
            DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (auth_id) VALUES (?)")) {
      stmt.setString(1, TEST_AUTH_ID);
      stmt.executeUpdate();
    }
  }

  @Test
  public void testRegisterScout() {
    Optional<Scout> scout =
        createScout("Register scout", ScoutStatus.UNREAD, RecruitingPlatform.MYNAVI);

    assertTrue(scout.isPresent());
    Scout registered = scout.get();
    assertNotNull(registered.id());
    assertEquals("Register scout", registered.title());
    assertEquals("Scout Corp", registered.companyName());
    assertEquals(ScoutStatus.UNREAD, registered.status());
    assertEquals(RecruitingPlatform.MYNAVI, registered.platform());
    assertEquals("https://example.com/details", registered.detailsUrl());
    assertNull(registered.creationSourceUrl());
  }

  @Test
  public void testFindScout() {
    Optional<Scout> created =
        createScout("Find scout", ScoutStatus.UNRESPONDED, RecruitingPlatform.ONE_CAREER);
    assertTrue(created.isPresent());

    Optional<Scout> found = scoutService.findScout(TEST_AUTH_ID, created.get().id());

    assertTrue(found.isPresent());
    assertEquals(created.get().id(), found.get().id());
    assertEquals("Find scout", found.get().title());
  }

  @Test
  public void testFindScoutNotFound() {
    Optional<Scout> found = scoutService.findScout(TEST_AUTH_ID, UUID.randomUUID());

    assertFalse(found.isPresent());
  }

  @Test
  public void testGetScoutsWithFilters() {
    createScout("Accepted scout", ScoutStatus.ACCEPTED, RecruitingPlatform.OFFER_BOX);
    createScout("Declined scout", ScoutStatus.DECLINED, RecruitingPlatform.MYNAVI);

    ScoutsPage page =
        scoutService.getScouts(
            TEST_AUTH_ID, 0, 50, ScoutStatus.ACCEPTED, RecruitingPlatform.OFFER_BOX);

    List<Scout> filtered = page.scouts();
    assertFalse(filtered.isEmpty());
    assertTrue(
        filtered.stream()
            .allMatch(
                scout ->
                    scout.status() == ScoutStatus.ACCEPTED
                        && scout.platform() == RecruitingPlatform.OFFER_BOX));
    assertEquals(0, page.page());
    assertEquals(50, page.pageSize());
  }

  @Test
  public void testUpdateScout() {
    Optional<Scout> created =
        createScout("Update scout", ScoutStatus.UNREAD, RecruitingPlatform.MYNAVI);
    assertTrue(created.isPresent());

    Optional<Scout> updated =
        scoutService.updateScout(
            TEST_AUTH_ID,
            created.get().id(),
            "Updated scout",
            "Updated Corp",
            ScoutStatus.ACCEPTED,
            RecruitingPlatform.TRACK_JOB,
            null,
            "https://example.com/source");

    assertTrue(updated.isPresent());
    assertEquals("Updated scout", updated.get().title());
    assertEquals("Updated Corp", updated.get().companyName());
    assertEquals(ScoutStatus.ACCEPTED, updated.get().status());
    assertEquals(RecruitingPlatform.TRACK_JOB, updated.get().platform());
    assertNull(updated.get().detailsUrl());
    assertEquals("https://example.com/source", updated.get().creationSourceUrl());
  }

  @Test
  public void testDeleteScout() {
    Optional<Scout> created =
        createScout("Delete scout", ScoutStatus.UNREAD, RecruitingPlatform.MYNAVI);
    assertTrue(created.isPresent());

    boolean deleted = scoutService.deleteScout(TEST_AUTH_ID, created.get().id());

    assertTrue(deleted);
    assertFalse(scoutService.findScout(TEST_AUTH_ID, created.get().id()).isPresent());
  }

  @Test
  public void testDeleteScoutNotFound() {
    boolean deleted = scoutService.deleteScout(TEST_AUTH_ID, UUID.randomUUID());

    assertFalse(deleted);
  }

  private static Optional<Scout> createScout(
      String title, ScoutStatus status, RecruitingPlatform platform) {
    return scoutService.registerScout(
        TEST_AUTH_ID, title, "Scout Corp", status, platform, "https://example.com/details", null);
  }
}

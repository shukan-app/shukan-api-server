package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.ScoutStatus;
import dev.shoheiyamagiwa.shukan.infra.repository.ScoutRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class ScoutService {
  private final ScoutRepository scoutRepository;

  public ScoutService(ScoutRepository scoutRepository) {
    this.scoutRepository = scoutRepository;
  }

  public boolean userExists(String authId) {
    return scoutRepository.userExists(authId);
  }

  public ScoutsPage getScouts(
      String authId,
      int page,
      int pageSize,
      @Nullable ScoutStatus status,
      @Nullable RecruitingPlatform platform) {
    List<Scout> scouts = scoutRepository.findAll(authId, page, pageSize, status, platform);
    int total = scoutRepository.count(authId, status, platform);
    int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    return new ScoutsPage(scouts, page, pageSize, totalPages);
  }

  public Optional<Scout> findScout(String authId, UUID scoutId) {
    return scoutRepository.findById(authId, scoutId);
  }

  public Optional<Scout> registerScout(
      String authId,
      String title,
      String companyName,
      ScoutStatus status,
      RecruitingPlatform platform,
      @Nullable String detailsUrl,
      @Nullable String creationSourceUrl) {
    return scoutRepository.create(
        authId, title, companyName, status, platform, detailsUrl, creationSourceUrl);
  }

  public Optional<Scout> updateScout(
      String authId,
      UUID scoutId,
      String title,
      String companyName,
      ScoutStatus status,
      RecruitingPlatform platform,
      @Nullable String detailsUrl,
      @Nullable String creationSourceUrl) {
    return scoutRepository.update(
        authId, scoutId, title, companyName, status, platform, detailsUrl, creationSourceUrl);
  }

  public boolean deleteScout(String authId, UUID scoutId) {
    return scoutRepository.softDelete(authId, scoutId);
  }
}

package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;

import java.util.List;

public record ScoutsPage(List<Scout> scouts, int page, int pageSize, int totalPages) {
}

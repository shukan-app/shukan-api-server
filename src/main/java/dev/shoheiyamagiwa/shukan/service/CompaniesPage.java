package dev.shoheiyamagiwa.shukan.service;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;

import java.util.List;

public record CompaniesPage(List<Company> companies, int page, int pageSize, int totalPages) {
}

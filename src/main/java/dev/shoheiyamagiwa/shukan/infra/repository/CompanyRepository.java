package dev.shoheiyamagiwa.shukan.infra.repository;

import dev.shoheiyamagiwa.shukan.domain.entity.Company;
import dev.shoheiyamagiwa.shukan.domain.vo.CompanyStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.ContactType;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.infra.dto.CompanyDto;
import dev.shoheiyamagiwa.shukan.infra.mapper.CompanyMapper;
import org.jspecify.annotations.Nullable;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CompanyRepository {
	private static final String SELECT_COMPANY =
			"SELECT c.id, c.name, c.applied_role,"
					+ " cs.name AS status,"
					+ " ar.name AS application_route,"
					+ " ct.name AS contact_type,"
					+ " c.created_at, c.updated_at, c.deleted_at"
					+ " FROM companies c"
					+ " JOIN company_status cs ON cs.id = c.status_id"
					+ " JOIN application_routes ar ON ar.id = c.application_route_id"
					+ " JOIN contact_types ct ON ct.id = c.contact_type_id";
	
	private final String jdbcUrl;
	private final String jdbcUsername;
	private final String jdbcPassword;
	
	public CompanyRepository(String jdbcUrl, String jdbcUsername, String jdbcPassword) {
		this.jdbcUrl = jdbcUrl;
		this.jdbcUsername = jdbcUsername;
		this.jdbcPassword = jdbcPassword;
	}
	
	private static String buildOrderByClause(@Nullable String sort, @Nullable String order) {
		if (sort == null) {
			return "c.updated_at DESC";
		}
		String dir = "descend".equals(order) ? "DESC" : "ASC";
		return switch (sort) {
			case "taskDate" -> "(SELECT MIN(t.deadline) FROM tasks t"
					+ " WHERE t.company_id = c.id AND t.deleted_at IS NULL) "
					+ dir
					+ " NULLS LAST";
			case "eventDate" -> "(SELECT MIN(e.begin_at) FROM events e"
					+ " WHERE e.company_id = c.id AND e.deleted_at IS NULL) "
					+ dir
					+ " NULLS LAST";
			default -> "c.updated_at DESC";
		};
	}
	
	private static void setParams(PreparedStatement stmt, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			stmt.setObject(i + 1, params.get(i));
		}
	}
	
	public List<Company> findAll(
			String authId,
			int page,
			int pageSize,
			@Nullable String q,
			@Nullable CompanyStatus status,
			@Nullable String sort,
			@Nullable String order) {
		List<Object> params = new ArrayList<>();
		params.add(authId);
		
		StringBuilder where =
				new StringBuilder(
						" WHERE c.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
								+ " AND c.deleted_at IS NULL");
		
		if (q != null && !q.isBlank()) {
			where.append(" AND c.name ILIKE '%' || ? || '%'");
			params.add(q);
		}
		if (status != null) {
			where.append(" AND cs.name = ?");
			params.add(status.getValue());
		}
		
		String orderBy = buildOrderByClause(sort, order);
		String sql = SELECT_COMPANY + where + " ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
		params.add(pageSize);
		params.add((long) page * pageSize);
		
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParams(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				List<Company> companies = new ArrayList<>();
				while (rs.next()) {
					companies.add(CompanyMapper.toEntity(mapRow(rs)));
				}
				return companies;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public int count(String authId, @Nullable String q, @Nullable CompanyStatus status) {
		List<Object> params = new ArrayList<>();
		params.add(authId);
		
		StringBuilder where = new StringBuilder(
				" WHERE c.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
						+ " AND c.deleted_at IS NULL");
		
		if (q != null && !q.isBlank()) {
			where.append(" AND c.name ILIKE '%' || ? || '%'");
			params.add(q);
		}
		
		if (status != null) {
			where.append(" AND cs.name = ?");
			params.add(status.getValue());
		}
		
		String sql =
				"SELECT COUNT(*) FROM companies c"
						+ " JOIN company_status cs ON cs.id = c.status_id"
						+ where;
		
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParams(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public Optional<Company> findById(String authId, UUID companyId) {
		try (Connection conn = getConnection()) {
			return findById(conn, authId, companyId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public Company create(
			String authId,
			String name,
			String appliedRole,
			CompanyStatus status,
			RecruitingPlatform applicationRoute,
			ContactType contactType) {
		try (Connection conn = getConnection()) {
			UUID statusId = resolveLookupId(conn, "company_status", status.getValue());
			UUID applicationRouteId = resolveLookupId(conn, "application_routes", applicationRoute.getValue());
			UUID contactTypeId = resolveLookupId(conn, "contact_types", contactType.getValue());
			
			String insertSql = "INSERT INTO companies"
					+ " (user_id, name, applied_role, status_id, application_route_id, contact_type_id, email_url)"
					+ " VALUES ((SELECT u.id FROM users u WHERE u.auth_id = ?), ?, ?, ?, ?, ?, '')"
					+ " RETURNING id";
			UUID companyId;
			
			try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
				stmt.setString(1, authId);
				stmt.setString(2, name);
				stmt.setString(3, appliedRole);
				stmt.setObject(4, statusId);
				stmt.setObject(5, applicationRouteId);
				stmt.setObject(6, contactTypeId);
				
				try (ResultSet rs = stmt.executeQuery()) {
					rs.next();
					companyId = rs.getObject("id", UUID.class);
				}
			}
			
			return findById(conn, authId, companyId).orElseThrow();
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public Optional<Company> update(
			String authId,
			UUID companyId,
			String name,
			String appliedRole,
			CompanyStatus status,
			RecruitingPlatform applicationRoute,
			ContactType contactType) {
		try (Connection conn = getConnection()) {
			UUID statusId = resolveLookupId(conn, "company_status", status.getValue());
			UUID applicationRouteId = resolveLookupId(conn, "application_routes", applicationRoute.getValue());
			UUID contactTypeId = resolveLookupId(conn, "contact_types", contactType.getValue());
			
			String updateSql = "UPDATE companies SET"
					+ " name = ?, applied_role = ?,"
					+ " status_id = ?, application_route_id = ?, contact_type_id = ?,"
					+ " updated_at = CURRENT_TIMESTAMP"
					+ " WHERE id = ?"
					+ " AND user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
					+ " AND deleted_at IS NULL";
			int affected;
			
			try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
				stmt.setString(1, name);
				stmt.setString(2, appliedRole);
				stmt.setObject(3, statusId);
				stmt.setObject(4, applicationRouteId);
				stmt.setObject(5, contactTypeId);
				stmt.setObject(6, companyId);
				stmt.setString(7, authId);
				affected = stmt.executeUpdate();
			}
			
			if (affected == 0) {
				return Optional.empty();
			}
			
			return findById(conn, authId, companyId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public boolean softDelete(String authId, UUID companyId) {
		String sql = "UPDATE companies SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP"
				+ " WHERE id = ?"
				+ " AND user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND deleted_at IS NULL";
		
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setObject(1, companyId);
			stmt.setString(2, authId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	private Optional<Company> findById(Connection conn, String authId, UUID companyId) throws SQLException {
		String sql = SELECT_COMPANY
				+ " WHERE c.id = ?"
				+ " AND c.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND c.deleted_at IS NULL";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setObject(1, companyId);
			stmt.setString(2, authId);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(CompanyMapper.toEntity(mapRow(rs)));
				}
				return Optional.empty();
			}
		}
	}
	
	private UUID resolveLookupId(Connection conn, String tableName, String value) throws SQLException {
		String selectSql = "SELECT id FROM " + tableName + " WHERE name = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setString(1, value);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getObject("id", UUID.class);
				}
			}
		}
		
		throw new IllegalArgumentException("Unknown value in " + tableName + ": " + value);
	}
	
	@SuppressWarnings("NullAway")
	private CompanyDto mapRow(ResultSet rs) throws SQLException {
		return new CompanyDto(
				rs.getObject("id", UUID.class),
				rs.getString("name"),
				rs.getString("applied_role"),
				rs.getString("status"),
				rs.getString("application_route"),
				rs.getString("contact_type"),
				rs.getObject("created_at", OffsetDateTime.class),
				rs.getObject("updated_at", OffsetDateTime.class),
				rs.getObject("deleted_at", OffsetDateTime.class));
	}
	
	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
	}
}

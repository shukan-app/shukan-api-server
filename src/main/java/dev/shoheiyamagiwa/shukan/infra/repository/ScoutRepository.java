package dev.shoheiyamagiwa.shukan.infra.repository;

import dev.shoheiyamagiwa.shukan.domain.entity.Scout;
import dev.shoheiyamagiwa.shukan.domain.vo.RecruitingPlatform;
import dev.shoheiyamagiwa.shukan.domain.vo.ScoutStatus;
import dev.shoheiyamagiwa.shukan.infra.dto.ScoutDto;
import dev.shoheiyamagiwa.shukan.infra.mapper.ScoutMapper;
import org.jspecify.annotations.Nullable;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ScoutRepository {
	private static final String SELECT_SCOUT =
		"SELECT s.id, s.title, s.company_name,"
			+ " ss.name AS status,"
			+ " rp.name AS platform,"
			+ " s.details_url, s.creation_source_url,"
			+ " s.created_at, s.updated_at, s.deleted_at"
			+ " FROM scouts s"
			+ " JOIN scout_status ss ON ss.id = s.status_id"
			+ " JOIN recruiting_platforms rp ON rp.id = s.recruiting_platform_id";
	
	private final String jdbcUrl;
	private final String jdbcUsername;
	private final String jdbcPassword;
	
	public ScoutRepository(String jdbcUrl, String jdbcUsername, String jdbcPassword) {
		this.jdbcUrl = jdbcUrl;
		this.jdbcUsername = jdbcUsername;
		this.jdbcPassword = jdbcPassword;
	}
	
	private static void setParams(PreparedStatement stmt, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			stmt.setObject(i + 1, params.get(i));
		}
	}
	
	public boolean userExists(String authId) {
		String sql = "SELECT 1 FROM users WHERE auth_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, authId);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public List<Scout> findAll(
		String authId,
		int page,
		int pageSize,
		@Nullable ScoutStatus status,
		@Nullable RecruitingPlatform platform) {
		List<Object> params = new ArrayList<>();
		params.add(authId);
		
		StringBuilder where =
			new StringBuilder(
				" WHERE s.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
					+ " AND s.deleted_at IS NULL");
		
		if (status != null) {
			where.append(" AND ss.name = ?");
			params.add(status.toDatabaseValue());
		}
		
		if (platform != null) {
			where.append(" AND rp.name = ?");
			params.add(platform.toDatabaseValue());
		}
		
		String sql = SELECT_SCOUT + where + " ORDER BY s.created_at, s.id LIMIT ? OFFSET ?";
		params.add(pageSize);
		params.add((long) page * pageSize);
		
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParams(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				List<Scout> scouts = new ArrayList<>();
				while (rs.next()) {
					scouts.add(ScoutMapper.toEntity(mapRow(rs)));
				}
				return scouts;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public int count(
		String authId, @Nullable ScoutStatus status, @Nullable RecruitingPlatform platform) {
		List<Object> params = new ArrayList<>();
		params.add(authId);
		
		StringBuilder where =
			new StringBuilder(
				" WHERE s.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
					+ " AND s.deleted_at IS NULL");
		
		if (status != null) {
			where.append(" AND ss.name = ?");
			params.add(status.toDatabaseValue());
		}
		
		if (platform != null) {
			where.append(" AND rp.name = ?");
			params.add(platform.toDatabaseValue());
		}
		
		String sql =
			"SELECT COUNT(*) FROM scouts s"
				+ " JOIN scout_status ss ON ss.id = s.status_id"
				+ " JOIN recruiting_platforms rp ON rp.id = s.recruiting_platform_id"
				+ where;
		
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParams(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public Optional<Scout> findById(String authId, UUID scoutId) {
		try (Connection conn = getConnection()) {
			return findById(conn, authId, scoutId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public Optional<Scout> create(
		String authId,
		String title,
		String companyName,
		ScoutStatus status,
		RecruitingPlatform platform,
		@Nullable String detailsUrl,
		@Nullable String creationSourceUrl) {
		try (Connection conn = getConnection()) {
			UUID statusId = resolveLookupId(conn, "scout_status", status.toDatabaseValue());
			UUID platformId = resolveLookupId(conn, "recruiting_platforms", platform.toDatabaseValue());
			
			String insertSql =
				"INSERT INTO scouts"
					+ " (user_id, title, company_name, status_id, recruiting_platform_id, details_url, creation_source_url)"
					+ " SELECT u.id, ?, ?, ?, ?, ?, ?"
					+ " FROM users u"
					+ " WHERE u.auth_id = ?"
					+ " RETURNING id";
			UUID scoutId;
			
			try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
				stmt.setString(1, title);
				stmt.setString(2, companyName);
				stmt.setObject(3, statusId);
				stmt.setObject(4, platformId);
				stmt.setString(5, detailsUrl);
				stmt.setString(6, creationSourceUrl);
				stmt.setString(7, authId);
				
				try (ResultSet rs = stmt.executeQuery()) {
					if (!rs.next()) {
						return Optional.empty();
					}
					scoutId = rs.getObject("id", UUID.class);
				}
			}
			
			return findById(conn, authId, scoutId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public Optional<Scout> update(
		String authId,
		UUID scoutId,
		String title,
		String companyName,
		ScoutStatus status,
		RecruitingPlatform platform,
		@Nullable String detailsUrl,
		@Nullable String creationSourceUrl) {
		try (Connection conn = getConnection()) {
			UUID statusId = resolveLookupId(conn, "scout_status", status.toDatabaseValue());
			UUID platformId = resolveLookupId(conn, "recruiting_platforms", platform.toDatabaseValue());
			
			String updateSql =
				"UPDATE scouts s SET"
					+ " title = ?, company_name = ?, status_id = ?, recruiting_platform_id = ?,"
					+ " details_url = ?, creation_source_url = ?, updated_at = CURRENT_TIMESTAMP"
					+ " WHERE s.id = ?"
					+ " AND s.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
					+ " AND s.deleted_at IS NULL";
			int affected;
			
			try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
				stmt.setString(1, title);
				stmt.setString(2, companyName);
				stmt.setObject(3, statusId);
				stmt.setObject(4, platformId);
				stmt.setString(5, detailsUrl);
				stmt.setString(6, creationSourceUrl);
				stmt.setObject(7, scoutId);
				stmt.setString(8, authId);
				affected = stmt.executeUpdate();
			}
			
			if (affected == 0) {
				return Optional.empty();
			}
			
			return findById(conn, authId, scoutId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public boolean softDelete(String authId, UUID scoutId) {
		String sql =
			"UPDATE scouts SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP"
				+ " WHERE id = ?"
				+ " AND user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND deleted_at IS NULL";
		
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setObject(1, scoutId);
			stmt.setString(2, authId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	private Optional<Scout> findById(Connection conn, String authId, UUID scoutId)
		throws SQLException {
		String sql =
			SELECT_SCOUT
				+ " WHERE s.id = ?"
				+ " AND s.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND s.deleted_at IS NULL";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setObject(1, scoutId);
			stmt.setString(2, authId);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(ScoutMapper.toEntity(mapRow(rs)));
				}
				return Optional.empty();
			}
		}
	}
	
	private UUID resolveLookupId(Connection conn, String tableName, String value)
		throws SQLException {
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
	private ScoutDto mapRow(ResultSet rs) throws SQLException {
		return new ScoutDto(
			rs.getObject("id", UUID.class),
			rs.getString("title"),
			rs.getString("company_name"),
			rs.getString("status"),
			rs.getString("platform"),
			rs.getString("details_url"),
			rs.getString("creation_source_url"),
			rs.getObject("created_at", OffsetDateTime.class),
			rs.getObject("updated_at", OffsetDateTime.class),
			rs.getObject("deleted_at", OffsetDateTime.class));
	}
	
	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
	}
}

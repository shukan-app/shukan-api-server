package dev.shoheiyamagiwa.shukan.infra.repository;

import dev.shoheiyamagiwa.shukan.domain.entity.Task;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskCreationSource;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.TaskType;
import dev.shoheiyamagiwa.shukan.infra.dto.TaskDto;
import dev.shoheiyamagiwa.shukan.infra.mapper.TaskMapper;
import org.jspecify.annotations.Nullable;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TaskRepository {
	private static final String SELECT_TASK =
		"SELECT t.id, t.title,"
			+ " c.id AS company_id, c.name AS company_name, c.applied_role AS company_applied_role,"
			+ " cs.name AS company_status,"
			+ " ar.name AS company_application_route,"
			+ " ct.name AS company_contact_type,"
			+ " c.creation_source_url AS company_creation_source_url,"
			+ " c.created_at AS company_created_at, c.updated_at AS company_updated_at, c.deleted_at AS company_deleted_at,"
			+ " tt.name AS type,"
			+ " ts.name AS status,"
			+ " tck.name AS created_by,"
			+ " t.creation_source_url, t.deadline, t.created_at, t.updated_at, t.deleted_at"
			+ " FROM tasks t"
			+ " JOIN companies c ON c.id = t.company_id"
			+ " JOIN company_status cs ON cs.id = c.status_id"
			+ " JOIN application_routes ar ON ar.id = c.application_route_id"
			+ " JOIN contact_types ct ON ct.id = c.contact_type_id"
			+ " JOIN task_types tt ON tt.id = t.type_id"
			+ " JOIN task_status ts ON ts.id = t.status_id"
			+ " JOIN task_creator_kinds tck ON tck.id = t.creator_kind_id";
	
	private final String jdbcUrl;
	private final String jdbcUsername;
	private final String jdbcPassword;
	
	public TaskRepository(String jdbcUrl, String jdbcUsername, String jdbcPassword) {
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
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, authId);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public List<Task> findAll(String authId, int page, int pageSize, @Nullable TaskType type) {
		List<Object> params = new ArrayList<>();
		params.add(authId);
		
		StringBuilder where = new StringBuilder(
			" WHERE t.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND t.deleted_at IS NULL"
				+ " AND c.deleted_at IS NULL");
		
		if (type != null) {
			where.append(" AND tt.name = ?");
			params.add(type.toDatabaseValue());
		}
		
		String sql = SELECT_TASK + where + " ORDER BY t.deadline  NULLS LAST, t.created_at LIMIT ? OFFSET ?";
		params.add(pageSize);
		params.add((long) page * pageSize);
		
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParams(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				List<Task> tasks = new ArrayList<>();
				while (rs.next()) {
					tasks.add(TaskMapper.toEntity(mapRow(rs)));
				}
				return tasks;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public int count(String authId, @Nullable TaskType type) {
		List<Object> params = new ArrayList<>();
		params.add(authId);
		
		StringBuilder where = new StringBuilder(
			" WHERE t.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND t.deleted_at IS NULL"
				+ " AND c.deleted_at IS NULL");
		
		if (type != null) {
			where.append(" AND tt.name = ?");
			params.add(type.toDatabaseValue());
		}
		
		String sql = "SELECT COUNT(*) FROM tasks t"
			+ " JOIN companies c ON c.id = t.company_id"
			+ " JOIN task_types tt ON tt.id = t.type_id"
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
	
	public Optional<Task> findById(String authId, UUID taskId) {
		try (Connection conn = getConnection()) {
			return findById(conn, authId, taskId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public Optional<Task> create(
		String authId,
		String title,
		UUID companyId,
		TaskType type,
		TaskStatus status,
		@Nullable String creationSourceUrl,
		@Nullable OffsetDateTime deadline) {
		try (Connection conn = getConnection()) {
			UUID typeId = resolveLookupId(conn, "task_types", type.toDatabaseValue());
			UUID statusId = resolveLookupId(conn, "task_status", status.toDatabaseValue());
			UUID creatorKindId = resolveLookupId(conn, "task_creator_kinds", TaskCreationSource.USER.toDatabaseValue());
			
			String insertSql = "INSERT INTO tasks"
				+ " (user_id, title, company_id, type_id, status_id, creator_kind_id, creation_source_url, deadline)"
				+ " SELECT u.id, ?, c.id, ?, ?, ?, ?, ?"
				+ " FROM users u"
				+ " JOIN companies c ON c.id = ?"
				+ " WHERE u.auth_id = ?"
				+ " AND c.user_id = u.id"
				+ " AND c.deleted_at IS NULL"
				+ " RETURNING id";
			UUID taskId;
			
			try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
				stmt.setString(1, title);
				stmt.setObject(2, typeId);
				stmt.setObject(3, statusId);
				stmt.setObject(4, creatorKindId);
				stmt.setString(5, creationSourceUrl);
				stmt.setObject(6, deadline);
				stmt.setObject(7, companyId);
				stmt.setString(8, authId);
				
				try (ResultSet rs = stmt.executeQuery()) {
					if (!rs.next()) {
						return Optional.empty();
					}
					taskId = rs.getObject("id", UUID.class);
				}
			}
			
			return findById(conn, authId, taskId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public Optional<Task> update(
		String authId,
		UUID taskId,
		String title,
		UUID companyId,
		TaskType type,
		TaskStatus status,
		@Nullable String creationSourceUrl,
		@Nullable OffsetDateTime deadline) {
		try (Connection conn = getConnection()) {
			if (!isCompanyOwnedByUser(conn, authId, companyId)) {
				return Optional.empty();
			}
			
			UUID typeId = resolveLookupId(conn, "task_types", type.toDatabaseValue());
			UUID statusId = resolveLookupId(conn, "task_status", status.toDatabaseValue());
			
			String updateSql = "UPDATE tasks SET"
				+ " title = ?, company_id = ?, type_id = ?, status_id = ?, creation_source_url = ?, deadline = ?,"
				+ " updated_at = CURRENT_TIMESTAMP"
				+ " WHERE id = ?"
				+ " AND user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND deleted_at IS NULL";
			int affected;
			
			try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
				stmt.setString(1, title);
				stmt.setObject(2, companyId);
				stmt.setObject(3, typeId);
				stmt.setObject(4, statusId);
				stmt.setString(5, creationSourceUrl);
				stmt.setObject(6, deadline);
				stmt.setObject(7, taskId);
				stmt.setString(8, authId);
				affected = stmt.executeUpdate();
			}
			
			if (affected == 0) {
				return Optional.empty();
			}
			
			return findById(conn, authId, taskId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	public boolean softDelete(String authId, UUID taskId) {
		String sql = "UPDATE tasks SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP"
			+ " WHERE id = ?"
			+ " AND user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
			+ " AND deleted_at IS NULL";
		
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setObject(1, taskId);
			stmt.setString(2, authId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}
	
	private Optional<Task> findById(Connection conn, String authId, UUID taskId) throws SQLException {
		String sql = SELECT_TASK
			+ " WHERE t.id = ?"
			+ " AND t.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
			+ " AND t.deleted_at IS NULL"
			+ " AND c.deleted_at IS NULL";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setObject(1, taskId);
			stmt.setString(2, authId);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(TaskMapper.toEntity(mapRow(rs)));
				}
				return Optional.empty();
			}
		}
	}
	
	private boolean isCompanyOwnedByUser(Connection conn, String authId, UUID companyId) throws SQLException {
		String sql = "SELECT 1 FROM companies c"
			+ " WHERE c.id = ?"
			+ " AND c.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
			+ " AND c.deleted_at IS NULL";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setObject(1, companyId);
			stmt.setString(2, authId);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
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
	private TaskDto mapRow(ResultSet rs) throws SQLException {
		return new TaskDto(
			rs.getObject("id", UUID.class),
			rs.getString("title"),
			rs.getObject("company_id", UUID.class),
			rs.getString("company_name"),
			rs.getString("company_applied_role"),
			rs.getString("company_status"),
			rs.getString("company_application_route"),
			rs.getString("company_contact_type"),
			rs.getString("company_creation_source_url"),
			rs.getObject("company_created_at", OffsetDateTime.class),
			rs.getObject("company_updated_at", OffsetDateTime.class),
			rs.getObject("company_deleted_at", OffsetDateTime.class),
			rs.getString("type"),
			rs.getString("status"),
			rs.getString("created_by"),
			rs.getString("creation_source_url"),
			rs.getObject("deadline", OffsetDateTime.class),
			rs.getObject("created_at", OffsetDateTime.class),
			rs.getObject("updated_at", OffsetDateTime.class),
			rs.getObject("deleted_at", OffsetDateTime.class));
	}
	
	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
	}
}

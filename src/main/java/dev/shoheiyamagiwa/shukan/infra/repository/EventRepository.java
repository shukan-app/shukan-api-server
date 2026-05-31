package dev.shoheiyamagiwa.shukan.infra.repository;

import dev.shoheiyamagiwa.shukan.domain.entity.Event;
import dev.shoheiyamagiwa.shukan.domain.vo.EventFormatType;
import dev.shoheiyamagiwa.shukan.domain.vo.EventStatus;
import dev.shoheiyamagiwa.shukan.domain.vo.EventType;
import dev.shoheiyamagiwa.shukan.infra.dto.EventDto;
import dev.shoheiyamagiwa.shukan.infra.mapper.EventMapper;
import org.jspecify.annotations.Nullable;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class EventRepository {
	private static final String SELECT_EVENT =
		"SELECT e.id, e.title,"
			+ " c.id AS company_id, c.name AS company_name, c.applied_role AS company_applied_role,"
			+ " cs.name AS company_status,"
			+ " ar.name AS company_application_route,"
			+ " ct.name AS company_contact_type,"
			+ " c.creation_source_url AS company_creation_source_url,"
			+ " c.created_at AS company_created_at, c.updated_at AS company_updated_at, c.deleted_at AS company_deleted_at,"
			+ " et.name AS type,"
			+ " es.name AS status,"
			+ " eft.name AS format_type,"
			+ " e.location, e.creation_source_url, e.begin_at, e.end_at,"
			+ " e.created_at, e.updated_at, e.deleted_at"
			+ " FROM events e"
			+ " JOIN companies c ON c.id = e.company_id"
			+ " JOIN company_status cs ON cs.id = c.status_id"
			+ " JOIN application_routes ar ON ar.id = c.application_route_id"
			+ " JOIN contact_types ct ON ct.id = c.contact_type_id"
			+ " JOIN event_types et ON et.id = e.type_id"
			+ " JOIN event_status es ON es.id = e.status_id"
			+ " JOIN event_format_types eft ON eft.id = e.format_type_id";

	private final String jdbcUrl;
	private final String jdbcUsername;
	private final String jdbcPassword;

	public EventRepository(String jdbcUrl, String jdbcUsername, String jdbcPassword) {
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

	public List<Event> findAll(
		String authId,
		int page,
		int pageSize,
		@Nullable EventType type,
		@Nullable EventStatus status) {
		List<Object> params = new ArrayList<>();
		params.add(authId);

		StringBuilder where = new StringBuilder(
			" WHERE e.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND e.deleted_at IS NULL"
				+ " AND c.deleted_at IS NULL");

		if (type != null) {
			where.append(" AND et.name = ?");
			params.add(type.toDatabaseValue());
		}

		if (status != null) {
			where.append(" AND es.name = ?");
			params.add(status.toDatabaseValue());
		}

		String sql = SELECT_EVENT
			+ where
			+ " ORDER BY e.begin_at NULLS LAST, e.end_at NULLS LAST, e.created_at, e.id LIMIT ? OFFSET ?";
		params.add(pageSize);
		params.add((long) page * pageSize);

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParams(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				List<Event> events = new ArrayList<>();
				while (rs.next()) {
					events.add(EventMapper.toEntity(mapRow(rs)));
				}
				return events;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}

	public int count(String authId, @Nullable EventType type, @Nullable EventStatus status) {
		List<Object> params = new ArrayList<>();
		params.add(authId);

		StringBuilder where = new StringBuilder(
			" WHERE e.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND e.deleted_at IS NULL"
				+ " AND c.deleted_at IS NULL");

		if (type != null) {
			where.append(" AND et.name = ?");
			params.add(type.toDatabaseValue());
		}

		if (status != null) {
			where.append(" AND es.name = ?");
			params.add(status.toDatabaseValue());
		}

		String sql = "SELECT COUNT(*) FROM events e"
			+ " JOIN companies c ON c.id = e.company_id"
			+ " JOIN event_types et ON et.id = e.type_id"
			+ " JOIN event_status es ON es.id = e.status_id"
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

	public Optional<Event> findById(String authId, UUID eventId) {
		try (Connection conn = getConnection()) {
			return findById(conn, authId, eventId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}

	public Optional<Event> create(
		String authId,
		String title,
		UUID companyId,
		EventType type,
		EventStatus status,
		EventFormatType formatType,
		@Nullable String location,
		@Nullable String creationSourceUrl,
		OffsetDateTime beginAt,
		OffsetDateTime endAt) {
		try (Connection conn = getConnection()) {
			UUID typeId = resolveLookupId(conn, "event_types", type.toDatabaseValue());
			UUID statusId = resolveLookupId(conn, "event_status", status.toDatabaseValue());
			UUID formatTypeId = resolveLookupId(conn, "event_format_types", formatType.toDatabaseValue());

			String insertSql = "INSERT INTO events"
				+ " (user_id, title, company_id, type_id, status_id, format_type_id,"
				+ " location, creation_source_url, begin_at, end_at)"
				+ " SELECT u.id, ?, c.id, ?, ?, ?, ?, ?, ?, ?"
				+ " FROM users u"
				+ " JOIN companies c ON c.id = ?"
				+ " WHERE u.auth_id = ?"
				+ " AND c.user_id = u.id"
				+ " AND c.deleted_at IS NULL"
				+ " RETURNING id";
			UUID eventId;

			try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
				stmt.setString(1, title);
				stmt.setObject(2, typeId);
				stmt.setObject(3, statusId);
				stmt.setObject(4, formatTypeId);
				stmt.setString(5, location);
				stmt.setString(6, creationSourceUrl);
				stmt.setObject(7, beginAt);
				stmt.setObject(8, endAt);
				stmt.setObject(9, companyId);
				stmt.setString(10, authId);

				try (ResultSet rs = stmt.executeQuery()) {
					if (!rs.next()) {
						return Optional.empty();
					}
					eventId = rs.getObject("id", UUID.class);
				}
			}

			return findById(conn, authId, eventId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}

	public Optional<Event> update(
		String authId,
		UUID eventId,
		String title,
		UUID companyId,
		EventType type,
		EventStatus status,
		EventFormatType formatType,
		@Nullable String location,
		@Nullable String creationSourceUrl,
		OffsetDateTime beginAt,
		OffsetDateTime endAt) {
		try (Connection conn = getConnection()) {
			if (!isCompanyOwnedByUser(conn, authId, companyId)) {
				return Optional.empty();
			}

			UUID typeId = resolveLookupId(conn, "event_types", type.toDatabaseValue());
			UUID statusId = resolveLookupId(conn, "event_status", status.toDatabaseValue());
			UUID formatTypeId = resolveLookupId(conn, "event_format_types", formatType.toDatabaseValue());

			String updateSql = "UPDATE events e SET"
				+ " title = ?, company_id = ?, type_id = ?, status_id = ?, format_type_id = ?,"
				+ " location = ?, creation_source_url = ?, begin_at = ?, end_at = ?,"
				+ " updated_at = CURRENT_TIMESTAMP"
				+ " WHERE e.id = ?"
				+ " AND e.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
				+ " AND e.deleted_at IS NULL"
				+ " AND EXISTS ("
				+ " SELECT 1 FROM companies current_c"
				+ " WHERE current_c.id = e.company_id"
				+ " AND current_c.deleted_at IS NULL"
				+ " )";
			int affected;

			try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
				stmt.setString(1, title);
				stmt.setObject(2, companyId);
				stmt.setObject(3, typeId);
				stmt.setObject(4, statusId);
				stmt.setObject(5, formatTypeId);
				stmt.setString(6, location);
				stmt.setString(7, creationSourceUrl);
				stmt.setObject(8, beginAt);
				stmt.setObject(9, endAt);
				stmt.setObject(10, eventId);
				stmt.setString(11, authId);
				affected = stmt.executeUpdate();
			}

			if (affected == 0) {
				return Optional.empty();
			}

			return findById(conn, authId, eventId);
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}

	public boolean softDelete(String authId, UUID eventId) {
		String sql = "UPDATE events SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP"
			+ " WHERE id = ?"
			+ " AND user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
			+ " AND deleted_at IS NULL";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setObject(1, eventId);
			stmt.setString(2, authId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new RuntimeException("Database error", e);
		}
	}

	private Optional<Event> findById(Connection conn, String authId, UUID eventId) throws SQLException {
		String sql = SELECT_EVENT
			+ " WHERE e.id = ?"
			+ " AND e.user_id = (SELECT u.id FROM users u WHERE u.auth_id = ?)"
			+ " AND e.deleted_at IS NULL"
			+ " AND c.deleted_at IS NULL";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setObject(1, eventId);
			stmt.setString(2, authId);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(EventMapper.toEntity(mapRow(rs)));
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
	private EventDto mapRow(ResultSet rs) throws SQLException {
		return new EventDto(
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
			rs.getString("format_type"),
			rs.getString("location"),
			rs.getString("creation_source_url"),
			rs.getObject("begin_at", OffsetDateTime.class),
			rs.getObject("end_at", OffsetDateTime.class),
			rs.getObject("created_at", OffsetDateTime.class),
			rs.getObject("updated_at", OffsetDateTime.class),
			rs.getObject("deleted_at", OffsetDateTime.class));
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
	}
}

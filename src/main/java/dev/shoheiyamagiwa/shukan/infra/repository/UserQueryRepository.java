package dev.shoheiyamagiwa.shukan.infra.repository;

final class UserQueryRepository {
	private UserQueryRepository() {
	}

	static final String ACTIVE_USER_ID_BY_AUTH_ID =
		"SELECT u.id FROM users u WHERE u.auth_id = ? AND u.deleted_at IS NULL";

	static final String ACTIVE_USER_EXISTS_BY_AUTH_ID =
		"SELECT 1 FROM users WHERE auth_id = ? AND deleted_at IS NULL";
}

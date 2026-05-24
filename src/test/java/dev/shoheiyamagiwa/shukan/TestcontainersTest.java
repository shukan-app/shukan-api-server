package dev.shoheiyamagiwa.shukan;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class TestcontainersTest {
	@Container
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");
	
	@Test
	void shouldBeRunning() {
		assertTrue(postgres.isRunning());
	}
	
	@Test
	void shouldGetResult() throws SQLException {
		String jdbcUrl = postgres.getJdbcUrl();
		String username = postgres.getUsername();
		String password = postgres.getPassword();
		
		try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
		     Statement statement = connection.createStatement()) {
			
			ResultSet rs = statement.executeQuery("SELECT 1 AS result");
			assertTrue(rs.next());
			assertEquals(1, rs.getInt("result"), "It should be successful to query data to PostgreSQL");
		}
	}
}

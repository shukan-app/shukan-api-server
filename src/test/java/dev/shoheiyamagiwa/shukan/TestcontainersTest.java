package dev.shoheiyamagiwa.shukan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
public class TestcontainersTest {
  @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

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

      try (ResultSet rs = statement.executeQuery("SELECT 1 AS result")) {
        assertTrue(rs.next());
        assertEquals(1, rs.getInt("result"), "Querying PostgreSQL returned an unexpected value");
      }
    }
  }
}

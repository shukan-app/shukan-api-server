package dev.shoheiyamagiwa.shukan;

import org.flywaydb.core.Flyway;

public final class Main {
	void main() {
		migrateDatabase();
		
		// TODO: Implement Javalin application logic
	}
	
	private void migrateDatabase() {
		String url = requireEnv("JDBC_DATABASE_URL");
		String user = requireEnv("JDBC_DATABASE_USERNAME");
		String password = requireEnv("JDBC_DATABASE_PASSWORD");
		
		Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
		flyway.migrate();
	}
	
	private String requireEnv(String name) {
		String value = System.getenv(name);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing required environment variable: " + name);
		}
		return value;
	}
}

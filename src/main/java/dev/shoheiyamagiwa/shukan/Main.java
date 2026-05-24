package dev.shoheiyamagiwa.shukan;

import dev.shoheiyamagiwa.shukan.controller.ErrorResponseDto;
import dev.shoheiyamagiwa.shukan.controller.HealthController;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson3;
import org.flywaydb.core.Flyway;
import org.jspecify.annotations.Nullable;

public final class Main {
	public static void main(String[] args) {
		Main main = new Main();
		main.start();
	}
	
	void start() {
		migrateDatabase();
		createApplication().start(resolvePort());
	}
	
	static Javalin createApplication() {
		return Javalin.create(config -> {
			config.http.defaultContentType = ContentType.JSON;
			config.jsonMapper(new JavalinJackson3());
			
			new HealthController().registerRoutes(config.routes);
			
			config.routes.error(HttpStatus.NOT_FOUND, ctx -> ctx
					.status(HttpStatus.NOT_FOUND)
					.json(new ErrorResponseDto("Not Found")));
			config.routes.exception(Exception.class, (exception, ctx) -> ctx
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.json(new ErrorResponseDto("Internal Server Error")));
		});
	}
	
	static int resolvePort() {
		return parsePort(System.getenv("PORT"));
	}
	
	static int parsePort(@Nullable String value) {
		if (value == null || value.isBlank()) {
			return 7070;
		}
		
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			throw new IllegalStateException("PORT must be a valid integer: " + value, exception);
		}
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

package dev.shoheiyamagiwa.shukan;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import dev.shoheiyamagiwa.shukan.controller.ErrorResponseDto;
import dev.shoheiyamagiwa.shukan.controller.HealthController;
import dev.shoheiyamagiwa.shukan.middleware.AuthMiddleware;
import dev.shoheiyamagiwa.shukan.middleware.AuthMiddlewareProvider;
import dev.shoheiyamagiwa.shukan.middleware.DenyingAuthMiddlewareProvider;
import dev.shoheiyamagiwa.shukan.middleware.FirebaseAuthMiddlewareProvider;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson3;
import io.javalin.router.JavalinDefaultRoutingApi;
import org.flywaydb.core.Flyway;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.Consumer;

public final class Main {
	public static void main(String[] args) {
		Main main = new Main();
		main.start();
	}
	
	public static Javalin createApplication() {
		return createApplication(new DenyingAuthMiddlewareProvider());
	}
	
	private static Javalin createApplication(AuthMiddlewareProvider authMiddlewareProvider) {
		return createApplication(authMiddlewareProvider, ignored -> {
		});
	}
	
	public static Javalin createApplication(AuthMiddlewareProvider authMiddlewareProvider,
	                                        Consumer<JavalinDefaultRoutingApi> routeRegistrar) {
		return Javalin.create(config -> {
			config.http.defaultContentType = ContentType.JSON;
			config.jsonMapper(new JavalinJackson3());
			
			new AuthMiddleware(authMiddlewareProvider).registerRoutes(config.routes);
			new HealthController().registerRoutes(config.routes);
			routeRegistrar.accept(config.routes);
			
			config.routes.error(HttpStatus.NOT_FOUND, ctx -> ctx
				.status(HttpStatus.NOT_FOUND)
				.json(new ErrorResponseDto("Not Found")));
			config.routes.exception(Exception.class, (exception, ctx) -> ctx
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.json(new ErrorResponseDto("Internal Server Error")));
		});
	}
	
	private static int resolvePort() {
		return parsePort(System.getenv("PORT"));
	}
	
	public static int parsePort(@Nullable String value) {
		if (value == null || value.isBlank()) {
			return 7070;
		}
		
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			throw new IllegalStateException("PORT must be a valid integer: " + value, exception);
		}
	}
	
	private void start() {
		migrateDatabase();
		FirebaseApp firebaseApp = initializeFirebase();
		createApplication(new FirebaseAuthMiddlewareProvider(
			FirebaseAuth.getInstance(firebaseApp)
		)).start(resolvePort());
	}

	private static FirebaseApp initializeFirebase() {
		try {
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.getApplicationDefault())
				.build();
			return FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load application default credentials", e);
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

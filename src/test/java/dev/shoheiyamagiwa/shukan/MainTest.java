package dev.shoheiyamagiwa.shukan;

import dev.shoheiyamagiwa.shukan.middleware.AuthMiddlewareProvider;
import dev.shoheiyamagiwa.shukan.middleware.AuthenticatedRequestContext;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public final class MainTest {
	@Test
	public void testHealthEndpoint() throws IOException, InterruptedException {
		Javalin app = Main.createApplication().start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/health");
			
			assertEquals(200, response.statusCode());
			assertEquals("{\"status\":\"ok\"}", response.body());
			assertTrue(response.headers().firstValue("content-type").orElse("").contains("application/json"));
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testNotFoundEndpoint() throws IOException, InterruptedException {
		Javalin app = Main.createApplication().start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/missing");
			
			assertEquals(404, response.statusCode());
			assertEquals("{\"message\":\"Not Found\"}", response.body());
			assertTrue(response.headers().firstValue("content-type").orElse("").contains("application/json"));
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointAcceptsVerifiedHeaders() throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true, true, true)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected",
				"Authorization", "Bearer valid-token",
				"X-Firebase-AppCheck", "valid-app-check-token",
				"CF-Connecting-IP", "203.0.113.10");
			
			assertEquals(200, response.statusCode());
			assertEquals("{\"authId\":\"auth-valid-token\",\"clientIp\":\"203.0.113.10\"}", response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsMissingAuthorizationHeader() throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true, true, true)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected",
				"X-Firebase-AppCheck", "valid-app-check-token",
				"CF-Connecting-IP", "203.0.113.10");
			
			assertEquals(401, response.statusCode());
			assertEquals("{\"message\":\"Unauthorized\"}", response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsMalformedAuthorizationHeader() throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true, true, true)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected",
				"Authorization", "valid-token",
				"X-Firebase-AppCheck", "valid-app-check-token",
				"CF-Connecting-IP", "203.0.113.10");
			
			assertEquals(401, response.statusCode());
			assertEquals("{\"message\":\"Unauthorized\"}", response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsAuthorizationProviderFailure() throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(false, true, true)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected",
				"Authorization", "Bearer valid-token",
				"X-Firebase-AppCheck", "valid-app-check-token",
				"CF-Connecting-IP", "203.0.113.10");
			
			assertEquals(401, response.statusCode());
			assertEquals("{\"message\":\"Unauthorized\"}", response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsMissingAppCheckHeader() throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true, true, true)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected",
				"Authorization", "Bearer valid-token",
				"CF-Connecting-IP", "203.0.113.10");
			
			assertEquals(403, response.statusCode());
			assertEquals("{\"message\":\"Forbidden\"}", response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsAppCheckProviderFailure() throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true, false, true)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected",
				"Authorization", "Bearer valid-token",
				"X-Firebase-AppCheck", "valid-app-check-token",
				"CF-Connecting-IP", "203.0.113.10");
			
			assertEquals(403, response.statusCode());
			assertEquals("{\"message\":\"Forbidden\"}", response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsMissingConnectingIpHeader() throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true, true, true)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected",
				"Authorization", "Bearer valid-token",
				"X-Firebase-AppCheck", "valid-app-check-token");
			
			assertEquals(400, response.statusCode());
			assertEquals("{\"message\":\"Bad Request\"}", response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsConnectingIpProviderFailure() throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true, true, false)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected",
				"Authorization", "Bearer valid-token",
				"X-Firebase-AppCheck", "valid-app-check-token",
				"CF-Connecting-IP", "203.0.113.10");
			
			assertEquals(400, response.statusCode());
			assertEquals("{\"message\":\"Bad Request\"}", response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testParsePortDefaultsTo7070WhenUnset() {
		assertEquals(7070, Main.parsePort(null));
	}
	
	@Test
	public void testParsePortUsesConfiguredPort() {
		assertEquals(9090, Main.parsePort("9090"));
	}
	
	@Test
	public void testParsePortRejectsInvalidPort() {
		assertThrows(IllegalStateException.class, () -> Main.parsePort("invalid"));
	}
	
	private Javalin createProtectedApplication(AuthMiddlewareProvider provider) {
		return Main.createApplication(provider, routes -> routes.get("/protected", ctx -> {
			AuthenticatedRequestContext authContext = ctx.attribute(AuthenticatedRequestContext.ATTRIBUTE_NAME);
			if (authContext == null) {
				throw new IllegalStateException("Missing authenticated request context");
			}
			ctx.json(new ProtectedResponseDto(authContext.authId(), authContext.clientIp()));
		}));
	}
	
	private HttpResponse<String> sendGet(Javalin app, String path, String... headers)
		throws IOException, InterruptedException {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + app.port() + path))
			.GET();
		
		for (int i = 0; i < headers.length; i += 2) {
			requestBuilder.header(headers[i], headers[i + 1]);
		}
		
		HttpRequest request = requestBuilder.build();
		
		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	}
	
	private record ProtectedResponseDto(String authId, String clientIp) {
	}
	
	private record TestAuthMiddlewareProvider(boolean acceptsAuthorization, boolean acceptsAppCheck,
	                                          boolean acceptsConnectingIp) implements AuthMiddlewareProvider {
		@Override
		public Optional<String> verifyBearerToken(String bearerToken) {
			if (!acceptsAuthorization) {
				return Optional.empty();
			}
			return Optional.of("auth-" + bearerToken);
		}
		
		@Override
		public boolean verifyAppCheckToken(String appCheckToken) {
			return acceptsAppCheck;
		}
		
		@Override
		public Optional<String> verifyConnectingIp(String connectingIp) {
			if (!acceptsConnectingIp) {
				return Optional.empty();
			}
			return Optional.of(connectingIp);
		}
	}
}

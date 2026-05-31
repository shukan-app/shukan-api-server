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
			assertTrue(
					response.headers().firstValue("content-type").orElse("").contains("application/json"));
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
			assertEquals(
				"{\"type\":\"about:blank\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"Not Found\",\"instance\":\"/missing\"}",
				response.body());
			assertTrue(
					response.headers().firstValue("content-type").orElse("").contains("application/problem+json"));
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointAcceptsVerifiedToken() throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true)).start(0);
		
		try {
			HttpResponse<String> response =
					sendGet(app, "/protected", "Authorization", "Bearer valid-token");
			
			assertEquals(200, response.statusCode());
			assertEquals("{\"userId\":\"auth-valid-token\"}", response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsMissingAuthorizationHeader()
			throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected");
			
			assertEquals(401, response.statusCode());
			assertEquals(unauthorizedProblemDetails(), response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsMalformedAuthorizationHeader()
			throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(true)).start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/protected", "Authorization", "valid-token");
			
			assertEquals(401, response.statusCode());
			assertEquals(unauthorizedProblemDetails(), response.body());
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testProtectedEndpointRejectsAuthorizationProviderFailure()
			throws IOException, InterruptedException {
		Javalin app = createProtectedApplication(new TestAuthMiddlewareProvider(false)).start(0);
		
		try {
			HttpResponse<String> response =
					sendGet(app, "/protected", "Authorization", "Bearer valid-token");
			
			assertEquals(401, response.statusCode());
			assertEquals(unauthorizedProblemDetails(), response.body());
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
		return Main.createApplication(
				provider,
				routes ->
						routes.get(
								"/protected",
								ctx -> {
									AuthenticatedRequestContext authContext =
											ctx.attribute(AuthenticatedRequestContext.ATTRIBUTE_NAME);
									if (authContext == null) {
										throw new IllegalStateException("Missing authenticated request context");
									}
									ctx.json(new ProtectedResponseDto(authContext.userId()));
								}));
	}
	
	private HttpResponse<String> sendGet(Javalin app, String path, String... headers)
			throws IOException, InterruptedException {
		HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().uri(URI.create("http://localhost:" + app.port() + path)).GET();
		
		for (int i = 0; i < headers.length; i += 2) {
			requestBuilder.header(headers[i], headers[i + 1]);
		}
		
		HttpRequest request = requestBuilder.build();
		
		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	}
	
	private String unauthorizedProblemDetails() {
		return "{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Unauthorized\",\"instance\":\"/protected\"}";
	}
	
	private record ProtectedResponseDto(String userId) {
	}
	
	private record TestAuthMiddlewareProvider(boolean acceptsAuthorization)
			implements AuthMiddlewareProvider {
		@Override
		public Optional<String> verifyBearerToken(String bearerToken) {
			if (!acceptsAuthorization) {
				return Optional.empty();
			}
			return Optional.of("auth-" + bearerToken);
		}
	}
}

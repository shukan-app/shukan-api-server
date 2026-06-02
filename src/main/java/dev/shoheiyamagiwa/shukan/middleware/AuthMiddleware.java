package dev.shoheiyamagiwa.shukan.middleware;

import dev.shoheiyamagiwa.shukan.presentation.ErrorResponses;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.router.JavalinDefaultRoutingApi;

import java.util.Optional;

public final class AuthMiddleware {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	
	private final AuthMiddlewareProvider provider;
	
	public AuthMiddleware(AuthMiddlewareProvider provider) {
		this.provider = provider;
	}
	
	public void registerRoutes(JavalinDefaultRoutingApi routes) {
		routes.beforeMatched(this::handle);
	}
	
	private void handle(Context ctx) {
		if (ctx.path().equals("/health")) {
			return;
		}
		
		String authorizationHeader = ctx.header(AUTHORIZATION_HEADER);
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			reject(ctx, HttpStatus.UNAUTHORIZED, "Unauthorized");
			return;
		}
		
		String bearerToken = authorizationHeader.substring(BEARER_PREFIX.length());
		if (bearerToken.isBlank()) {
			reject(ctx, HttpStatus.UNAUTHORIZED, "Unauthorized");
			return;
		}
		
		Optional<String> userId = provider.verifyBearerToken(bearerToken);
		if (userId.isEmpty()) {
			reject(ctx, HttpStatus.UNAUTHORIZED, "Unauthorized");
			return;
		}
		
		ctx.attribute(
			AuthenticatedRequestContext.ATTRIBUTE_NAME, new AuthenticatedRequestContext(userId.get()));
	}
	
	private void reject(Context ctx, HttpStatus status, String message) {
		ErrorResponses.respond(ctx, status, message);
		ctx.skipRemainingHandlers();
	}
}

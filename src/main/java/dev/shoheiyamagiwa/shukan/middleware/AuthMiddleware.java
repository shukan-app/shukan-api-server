package dev.shoheiyamagiwa.shukan.middleware;

import dev.shoheiyamagiwa.shukan.controller.ErrorResponseDto;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.router.JavalinDefaultRoutingApi;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class AuthMiddleware {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String APP_CHECK_HEADER = "X-Firebase-AppCheck";
	private static final String CONNECTING_IP_HEADER = "CF-Connecting-IP";
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

		Optional<String> authId = provider.verifyBearerToken(bearerToken);
		if (authId.isEmpty()) {
			reject(ctx, HttpStatus.UNAUTHORIZED, "Unauthorized");
			return;
		}

		String appCheckToken = ctx.header(APP_CHECK_HEADER);
		if (isBlank(appCheckToken) || !provider.verifyAppCheckToken(appCheckToken)) {
			reject(ctx, HttpStatus.FORBIDDEN, "Forbidden");
			return;
		}

		String connectingIp = ctx.header(CONNECTING_IP_HEADER);
		if (isBlank(connectingIp)) {
			reject(ctx, HttpStatus.BAD_REQUEST, "Bad Request");
			return;
		}

		Optional<String> clientIp = provider.verifyConnectingIp(connectingIp);
		if (clientIp.isEmpty()) {
			reject(ctx, HttpStatus.BAD_REQUEST, "Bad Request");
			return;
		}

		ctx.attribute(AuthenticatedRequestContext.ATTRIBUTE_NAME,
				new AuthenticatedRequestContext(authId.get(), clientIp.get()));
	}

	private boolean isBlank(@Nullable String value) {
		return value == null || value.isBlank();
	}

	private void reject(Context ctx, HttpStatus status, String message) {
		ctx.status(status).json(new ErrorResponseDto(message));
		ctx.skipRemainingHandlers();
	}
}

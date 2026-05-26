package dev.shoheiyamagiwa.shukan.middleware;

import java.util.Optional;

public final class DenyingAuthMiddlewareProvider implements AuthMiddlewareProvider {
	@Override
	public Optional<String> verifyBearerToken(String bearerToken) {
		return Optional.empty();
	}


}

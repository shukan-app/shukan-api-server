package dev.shoheiyamagiwa.shukan.middleware;

import java.util.Optional;

public final class DenyingAuthMiddlewareProvider implements AuthMiddlewareProvider {
	@Override
	public Optional<String> verifyBearerToken(String bearerToken) {
		return Optional.empty();
	}
	
	@Override
	public boolean verifyAppCheckToken(String appCheckToken) {
		return false;
	}
	
	@Override
	public Optional<String> verifyConnectingIp(String connectingIp) {
		return Optional.empty();
	}
}

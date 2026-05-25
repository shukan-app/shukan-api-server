package dev.shoheiyamagiwa.shukan.middleware;

import java.util.Optional;

public interface AuthorizationHeaderProvider {
	Optional<String> verifyBearerToken(String bearerToken);
}

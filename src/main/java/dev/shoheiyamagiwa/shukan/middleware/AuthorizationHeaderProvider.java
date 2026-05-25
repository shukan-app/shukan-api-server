package dev.shoheiyamagiwa.shukan.middleware;

import java.util.Optional;

public interface AuthorizationHeaderProvider {
	/**
	 * Verifies the bearer token extracted from the {@code Authorization} header.
	 *
	 * @param bearerToken token value without the {@code Bearer } prefix
	 * @return authenticated user auth ID when the token is valid, otherwise {@link Optional#empty()}
	 */
	Optional<String> verifyBearerToken(String bearerToken);
}

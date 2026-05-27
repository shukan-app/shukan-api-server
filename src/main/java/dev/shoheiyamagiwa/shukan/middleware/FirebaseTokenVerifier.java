package dev.shoheiyamagiwa.shukan.middleware;

import java.util.Optional;

/**
 * Abstracts Firebase token verification to allow testing without depending on the Firebase SDK directly.
 */
@FunctionalInterface
interface FirebaseTokenVerifier {
	/**
	 * Verifies the given bearer token and returns the authenticated user's UID.
	 *
	 * @param token the Firebase ID token to verify
	 * @return the UID of the authenticated user, or {@link Optional#empty()} if the token is invalid
	 */
	Optional<String> verify(String token);
}

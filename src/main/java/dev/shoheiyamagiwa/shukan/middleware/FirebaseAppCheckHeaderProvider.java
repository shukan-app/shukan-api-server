package dev.shoheiyamagiwa.shukan.middleware;

public interface FirebaseAppCheckHeaderProvider {
	/**
	 * Verifies the Firebase App Check token from the {@code X-Firebase-AppCheck} header.
	 *
	 * @param appCheckToken Firebase App Check token header value
	 * @return {@code true} when the token is accepted, otherwise {@code false}
	 */
	boolean verifyAppCheckToken(String appCheckToken);
}

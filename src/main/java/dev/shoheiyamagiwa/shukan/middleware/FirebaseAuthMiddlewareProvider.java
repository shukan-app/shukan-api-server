package dev.shoheiyamagiwa.shukan.middleware;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class FirebaseAuthMiddlewareProvider implements AuthMiddlewareProvider {
	private static final Logger logger =
			LoggerFactory.getLogger(FirebaseAuthMiddlewareProvider.class);
	
	private final FirebaseTokenVerifier verifier;
	
	public FirebaseAuthMiddlewareProvider(FirebaseAuth firebaseAuth) {
		this.verifier =
				token -> {
					try {
						@Nullable FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);
						if (firebaseToken == null) {
							return Optional.empty();
						}
						@Nullable String uid = firebaseToken.getUid();
						if (uid == null || uid.isBlank()) {
							return Optional.empty();
						}
						return Optional.of(uid);
					} catch (FirebaseAuthException e) {
						return Optional.empty();
					}
				};
	}
	
	/**
	 * Package-private constructor for testing.
	 */
	FirebaseAuthMiddlewareProvider(FirebaseTokenVerifier verifier) {
		this.verifier = verifier;
	}
	
	@Override
	public Optional<String> verifyBearerToken(String bearerToken) {
		try {
			return verifier.verify(bearerToken);
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		} catch (RuntimeException e) {
			logger.error("Unexpected error during bearer token verification", e);
			return Optional.empty();
		}
	}
}

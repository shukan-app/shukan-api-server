package dev.shoheiyamagiwa.shukan.middleware;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class FirebaseAuthMiddlewareProvider implements AuthMiddlewareProvider {
	private final FirebaseTokenVerifier verifier;

	public FirebaseAuthMiddlewareProvider(FirebaseAuth firebaseAuth) {
		this.verifier = token -> {
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
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}
}

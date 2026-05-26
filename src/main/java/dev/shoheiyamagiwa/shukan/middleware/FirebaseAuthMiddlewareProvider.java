package dev.shoheiyamagiwa.shukan.middleware;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import java.util.Optional;

public final class FirebaseAuthMiddlewareProvider implements AuthMiddlewareProvider {
	private final FirebaseAuth firebaseAuth;

	public FirebaseAuthMiddlewareProvider(FirebaseAuth firebaseAuth) {
		this.firebaseAuth = firebaseAuth;
	}

	@Override
	public Optional<String> verifyBearerToken(String bearerToken) {
		try {
			FirebaseToken token = firebaseAuth.verifyIdToken(bearerToken);
			if (token == null) {
				return Optional.empty();
			}
			String uid = token.getUid();
			if (uid == null || uid.isBlank()) {
				return Optional.empty();
			}
			return Optional.of(uid);
		} catch (FirebaseAuthException e) {
			return Optional.empty();
		}
	}


}

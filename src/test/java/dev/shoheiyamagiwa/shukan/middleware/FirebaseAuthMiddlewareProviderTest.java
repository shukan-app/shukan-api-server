package dev.shoheiyamagiwa.shukan.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public final class FirebaseAuthMiddlewareProviderTest {
  @Test
  public void testVerifyBearerTokenReturnsUidOnSuccess() {
    FirebaseAuthMiddlewareProvider provider =
        new FirebaseAuthMiddlewareProvider(token -> Optional.of("test-uid"));

    Optional<String> result = provider.verifyBearerToken("valid-token");

    assertEquals(Optional.of("test-uid"), result);
  }

  @Test
  public void testVerifyBearerTokenReturnsEmptyWhenVerifierReturnsEmpty() {
    FirebaseAuthMiddlewareProvider provider =
        new FirebaseAuthMiddlewareProvider(token -> Optional.empty());

    Optional<String> result = provider.verifyBearerToken("invalid-token");

    assertTrue(result.isEmpty());
  }

  @Test
  public void testVerifyBearerTokenReturnsEmptyOnIllegalArgumentException() {
    FirebaseAuthMiddlewareProvider provider =
        new FirebaseAuthMiddlewareProvider(
            token -> {
              throw new IllegalArgumentException("Malformed token");
            });

    Optional<String> result = provider.verifyBearerToken("malformed-token");

    assertTrue(result.isEmpty());
  }

  @Test
  public void testVerifyBearerTokenReturnsEmptyOnUnexpectedRuntimeException() {
    FirebaseAuthMiddlewareProvider provider =
        new FirebaseAuthMiddlewareProvider(
            token -> {
              throw new RuntimeException("Unexpected error");
            });

    Optional<String> result = provider.verifyBearerToken("some-token");

    assertTrue(result.isEmpty());
  }
}

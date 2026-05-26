package dev.shoheiyamagiwa.shukan.middleware;

/**
 * Composite provider used by {@link AuthMiddleware} to verify all required request headers.
 */
public interface AuthMiddlewareProvider extends AuthorizationHeaderProvider {
}

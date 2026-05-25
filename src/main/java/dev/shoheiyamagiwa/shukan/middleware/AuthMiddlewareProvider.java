package dev.shoheiyamagiwa.shukan.middleware;

public interface AuthMiddlewareProvider extends AuthorizationHeaderProvider, FirebaseAppCheckHeaderProvider,
		CloudflareConnectingIpHeaderProvider {
}

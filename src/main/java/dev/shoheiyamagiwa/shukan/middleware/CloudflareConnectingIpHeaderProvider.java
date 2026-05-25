package dev.shoheiyamagiwa.shukan.middleware;

import java.util.Optional;

public interface CloudflareConnectingIpHeaderProvider {
	/**
	 * Verifies and normalizes the client IP address from the {@code CF-Connecting-IP} header.
	 *
	 * @param connectingIp raw Cloudflare connecting IP header value
	 * @return normalized client IP address when accepted, otherwise {@link Optional#empty()}
	 */
	Optional<String> verifyConnectingIp(String connectingIp);
}

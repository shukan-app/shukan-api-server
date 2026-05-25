package dev.shoheiyamagiwa.shukan.middleware;

import java.util.Optional;

public interface CloudflareConnectingIpHeaderProvider {
	Optional<String> verifyConnectingIp(String connectingIp);
}

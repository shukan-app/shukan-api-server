package dev.shoheiyamagiwa.shukan.controller;

import io.javalin.http.HttpStatus;
import io.javalin.router.JavalinDefaultRoutingApi;

public final class HealthController {
	public void registerRoutes(JavalinDefaultRoutingApi routes) {
		routes.get("/health", ctx -> ctx
				.status(HttpStatus.OK)
				.json(new HealthResponseDto("ok")));
	}
}

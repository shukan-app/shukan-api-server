package dev.shoheiyamagiwa.shukan.presentation.controller;

import dev.shoheiyamagiwa.shukan.presentation.dto.HealthResponseDto;
import io.javalin.http.HttpStatus;
import io.javalin.router.JavalinDefaultRoutingApi;

public final class HealthController {
	public void registerRoutes(JavalinDefaultRoutingApi routes) {
		routes.get("/health", ctx -> ctx.status(HttpStatus.OK).json(new HealthResponseDto("ok")));
	}
}

package dev.shoheiyamagiwa.shukan;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MainTest {
	@Test
	public void testHealthEndpoint() throws IOException, InterruptedException {
		Javalin app = Main.createApplication().start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/health");
			
			assertEquals(200, response.statusCode());
			assertEquals("{\"status\":\"ok\"}", response.body());
			assertTrue(response.headers().firstValue("content-type").orElse("").contains("application/json"));
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testNotFoundEndpoint() throws IOException, InterruptedException {
		Javalin app = Main.createApplication().start(0);
		
		try {
			HttpResponse<String> response = sendGet(app, "/missing");
			
			assertEquals(404, response.statusCode());
			assertEquals("{\"message\":\"Not Found\"}", response.body());
			assertTrue(response.headers().firstValue("content-type").orElse("").contains("application/json"));
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testParsePortDefaultsTo7070WhenUnset() {
		assertEquals(7070, Main.parsePort(null));
	}
	
	@Test
	public void testParsePortUsesConfiguredPort() {
		assertEquals(9090, Main.parsePort("9090"));
	}
	
	@Test
	public void testParsePortRejectsInvalidPort() {
		assertThrows(IllegalStateException.class, () -> Main.parsePort("invalid"));
	}
	
	private HttpResponse<String> sendGet(Javalin app, String path) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + app.port() + path))
				.GET()
				.build();
		
		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	}
}

package dev.shoheiyamagiwa.shukan.middleware;

public record AuthenticatedRequestContext(String authId) {
	public static final String ATTRIBUTE_NAME = AuthenticatedRequestContext.class.getName();
}

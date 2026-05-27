package dev.shoheiyamagiwa.shukan.middleware;

public record AuthenticatedRequestContext(String userId) {
	public static final String ATTRIBUTE_NAME = AuthenticatedRequestContext.class.getName();
}

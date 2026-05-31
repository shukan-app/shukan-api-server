package dev.shoheiyamagiwa.shukan.presentation;

import dev.shoheiyamagiwa.shukan.presentation.dto.ErrorResponseDto;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class ErrorResponses {
	private static final String DEFAULT_TYPE = "about:blank";
	private static final String PROBLEM_JSON = "application/problem+json";
	private static final String RESPONDED_ATTRIBUTE = ErrorResponses.class.getName() + ".responded";
	
	private ErrorResponses() {
	}
	
	public static void respond(Context ctx, HttpStatus status, String detail) {
		ctx.attribute(RESPONDED_ATTRIBUTE, true);
		ctx.status(status).json(toDto(ctx, status, detail));
		ctx.contentType(PROBLEM_JSON);
	}
	
	public static boolean hasResponded(Context ctx) {
		return Boolean.TRUE.equals(ctx.attribute(RESPONDED_ATTRIBUTE));
	}
	
	private static ErrorResponseDto toDto(Context ctx, HttpStatus status, String detail) {
		return new ErrorResponseDto(
			DEFAULT_TYPE,
			title(status),
			status.getCode(),
			detail,
			instance(ctx));
	}
	
	private static String instance(Context ctx) {
		return ctx.path();
	}
	
	private static String title(HttpStatus status) {
		if (status == HttpStatus.BAD_REQUEST) {
			return "Bad Request";
		}
		if (status == HttpStatus.UNAUTHORIZED) {
			return "Unauthorized";
		}
		if (status == HttpStatus.FORBIDDEN) {
			return "Forbidden";
		}
		if (status == HttpStatus.NOT_FOUND) {
			return "Not Found";
		}
		if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
			return "Internal Server Error";
		}
		return status.name();
	}
}

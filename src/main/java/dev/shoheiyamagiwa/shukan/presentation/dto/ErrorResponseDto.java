package dev.shoheiyamagiwa.shukan.presentation.dto;

public record ErrorResponseDto(
	String type,
	String title,
	int status,
	String detail,
	String instance) {
}

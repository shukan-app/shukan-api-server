package dev.shoheiyamagiwa.shukan.domain.vo;

public enum TaskCreationSource {
	USER("user", "user"),
	AI("ai", "ai");

	private final String value;
	private final String databaseValue;

	TaskCreationSource(String value, String databaseValue) {
		this.value = value;
		this.databaseValue = databaseValue;
	}

	public static TaskCreationSource fromValue(String value) {
		for (TaskCreationSource source : values()) {
			if (source.value.equals(value)) {
				return source;
			}
		}
		throw new IllegalArgumentException("Unknown TaskCreationSource: " + value);
	}

	public static TaskCreationSource fromDatabaseValue(String value) {
		for (TaskCreationSource source : values()) {
			if (source.databaseValue.equals(value)) {
				return source;
			}
		}
		throw new IllegalArgumentException("Unknown TaskCreationSource: " + value);
	}

	public String getValue() {
		return value;
	}

	public String toDatabaseValue() {
		return databaseValue;
	}
}

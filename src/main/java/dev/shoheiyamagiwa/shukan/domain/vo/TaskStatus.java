package dev.shoheiyamagiwa.shukan.domain.vo;

public enum TaskStatus {
	COMPLETE("complete", "complete"),
	INCOMPLETE("incomplete", "incomplete");
	
	private final String value;
	private final String databaseValue;
	
	TaskStatus(String value, String databaseValue) {
		this.value = value;
		this.databaseValue = databaseValue;
	}
	
	public static TaskStatus fromValue(String value) {
		for (TaskStatus status : values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown TaskStatus: " + value);
	}
	
	public static TaskStatus fromDatabaseValue(String value) {
		for (TaskStatus status : values()) {
			if (status.databaseValue.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown TaskStatus: " + value);
	}
	
	public String getValue() {
		return value;
	}
	
	public String toDatabaseValue() {
		return databaseValue;
	}
}

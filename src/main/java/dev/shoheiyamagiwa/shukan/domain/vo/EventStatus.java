package dev.shoheiyamagiwa.shukan.domain.vo;

public enum EventStatus {
	SCHEDULED("scheduled", "scheduled"),
	ATTENDED("attended", "attended"),
	ABSENT("absent", "absent"),
	CANCELED("canceled", "canceled"),
	RESCHEDULING_REQUIRED("reschedulingRequired", "rescheduling_required");

	private final String value;
	private final String databaseValue;

	EventStatus(String value, String databaseValue) {
		this.value = value;
		this.databaseValue = databaseValue;
	}

	public static EventStatus fromValue(String value) {
		for (EventStatus status : values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown EventStatus: " + value);
	}

	public static EventStatus fromDatabaseValue(String value) {
		for (EventStatus status : values()) {
			if (status.databaseValue.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown EventStatus: " + value);
	}

	public String getValue() {
		return value;
	}

	public String toDatabaseValue() {
		return databaseValue;
	}
}

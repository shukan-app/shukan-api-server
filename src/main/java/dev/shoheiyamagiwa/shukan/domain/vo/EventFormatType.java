package dev.shoheiyamagiwa.shukan.domain.vo;

public enum EventFormatType {
	ONLINE("online", "online"),
	OFFLINE("offline", "offline"),
	HYBRID("hybrid", "hybrid"),
	ON_DEMAND("onDemand", "on_demand");

	private final String value;
	private final String databaseValue;

	EventFormatType(String value, String databaseValue) {
		this.value = value;
		this.databaseValue = databaseValue;
	}

	public static EventFormatType fromValue(String value) {
		for (EventFormatType type : values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown EventFormatType: " + value);
	}

	public static EventFormatType fromDatabaseValue(String value) {
		for (EventFormatType type : values()) {
			if (type.databaseValue.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown EventFormatType: " + value);
	}

	public String getValue() {
		return value;
	}

	public String toDatabaseValue() {
		return databaseValue;
	}
}

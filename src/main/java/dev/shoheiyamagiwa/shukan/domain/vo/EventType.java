package dev.shoheiyamagiwa.shukan.domain.vo;

public enum EventType {
	INFO_SESSION("infoSession", "info_session"),
	INFORMAL_MEET("informalMeet", "informal_meet"),
	INTERVIEW("interview", "interview"),
	GROUP_WORK("groupWork", "group_work"),
	EXPERIENCE_PROGRAM("experienceProgram", "experience_program"),
	ASSESSMENT("assessment", "assessment"),
	OFFER_EVENT("offerEvent", "offer_event"),
	OTHER("other", "other");

	private final String value;
	private final String databaseValue;

	EventType(String value, String databaseValue) {
		this.value = value;
		this.databaseValue = databaseValue;
	}

	public static EventType fromValue(String value) {
		for (EventType type : values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown EventType: " + value);
	}

	public static EventType fromDatabaseValue(String value) {
		for (EventType type : values()) {
			if (type.databaseValue.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown EventType: " + value);
	}

	public String getValue() {
		return value;
	}

	public String toDatabaseValue() {
		return databaseValue;
	}
}

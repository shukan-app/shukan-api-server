package dev.shoheiyamagiwa.shukan.domain.vo;

public enum TaskType {
	DOCUMENT_SUBMISSION("documentSubmission", "document_submission"),
	ASSESSMENT("assessment", "assessment"),
	SCHEDULING("scheduling", "scheduling"),
	REPLY_REQUIRED("replyRequired", "reply_required"),
	PREPARATION("preparation", "preparation"),
	OTHER("other", "other");

	private final String value;
	private final String databaseValue;

	TaskType(String value, String databaseValue) {
		this.value = value;
		this.databaseValue = databaseValue;
	}

	public static TaskType fromValue(String value) {
		for (TaskType type : values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown TaskType: " + value);
	}

	public static TaskType fromDatabaseValue(String value) {
		for (TaskType type : values()) {
			if (type.databaseValue.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown TaskType: " + value);
	}

	public String getValue() {
		return value;
	}

	public String toDatabaseValue() {
		return databaseValue;
	}
}

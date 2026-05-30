package dev.shoheiyamagiwa.shukan.domain.vo;

public enum ContactType {
	EMAIL("email", "email"),
	PHONE("phone", "phone"),
	LINE("line", "line"),
	PLATFORM_MESSAGE("platformMessage", "platform_message"),
	CONTACT_FORM("contactForm", "contact_form"),
	OTHER("other", "other");
	
	private final String value;
	private final String databaseValue;
	
	ContactType(String value, String databaseValue) {
		this.value = value;
		this.databaseValue = databaseValue;
	}
	
	public static ContactType fromValue(String value) {
		for (ContactType type : values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown ContactType: " + value);
	}

	public static ContactType fromDatabaseValue(String value) {
		for (ContactType type : values()) {
			if (type.databaseValue.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown ContactType: " + value);
	}
	
	public String getValue() {
		return value;
	}

	public String toDatabaseValue() {
		return databaseValue;
	}
}

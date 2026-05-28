package dev.shoheiyamagiwa.shukan.domain.vo;

public enum ContactType {
	EMAIL("email"),
	PHONE("phone"),
	LINE("line"),
	PLATFORM_MESSAGE("platformMessage"),
	CONTACT_FORM("contactForm"),
	OTHER("other");
	
	private final String value;
	
	ContactType(String value) {
		this.value = value;
	}
	
	public static ContactType fromValue(String value) {
		for (ContactType type : values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown ContactType: " + value);
	}
	
	public String getValue() {
		return value;
	}
}

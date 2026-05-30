package dev.shoheiyamagiwa.shukan.domain.vo;

public enum CompanyStatus {
	BOOKMARKED("bookmarked", "bookmarked"),
	PREENTRY("preentry", "preentry"),
	INFORMAL_CONTACT("informalContact", "informal_contact"),
	EXPERIENCE_PROGRAM("experienceProgram", "experience_program"),
	PREPARING_APPLICATION("preparingApplication", "preparing_application"),
	UNDER_SCREENING("underScreening", "under_screening"),
	INTERVIEW_SCHEDULING("interviewScheduling", "interview_scheduling"),
	INTERVIEW_IN_PROGRESS("interviewInProgress", "interview_in_progress"),
	FINAL_SELECTION("finalSelection", "final_selection"),
	OFFER_RECEIVED("offerReceived", "offer_received"),
	OFFER_ACCEPTED("offerAccepted", "offer_accepted"),
	OFFER_DECLINED("offerDeclined", "offer_declined"),
	SELECTION_WITHDRAWN("selectionWithdrawn", "selection_withdrawn"),
	REJECTED("rejected", "rejected");
	
	private final String value;
	private final String databaseValue;
	
	CompanyStatus(String value, String databaseValue) {
		this.value = value;
		this.databaseValue = databaseValue;
	}
	
	public static CompanyStatus fromValue(String value) {
		for (CompanyStatus status : values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown CompanyStatus: " + value);
	}

	public static CompanyStatus fromDatabaseValue(String value) {
		for (CompanyStatus status : values()) {
			if (status.databaseValue.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown CompanyStatus: " + value);
	}
	
	public String getValue() {
		return value;
	}

	public String toDatabaseValue() {
		return databaseValue;
	}
}

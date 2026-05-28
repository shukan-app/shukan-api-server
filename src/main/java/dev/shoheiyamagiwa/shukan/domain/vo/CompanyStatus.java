package dev.shoheiyamagiwa.shukan.domain.vo;

public enum CompanyStatus {
	BOOKMARKED("bookmarked"),
	PREENTRY("preentry"),
	INFORMAL_CONTACT("informalContact"),
	EXPERIENCE_PROGRAM("experienceProgram"),
	PREPARING_APPLICATION("preparingApplication"),
	UNDER_SCREENING("underScreening"),
	INTERVIEW_SCHEDULING("interviewScheduling"),
	INTERVIEW_IN_PROGRESS("interviewInProgress"),
	FINAL_SELECTION("finalSelection"),
	OFFER_RECEIVED("offerReceived"),
	OFFER_ACCEPTED("offerAccepted"),
	OFFER_DECLINED("offerDeclined"),
	SELECTION_WITHDRAWN("selectionWithdrawn"),
	REJECTED("rejected");
	
	private final String value;
	
	CompanyStatus(String value) {
		this.value = value;
	}
	
	public static CompanyStatus fromValue(String value) {
		for (CompanyStatus status : values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown CompanyStatus: " + value);
	}
	
	public String getValue() {
		return value;
	}
}

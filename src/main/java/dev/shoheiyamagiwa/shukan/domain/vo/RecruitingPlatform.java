package dev.shoheiyamagiwa.shukan.domain.vo;

public enum RecruitingPlatform {
	MYNAVI("mynavi", "mynavi"),
	RIKUNABI("rikunabi", "rikunabi"),
	ONE_CAREER("oneCareer", "one_career"),
	GAISHI_SHUKATSU("gaishiShukatsu", "gaishi_shukatsu"),
	OFFER_BOX("offerBox", "offer_box"),
	KIMISUKA("kimisuka", "kimisuka"),
	DODA_CAMPUS("dodaCampus", "doda_campus"),
	IROOTS("iroots", "iroots"),
	SUPPORTERZ("supporterz", "supporterz"),
	PAIZA("paiza", "paiza"),
	LEVTECH("levtech", "levtech"),
	TRACK_JOB("trackJob", "track_job"),
	WANTEDLY("wantedly", "wantedly"),
	DIRECT("direct", "direct"),
	AGENT("agent", "agent"),
	REFERRAL("referral", "referral"),
	OTHER("other", "other");
	
	private final String value;
	private final String databaseValue;
	
	RecruitingPlatform(String value, String databaseValue) {
		this.value = value;
		this.databaseValue = databaseValue;
	}
	
	public static RecruitingPlatform fromValue(String value) {
		for (RecruitingPlatform platform : values()) {
			if (platform.value.equals(value)) {
				return platform;
			}
		}
		throw new IllegalArgumentException("Unknown RecruitingPlatform: " + value);
	}

	public static RecruitingPlatform fromDatabaseValue(String value) {
		for (RecruitingPlatform platform : values()) {
			if (platform.databaseValue.equals(value)) {
				return platform;
			}
		}
		throw new IllegalArgumentException("Unknown RecruitingPlatform: " + value);
	}
	
	public String getValue() {
		return value;
	}

	public String toDatabaseValue() {
		return databaseValue;
	}
}

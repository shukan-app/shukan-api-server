package dev.shoheiyamagiwa.shukan.domain.vo;

public enum RecruitingPlatform {
	MYNAVI("mynavi"),
	RIKUNABI("rikunabi"),
	ONE_CAREER("oneCareer"),
	GAISHI_SHUKATSU("gaishiShukatsu"),
	OFFER_BOX("offerBox"),
	KIMISUKA("kimisuka"),
	DODA_CAMPUS("dodaCampus"),
	IROOTS("iroots"),
	SUPPORTERZ("supporterz"),
	PAIZA("paiza"),
	LEVTECH("levtech"),
	TRACK_JOB("trackJob"),
	WANTEDLY("wantedly"),
	DIRECT("direct"),
	AGENT("agent"),
	REFERRAL("referral"),
	OTHER("other");
	
	private final String value;
	
	RecruitingPlatform(String value) {
		this.value = value;
	}
	
	public static RecruitingPlatform fromValue(String value) {
		for (RecruitingPlatform platform : values()) {
			if (platform.value.equals(value)) {
				return platform;
			}
		}
		throw new IllegalArgumentException("Unknown RecruitingPlatform: " + value);
	}
	
	public String getValue() {
		return value;
	}
}

package dev.shoheiyamagiwa.shukan.domain.vo;

public enum RecrutingPlatform {
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

  RecrutingPlatform(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static RecrutingPlatform fromValue(String value) {
    for (RecrutingPlatform platform : values()) {
      if (platform.value.equals(value)) {
        return platform;
      }
    }
    throw new IllegalArgumentException("Unknown RecrutingPlatform: " + value);
  }
}

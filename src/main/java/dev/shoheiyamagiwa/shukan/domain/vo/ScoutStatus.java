package dev.shoheiyamagiwa.shukan.domain.vo;

public enum ScoutStatus {
  UNREAD("unread", "unread"),
  UNRESPONDED("unresponded", "unresponded"),
  ACCEPTED("accepted", "accepted"),
  DECLINED("declined", "declined");

  private final String value;
  private final String databaseValue;

  ScoutStatus(String value, String databaseValue) {
    this.value = value;
    this.databaseValue = databaseValue;
  }

  public static ScoutStatus fromValue(String value) {
    for (ScoutStatus status : values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown ScoutStatus: " + value);
  }

  public static ScoutStatus fromDatabaseValue(String value) {
    for (ScoutStatus status : values()) {
      if (status.databaseValue.equals(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown ScoutStatus: " + value);
  }

  public String getValue() {
    return value;
  }

  public String toDatabaseValue() {
    return databaseValue;
  }
}

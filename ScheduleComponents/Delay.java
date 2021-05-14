package ScheduleComponents;

public class Delay {

  public String origin;
  public String reason;
  public boolean resolved;

  public Delay(String origin, String reason) {
    this.origin = origin;
    this.reason = reason;
  }

  public void resolve() {
    this.resolved = true;
  }
}

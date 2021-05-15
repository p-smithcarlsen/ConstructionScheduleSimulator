package ScheduleComponents;

public class Alarm {

  public int day;
  public Type type;
  public String origin;
  public String reason;
  public boolean resolved;

  public enum Type {
    workerSick,
    badWeather,
    taskDifficult,
    delayedMaterials
  }

  public Alarm(int day, Type type, String origin, String reason) {
    this.day = day;
    this.type = type;
    this.origin = origin;
    this.reason = reason;
    System.out.println(this);
  }

  public void resolve() {
    this.resolved = true;
  }

  public String toString() {
    return "day " + day + " - " + origin + ": " + reason + " (resolved=" + resolved + ")";
  }
}
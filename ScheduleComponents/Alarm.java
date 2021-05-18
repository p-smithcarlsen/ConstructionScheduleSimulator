package ScheduleComponents;

public class Alarm {

  public Task task;
  public int day;
  // public Type type;
  public String trade;
  public String reason;
  public boolean resolved;

  // public enum Type {
  //   workerSick,
  //   badWeather,
  //   taskDifficult,
  //   delayedMaterials,
  //   taskDelayed
  // }

  public Alarm(int day, Task task, String trade, String reason) {
    this.day = day;
    this.task = task;
    this.trade = trade;
    this.reason = reason;
    System.out.println(this);
  }

  public void resolve() {
    this.resolved = true;
  }

  public String toString() {
    return "day " + day + " - " + trade + ": " + reason + " (resolved=" + resolved + ")";
  }
}
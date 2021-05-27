package ScheduleComponents;

import ScheduleComponents.Contractor.Trade;

public class Alarm {

  public Task task;
  public int day;
  public Trade trade;
  public String reason;
  public boolean resolved;

  public Alarm(int day, Task task, Trade trade, String reason) {
    this.day = day;
    this.task = task;
    this.trade = trade;
    this.reason = reason;
  }

  /**
   * Turns the alarm into 'resolved', meaning we do not have to
   * consider it a risk anymore.
   */
  public void resolve() {
    this.resolved = true;
  }

  public String toString() {
    return "day " + day + " - " + trade + ": " + reason;
  }
}
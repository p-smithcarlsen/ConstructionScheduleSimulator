package ScheduleComponents;

import java.util.Arrays;

public class Task {
  public String id;
  public String location;
  public String activity;
  public String trade;
  public double quantity;
  public String[] dependencies;
  public int progress;

  public Task(String[] taskParameters) {
    this.id = taskParameters[0];
    this.location = taskParameters[1];
    this.activity = taskParameters[2];
    this.trade = taskParameters[3];
    taskParameters[4] = taskParameters[4].replaceAll(",", ".");
    this.quantity = Double.parseDouble(taskParameters[4]);
    this.dependencies = taskParameters[5].split(",");
  }

  public void work(int workers) {
    progress = (int)Math.min(progress + (workers / quantity) * 100, 100);
    System.out.println(String.format("Progress: %03d percent (%s of %s) of task %s (%s) in location %s%n", progress, workers, quantity, id, activity, location));
  }

  public boolean isFinished() {
    return progress >= 100;
  }

  public void print() {
    System.out.println(String.format(" --Task %s: %s, trade: %s, quantity: %s, dependency: %s", id, activity, trade, quantity, Arrays.deepToString(dependencies)));
  }
}

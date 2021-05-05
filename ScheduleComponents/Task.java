package ScheduleComponents;

import java.util.Arrays;

public class Task {
  public String id;
  public String location;
  public String activity;
  // public boolean criticality;
  public String trade;
  public int optimalCrew;
  // public int maxCrew;
  public int meanDuration;            // Not part of taskParameters
  public double standardDeviation;    // Not part of taskParameters
  public double quantity;
  public int productionRate;
  public String[] dependencies;
  public int progress;                // Not part of taskParameters

  public int earliestStart;
  public int earliestFinish;
  public int latestStart;
  public int latestFinish;
  public int maximumTime;

  public Task(String[] taskParameters) {
    createMetadata(taskParameters);
  }

  public void createMetadata(String[] taskParameters) {
    this.id = taskParameters[0];
    this.location = taskParameters[1];
    this.activity = taskParameters[2];
    this.trade = taskParameters[3];
    this.optimalCrew = Integer.parseInt(taskParameters[4]);
    taskParameters[5] = taskParameters[5].replaceAll(",", ".");
    this.quantity = Double.parseDouble(taskParameters[5]);
    this.productionRate = Integer.parseInt(taskParameters[6]);
    this.dependencies = taskParameters[7].split(",");
    
    // Calculate expected duration
    this.meanDuration = (int)Math.ceil(this.quantity / this.productionRate);
    System.out.println(this.id + ": " + "duration = " + this.meanDuration);
    this.standardDeviation = 0.25;
  }

  public void calculateEarliestTimings(int lastTaskFinished) {
    this.earliestStart = lastTaskFinished;
    this.earliestFinish = lastTaskFinished + meanDuration;
  }

  public void calculateLatestTimings(int deadline) {
    this.latestFinish = deadline;
    this.latestStart = deadline - meanDuration;
  }

  public void work(int workers) {
    progress = (int)Math.min(progress + (workers / quantity) * 100, 100);
    System.out.println(String.format("Progress: %03d percent (%s of %s) of task %s (%s) in location %s%n", progress, workers, quantity, id, activity, location));
  } // Move to workforce? - still need something to change progress

  public boolean isFinished() {
    return progress >= 100;
  }

  public void print() {
    System.out.println(String.format(" --Task %s: %s, trade: %s, quantity: %s, dependency: %s", id, activity, trade, quantity, Arrays.deepToString(dependencies)));
  }
}

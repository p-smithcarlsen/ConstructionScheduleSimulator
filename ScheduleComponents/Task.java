package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class Task {
  public String id;
  public String location;
  public String activity;
  // public boolean criticality;
  public String trade;
  public int optimalWorkerCount;
  public int workersAssigned;         // Not part of taskParameters
  // public int maxCrew;
  public int meanDuration;            // Not part of taskParameters
  public double standardDeviation;    // Not part of taskParameters
  public double quantity;
  public int productionRate;
  public String dependencies;
  public List<Task> predecessorTasks = new ArrayList<>();
  public int progress;                // Not part of taskParameters

  public int earliestStart;
  public int earliestFinish;
  public int latestStart;
  public int latestFinish;
  // public int maximumTime;

  public Task(String[] taskParameters) {
    createMetadata(taskParameters);
  }

  public void createMetadata(String[] taskParameters) {
    this.id = taskParameters[0];
    this.location = taskParameters[1];
    this.activity = taskParameters[2];
    this.trade = taskParameters[3];
    this.optimalWorkerCount = Integer.parseInt(taskParameters[4]);
    taskParameters[5] = taskParameters[5].replaceAll(",", ".");
    this.quantity = Double.parseDouble(taskParameters[5]);
    this.productionRate = Integer.parseInt(taskParameters[6]);
    this.dependencies = taskParameters[7];
    
    // Calculate expected duration
    this.meanDuration = (int)Math.ceil(this.quantity / this.productionRate);
    // System.out.println(this.id + ": " + "duration = " + this.meanDuration);
    this.standardDeviation = 0.25;
  }

  public String getDependencies() {
    return this.dependencies;
  }

  public void addPredecessor(Task t) {
    predecessorTasks.add(t);
  }

  public void calculateEarliestTimings(int lastTaskFinished) {
    this.earliestStart = lastTaskFinished;
    this.earliestFinish = lastTaskFinished + meanDuration;
  }

  public void calculateLatestTimings(int deadline) {
    this.latestFinish = deadline;
    this.latestStart = deadline - meanDuration;
    // Todo: calculate maximum time and criticality?
  }

  public void assignWorkers(int workers) {
    this.workersAssigned = workers;
  }

  public void work() {
    // An optimal worker count would increase the progress by (quantity / productionRate)
    // If workersAssigned is lower, the progress will increase slower (optimalWorkerCount / workersAssigned)
    // Providing more workers than the optimal crew size will contribute less and less
    if (workersAssigned == 0) return;

    double workerContribution = workersAssigned >= optimalWorkerCount ? 1 : (optimalWorkerCount / workersAssigned);
    if (workersAssigned > optimalWorkerCount) workerContribution += (workersAssigned - optimalWorkerCount) * 0.5;

    this.progress += (workerContribution * productionRate) / quantity * 100;
    System.out.println(String.format("Progress: % 3d percent (%s of %s) of task %s (%s) in location %s", progress, workersAssigned, quantity, id, activity, location));

    // Reset workers
    this.workersAssigned = 0;
  }

  public boolean canBeStarted() {
    boolean canBeStarted = true;
    for (Task t : predecessorTasks) {
      if (!t.isFinished()) {
        canBeStarted = false;
        break;
      }
    }

    return canBeStarted;
  }

  // public void work(int workers) {
  //   progress = (int)Math.min(progress + (workers / quantity) * 100, 100);
  //   System.out.println(String.format("Progress: %03d percent (%s of %s) of task %s (%s) in location %s%n", progress, workers, quantity, id, activity, location));
  // } // Move to workforce? - still need something to change progress

  public boolean isFinished() {
    return progress >= 100;
  }

  public void print() {
    System.out.println(String.format(" --Task %s: %s, trade: %s, quantity: %s, dependency: %s", id, activity, trade, quantity, dependencies));
  }
}
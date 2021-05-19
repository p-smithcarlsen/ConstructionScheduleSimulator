package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class Task {
  // Metadata and type
  public String id;
  public String location;
  public String activity;
  public String trade;
  
  // Duration and progress
  public double quantity;
  public int optimalWorkerCount;
  public int meanDuration;            // Not part of taskParameters
  public double standardDeviation;    // Not part of taskParameters
  public int workersAssigned;         // Not part of taskParameters
  public int productionRate;
  public double progress;                // Not part of taskParameters

  // Dependencies and path duration
  public String dependencies;
  public List<Task> predecessorTasks = new ArrayList<>();
  public List<Task> successorTasks = new ArrayList<>();
  // public double longestPathDuration;   // Not used

  // Task timings and criticality
  public boolean isCritical;
  public int earliestStart = 0;
  public int earliestFinish = 0;
  public int latestStart = Integer.MAX_VALUE;
  public int latestFinish = Integer.MAX_VALUE;
  public int maximumTime;
  public int taskFloat;

  public Task(String[] taskParameters) {
    saveMetadata(taskParameters);
  }

  /**
   * Sets the metadata of this task based on the taskParameters input.
   * @param taskParameters is the string array of task metadata
   */
  public void saveMetadata(String[] taskParameters) {
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
    // this.standardDeviation = 0.25;
  }

  public String getDependencies() {
    return this.dependencies;
  }

  /**
   * Adds a task to the predecessor list.
   * @param t
   */
  public void addPredecessor(Task t) {
    predecessorTasks.add(t);
  }

  /**
   * Adds a task to the successor list.
   * @param t
   */
  public void addSuccessor(Task t) {
    successorTasks.add(t);
  }

  /**
   * Sets the earliestStart and earliestFinish variables based on
   * the previous task.
   * @param lastTaskFinished
   */
  public void calculateEarliestTimings(int lastTaskFinished) {
    this.earliestStart = lastTaskFinished;
    this.earliestFinish = lastTaskFinished + meanDuration - 1; // tasks cant stop and start at the same time unit
  }

  /**
   * Sets the latestStart and latestFinish variables based on the
   * subsequent tasks. 
   * @param deadline
   */
  public void calculateLatestTimings(int deadline) {
    this.latestFinish = deadline;
    this.latestStart = deadline - meanDuration;
  }

  /**
   * Assigns a number of workers to this task. Note that work is not 
   * performed yet, thus the progress is not incremented in this method.
   * Contractors will attempt to assign the optimal worker count.
   * @param workers
   */
  public void assignWorkers(int workers) {
    this.workersAssigned = workers;
  }

  /**
   * Works the task, thus incrementing the progress of this task. Assigning
   * the optimal worker count will increment the progress by the productionRate,
   * and assigning a higher number of workers will mean a diminishing factor of 
   * production (reflected in the workerContribution variable). After working the
   * task, the number of workers assigned is reset to 0.
   */
  public void work() {
    // An optimal worker count would increase the progress by (quantity / productionRate)
    // Providing more workers than the optimal crew size will contribute less and less
    if (workersAssigned == 0) { return; }

    this.progress += transformWorkersToProgress(workersAssigned);
    if (this.progress > 100) this.progress = 100;
    System.out.println(String.format("%12s has finished %6.1f%% of %s%s %s", trade.substring(0,Math.min(12, trade.length())), progress, location, id, isCritical ? "(Critical)" : ""));

    // Reset workers
    this.workersAssigned = 0;

    // TODO: Print out whether task has been delayed
  }

  /**
   * 
   * @param workers
   * @return
   */
  private double transformWorkersToProgress(int workers) {
    // If workersAssigned is equal or higher than optimalWorkerCount, contribution 
    // is equal or higher than productionRate. Assigning more workers than optimal
    // amount will provide less (half) productionRate per worker
    double workerContribution = workers >= optimalWorkerCount ? 1 : ((double)workersAssigned / (double)optimalWorkerCount);
    if (workersAssigned > optimalWorkerCount) workerContribution += (workersAssigned - optimalWorkerCount) / optimalWorkerCount * 0.5;
    double progress = (workerContribution * productionRate) / quantity * 100;
    return progress;
  }

  /**
   * This function is called when an alarm is encountered due to a delay.
   * Based on the remaining quantity, it calculates the remaining duration.
   */
  public void recalculateDuration() {
    double remainingQuantity = (100 - progress) * quantity / 100;
    if (Math.abs(remainingQuantity % 1) < 0.000001) remainingQuantity = Math.round(remainingQuantity);
    this.meanDuration = (int)Math.ceil(remainingQuantity / this.productionRate);
  }

  /**
   * Used to determine whether all predecessor tasks are finished.
   * @return a boolean variable, indicating whether all predecessor tasks are finished
   */
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

  /**
   * Used to determine whether this task i finished and thus no longer active.
   * @return a boolean variable, indicating whether this task is finished
   */
  public boolean isFinished() {
    return progress >= 99.999;  // Taking into account presicion of float values
  }

  /**
   * Prints some metadata and the early/late start/finish variables.
   */
  public void print(int level) {
    // System.out.println(String.format(" --Task %s: %s, trade: %s, quantity: %s, dependency: %s", id, activity, trade, quantity, dependencies));
    // System.out.print(String.format(" --%s%s (dur: % 2d), dep: %s", location, id, meanDuration, dependencies));
    // System.out.print(" pred: ");
    // for (Task t : predecessorTasks) System.out.print(t.location + "" + t.id + " ");
    // System.out.print("succ:");
    // for (Task t : successorTasks) System.out.print(t.location + "" + t.id + " ");
    // System.out.println();
    // System.out.println(String.format("   ES: %s, EF: %s, LS: %s, LF: %s", earliestStart, earliestFinish, latestStart, latestFinish));
    // System.out.println();

    System.out.printf(" ".repeat(level*2-1) + "|" + "-" + "%s%s" + " ".repeat(20-level*2) + "%s (d=%02d, es=%02d, ef=%02d, ls=%02d, lf=%02d)   [ ", location, id, isCritical ? "(C)" : "   ", meanDuration, earliestStart, earliestFinish, latestStart, latestFinish);
    for (Task t : predecessorTasks) System.out.print(t.location + "" + t.id + " ");
    System.out.printf("]  [ ");
    for (Task t : successorTasks) System.out.print(t.location + "" + t.id + " ");
    System.out.printf("]%n");
  }

  /**
   * Prints some metadata for this task and all tasks depending on this task. 
   */
  public void printWithDependencies(int[][] adj, int level) {
    if (adj[Integer.parseInt("" + location.charAt(1))][Integer.parseInt("" + id.charAt(1))] == 1) return;
    if (!isFinished()) {
      print(level);
    } else {
      System.out.println();
    }
    adj[Integer.parseInt("" + location.charAt(1))][Integer.parseInt("" + id.charAt(1))] = 1;
    for (Task t : successorTasks) {
      t.printWithDependencies(adj, level + 1);
    }
  }
}

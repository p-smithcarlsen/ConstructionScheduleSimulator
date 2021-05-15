package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class Task {
  public String id;
  public String location;
  public String activity;
  public boolean isCritical;
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
  public List<Task> successorTasks = new ArrayList<>();
  public double longestPathDuration;

  public double progress;                // Not part of taskParameters

  public int earliestStart;
  public int earliestFinish;
  public int latestStart = Integer.MAX_VALUE;
  public int latestFinish = Integer.MAX_VALUE;
  public int maximumTime;
  public int taskFloat;

  public Task(String[] taskParameters) {
    createMetadata(taskParameters);
  }

  /**
   * Sets the metadata of this task based on the taskParameters input.
   * @param taskParameters is the string array of task metadata
   */
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
   * Iterates over all tasks in the successor list and their successor 
   * tasks. Finds the longest duration of tasks depending on this task. 
   * Also sets the longestPathDuration variable to this longest duration.
   * @return the path of tasks depending on this task with the longest duration.
   */
  public List<Task> checkLengthOfPath() {
    List<Task> longestPath = new ArrayList<>();

    for (Task t : successorTasks) {
      List<Task> path = t.checkLengthOfPath();
      
      if (path.get(path.size()-1).longestPathDuration > longestPathDuration) {
        longestPath = path;
        longestPathDuration = longestPath.get(longestPath.size()-1).longestPathDuration;
      }
    }
    
    longestPath.add(this);
    longestPathDuration += meanDuration;
    return longestPath;
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
    // Todo: calculate maximum time and criticality?
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
    // If workersAssigned is lower, the progress will increase slower (optimalWorkerCount / workersAssigned)
    // Providing more workers than the optimal crew size will contribute less and less
    if (workersAssigned == 0) {
      // Not true - should be started next day actually (just disregard for now but dont delete)
      // if (canBeStarted() && !isFinished()) System.out.println(trade + " not supplying workers to " + location + id + " despite task can be started");
      return;
    }

    //double workerContribution = workersAssigned >= optimalWorkerCount ? 1 : (optimalWorkerCount / workersAssigned);
    double workerContribution = workersAssigned >= optimalWorkerCount ? 1 : ((double)workersAssigned / (double)optimalWorkerCount);
    // vil workesrAssigned og optimalworkercount som maks være =?
    // har byttet rundt på workersassigned og optimalworkercount, da det var sat op forkert og incase der var færre end det optimale, så skal det give mindre en 1
    //if (workersAssigned > optimalWorkerCount) workerContribution += (workersAssigned - optimalWorkerCount) * 0.5;
    // kan der på nogen måde være assignet flere end den optimale mængde????
    // hvad er formålet med dette if? at reducere contribution fra workers assigned over optimalen?

    this.progress += (workerContribution * productionRate) / quantity * 100;
    if (this.progress > 100) this.progress = 100;
    // do the work and update the progress
    // System.out.println(String.format("Progress: % 2.2f percent (%s of %s) of task %s (%s) in location %s", progress, workerContribution*productionRate, quantity, id, activity, location));
    System.out.println(String.format("%12s has finished %6.1f%% of %s%s", trade.substring(0,Math.min(12, trade.length())), progress, location, id));
    // Reset workers
    this.workersAssigned = 0;

    // TODO: Print out whether task has been delayed
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
  public void print() {
    // System.out.println(String.format(" --Task %s: %s, trade: %s, quantity: %s, dependency: %s", id, activity, trade, quantity, dependencies));
    System.out.print(String.format(" --%s%s (dur: % 2d), dep: %s", location, id, meanDuration, dependencies));
    System.out.print(" pred: ");
    for (Task t : predecessorTasks) System.out.print(t.location + "" + t.id + " ");
    System.out.print("succ:");
    for (Task t : successorTasks) System.out.print(t.location + "" + t.id + " ");
    System.out.println();
    System.out.println(String.format("   ES: %s, EF: %s, LS: %s, LF: %s", earliestStart, earliestFinish, latestStart, latestFinish));
    System.out.println();
  }

  /**
   * Prints some metadata for this task and all tasks depending on this task. 
   */
  public void printWithDependencies() {
    print();
    for (Task t : successorTasks) {
      t.printWithDependencies();
    }
  }
}

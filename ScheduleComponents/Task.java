package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class Task {
  // Metadata and type
  public int id;
  public int location;
  public String activity;
  public String trade;
  
  // Duration and progress
  public double quantity;
  public int optimalWorkerCount;
  public int meanDuration;
  public double standardDeviation;
  public int workersAssigned;
  public int productionRate;
  public double progress;

  // Dependencies and path duration
  public String dependencies;
  public List<Task> predecessorTasks = new ArrayList<>();
  public List<Task> successorTasks = new ArrayList<>();

  // Task timings and criticality
  public boolean isCritical;
  public int earliestStart = 0;
  public int earliestFinish = 0;
  public int latestStart = Integer.MAX_VALUE;
  public int latestFinish = Integer.MAX_VALUE;
  public int scheduledStart;
  public int scheduledFinish;
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
    this.id = Integer.parseInt(""+taskParameters[0].charAt(1));
    this.location = Integer.parseInt(""+taskParameters[1].charAt(1));
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
   * Assigns a number of workers to this task. Note that work is not 
   * performed yet, thus the progress is not incremented in this method.
   * Contractors will attempt to assign the optimal worker count.
   * @param workers
   */
  public void assignWorkers(int workers) {
    this.workersAssigned += workers;
  }

  /**
   * Works the task, thus incrementing the progress of this task. Assigning
   * the optimal worker count will increment the progress by the productionRate,
   * and assigning a higher number of workers will mean a diminishing factor of 
   * production (reflected in the workerContribution variable). After working the
   * task, the number of workers assigned is reset to 0.
   */
  public void work() {
    if (workersAssigned == 0) { return; }
    this.progress += transformWorkersToProgress(workersAssigned);
    if (this.progress > 100) this.progress = 100;
    System.out.println(String.format("%12s has finished %6.1f%% of L%sT%s %s", 
      trade.substring(0,Math.min(12, trade.length())), progress, location, id, isCritical ? "(Critical)" : ""));

    this.workersAssigned = 0;
  }

  /**
   * Takes an amount of workers and determines the amount of progress that this
   * translates into. If workersAssigned is equal or higher than optimalWorkerCount, 
   * contribution is equal or higher than productionRate. So far, a worker 
   * contributes a static amount of production and does not stagnate with higher
   * numbers of workers. 
   * @param workers
   * @return
   */
  public double transformWorkersToProgress(int workers) {
    double contributionPerWorker = (double)productionRate / (double)optimalWorkerCount;
    double workerContribution = workers * contributionPerWorker;
    double progress = workerContribution / quantity * 100;
    return progress;
  }

  /**
   * This function is called when an alarm is encountered due to a delay.
   * Based on the remaining quantity, it calculates the remaining duration.
   */
  public void recalculateDuration() {
    double remainingQuantity = getRemainingQuantity();
    this.meanDuration = (int)Math.ceil(remainingQuantity / this.productionRate);
  }

  /**
   * Returns the amount of quantity still remaining to be completed, taking
   * into account precision of floating point numbers. 
   * @return
   */
  public double getRemainingQuantity() {
    double remainingQuantity = (100 - progress) * quantity / 100;
    if (Math.abs(remainingQuantity % 1) < 0.000001) remainingQuantity = Math.round(remainingQuantity);
    return remainingQuantity;
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
   * The function takes account of precision of floating point values by returning
   * true if the progress is higher than 99.999.
   * @return a boolean variable, indicating whether this task is finished
   */
  public boolean isFinished() {
    return progress >= 99.999;  // Taking into account precision of float values
  }

  public void print(int level) {
    System.out.printf(" ".repeat(level*2-1) + "|" + "-" + "L%sT%s" + " ".repeat(20-level*2) + 
      "%s (d=%02d, es=%02d, ef=%02d, ls=%02d, lf=%02d)   ", 
      location, id, isCritical ? "(C)" : "   ", meanDuration, 
      earliestStart, earliestFinish, latestStart, latestFinish);
    String tradeSubstring = trade.substring(0, Math.min(trade.length(), 15));
    System.out.printf("%-15s  [ ", tradeSubstring);
    for (Task t : predecessorTasks) System.out.print("L" + t.location + "T" + t.id + " ");
    System.out.printf("]  [ ");
    for (Task t : successorTasks) System.out.print("L" + t.location + "T" + t.id + " ");
    System.out.printf("]%n");
  }

  public void printWithDependencies(int[][] adj, int level) {
    if (adj[location][id] == 1) return;
    if (!isFinished()) {
      print(level);
    } else {
      System.out.println();
    }
    adj[location][id] = 1;
    for (Task t : successorTasks) {
      t.printWithDependencies(adj, level + 1);
    }
  }
}

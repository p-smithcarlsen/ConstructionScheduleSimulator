package ScheduleComponents;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Contractor {
  public String id;
  public String name;
  public String trade;
  public Random r = new Random();
  public double[] workerDemand;
  public int scheduleLength;
  public List<Task> scheduledTasks;
  public int availableWorkers;

  public Contractor(String id, String trade) {
    this.id = id;
    this.trade = trade;
  }

  /**
   * Iterates over all the tasks assigned to this contractor and calculates
   * when workers are needed. This information will be inserted into an array
   * of ints, where each entry denotes the amount of workers needed on that day
   * @param tasks is the list of tasks assigned to this contractor
   */
  public void calculateWorkerDemand(List<Task> tasks) {
    Collections.sort(tasks, new SortByEarliestStart());
    for (Task t : tasks)
      if (t.latestFinish > scheduleLength) scheduleLength = t.latestFinish;

    this.scheduledTasks = tasks;
    this.workerDemand = new double[scheduleLength+1];
    for (Task t : tasks) {
      // t.print();
      for (int i = t.earliestStart; i < t.earliestFinish; i++) {
        workerDemand[i] += t.optimalWorkerCount;
      }
    }
  }

  /**
   * Iterates over the contractor's tasks and assigns workers to the
   * active ones. A task is active if it is not finished and if all
   * predecessor tasks are finished.
   * @param today is the day of work
   */
  public void assignWorkers(int today, DelayManager delays) {
    if (today >= workerDemand.length) return;
    availableWorkers = (int)workerDemand[today]; // TODO: safe from double to int?
    availableWorkers = checkForSickWorkers(availableWorkers, delays);
    int w = 0;
    // TODO: prioritize critical tasks

    for (Task t : scheduledTasks) {
      // makes sure a) the task is not finished; b) all predecessor tasks are finished
      if (!t.isFinished() && t.canBeStarted()) {  
        w = Math.min(t.optimalWorkerCount, availableWorkers);
        // assigner enten det optimale antal workers, eller det mulige antal.
        t.assignWorkers(w); 
        // System.out.println(trade + " providing " + w + " of " + workerDemand[today] + " workers to task " + t.location + t.id);
        // System.out.println(String.format("Contractor %s providing %s workers of %s available", id, w, availableWorkers));
        availableWorkers -= w; // sørger for, de samme workers ikke kan assignes twice, samme dag. 
      }
    }
  }

  /**
   * Simulate workers being sick (each worker has a 3 sick days out of 200)
   */
  private int checkForSickWorkers(int scheduledWorkers, DelayManager delays) {
    int sickWorkers = 0;
    for (int i = 0; i < scheduledWorkers; i++) {
      if (r.nextInt(200) < 3) {
        sickWorkers++;
      }
    }
    if (sickWorkers > 0) {
      delays.addDelays(new Delay("Contractor " + id, sickWorkers + " workers are sick"));
      System.out.println(String.format("%s has %d sick workers!", trade, sickWorkers));
    }
    return scheduledWorkers - sickWorkers;
  }

  public void forecastWorkerSchedule(int day) {
    // New strategy: Get overview of workers available (workerDemand)
    // Go through tasks, starting with critical ones
    // Determine new estimated finish for each
    // Find out, whether this causes delays
      // Specifically whether a new critical path is now found


    // Walk through tasks, finding latest finishes
    // Determine new worker demand
    // Compare with worker supply
    // double[] newWorkerDemand = new double[scheduleLength+1];
    // for (Task t : scheduledTasks) {
    //   if (t.isFinished()) continue;
    //   // Find the remaining quantity (work) for the task
    //   double remainingQuantity = (1 - (t.progress / 100)) * t.quantity;
    //   // Find the contribution of a single worker
    //   double contributionPerWorker = t.productionRate / t.optimalWorkerCount;
    //   // Determine the number of workers needed to work to complete task
    //   double workersNeeded = remainingQuantity / contributionPerWorker;

    //   double workersSupplied = 0;
    //   double workersAvailable = 0;
    //   for (int i = day; i < t.latestFinish; i++) {
        
    //   }







    //   // Find average production needed per day
    //   double neededProductionRate = remainingQuantity / (t.latestFinish - day);
    //   // Find the average worker supply needed per day
    //   double neededCrew = (neededProductionRate / t.productionRate) * t.optimalWorkerCount;
    //   for (int i = day; i < t.latestFinish; i++) {
    //     newWorkerDemand[i] += neededCrew;
    //   }
    // }

    // double hiredWorkers = 0;
    // double neededWorkers = 0;
    // for (int i = day; i < scheduleLength; i++) {
    //   hiredWorkers += workerDemand[i];
    //   neededWorkers += newWorkerDemand[i];
    //   System.out.println("Day " + i + ": Hired = " + hiredWorkers + " | Needed = " + neededWorkers);
    // }
  }

  /**
   * Used to sort tasks ascendingly by earliest finish time
   */
  private class SortByEarliestStart implements Comparator<Task> {
    @Override
    public int compare(Task t1, Task t2) {
      return t1.earliestStart - t2.earliestStart;
    }
  }
}

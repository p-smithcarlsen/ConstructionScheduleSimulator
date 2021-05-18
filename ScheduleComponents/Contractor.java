package ScheduleComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Contractor {
  public String id;
  public String name;
  public String trade;
  public Random r = new Random();
  public int[] workerDemand;
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
  public int[] calculateWorkerDemand(List<Task> tasks) {
    Collections.sort(tasks, new SortByEarliestStart());
    for (Task t : tasks)
      if (t.latestFinish >= scheduleLength) scheduleLength = t.latestFinish+1;

    this.scheduledTasks = tasks;
    this.workerDemand = new int[scheduleLength];
    for (Task t : tasks) {
      if (t.isFinished()) continue;
      double remainingQuantity = t.quantity * (100 - t.progress) / 100;
      double productionRate = t.productionRate;
      int sufficientWorkers = 0;
      for (int i = t.earliestStart; i < t.earliestFinish; i++) {
        if (remainingQuantity >= productionRate) {
          workerDemand[i] += t.optimalWorkerCount;
          remainingQuantity -= productionRate;
        } else {
          sufficientWorkers = (int)Math.ceil((remainingQuantity / (double)t.productionRate) * (double)t.optimalWorkerCount);
          workerDemand[i] += sufficientWorkers;
          break;
        }
      }
    }
    
    return workerDemand;
  }

  /**
   * Iterates over the contractor's tasks and assigns workers to the
   * active ones. A task is active if it is not finished and if all
   * predecessor tasks are finished.
   * @param today is the day of work
   */
  public void assignWorkers(int today, AlarmManager alarms) {
    if (today >= workerDemand.length) return;
    availableWorkers = (int)workerDemand[today]; // TODO: safe from double to int?
    availableWorkers = checkForSickWorkers(today, availableWorkers, alarms);

    // First go through all critical tasks
    for (Task t : scheduledTasks) {
      if (!t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted()) {
        assignWorkers(today, t, alarms);
      }
    }

    // Then go through all non-critical tasks
    for (Task t : scheduledTasks) {
      if (t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted()) {  
        assignWorkers(today, t, alarms);
      }
    }

    // Check whether workers are idle
    List<Task> blockedTasks = new ArrayList<>();
    for (Task t : scheduledTasks) {
      if (t.earliestStart <= today && !t.canBeStarted()) {
        blockedTasks.add(t);
      }
    }

    if (availableWorkers > 0) { 
      System.out.println(trade + " has " + availableWorkers + " unassigned workers!"); 
      System.out.println("The following tasks have been blocked: ");
      blockedTasks.forEach(t -> t.print());
    }
  }

  /**
   * 
   * @param t
   * @param delays
   */
  private void assignWorkers(int day, Task t, AlarmManager alarms) {
    // Find out whether we can supply a lower number of workers
    int w = 0;
    double remainingQuantity = (1 - (t.progress / 100)) * t.quantity;
    if (Math.abs(remainingQuantity % 1) < 0.000001) remainingQuantity = Math.round(remainingQuantity);
    int sufficientWorkers = 0;
    if (remainingQuantity >= t.productionRate) {
      sufficientWorkers = t.optimalWorkerCount;
    } else {
      double rate = (double)t.productionRate;
      double optimalWorkers = (double)t.optimalWorkerCount;
      sufficientWorkers = (int)Math.ceil((remainingQuantity / rate * optimalWorkers));
    }

    // Find the number of workers assigned
    w = Math.min(sufficientWorkers, availableWorkers);

    if (w < sufficientWorkers) {
      alarms.addDelays(new Alarm(day, t, trade, "supplying " + w + " workers instead of " + sufficientWorkers));
    }

    t.assignWorkers(w); 
    availableWorkers -= w; 
  }

  /**
   * Simulate workers being sick (each worker has a 3 sick days out of 200)
   * @param day
   * @param scheduledWorkers
   * @param alarms
   * @return
   */
  private int checkForSickWorkers(int day, int scheduledWorkers, AlarmManager alarms) {
    int sickWorkers = 0;
    for (int i = 0; i < scheduledWorkers; i++) {
      if (r.nextInt(200) < 3) {
        sickWorkers++;
      }
    }
    return scheduledWorkers - sickWorkers;
  }

  // public void checkWorkerSupply(int day, AlarmManager delays, Alarm d) {
  //   double[] newWorkerDemand = new double[scheduleLength];
  //   for (Task t : scheduledTasks) {
  //     if (t.isFinished()) continue;
  //     double remainingQuantity = (1 - (t.progress / 100)) * t.quantity;
  //     double contributionPerWorker = (double)t.productionRate / (double)t.optimalWorkerCount;
  //     double workersNeeded = Math.ceil(remainingQuantity / contributionPerWorker);
  //     newWorkerDemand[t.latestFinish] += workersNeeded;
      // newWorkerDemand[t.latestFinish] += workersNeeded % t.optimalWorkerCount;
      // workersNeeded -= newWorkerDemand[t.latestFinish];
      // int i = 1;
      // while (workersNeeded > 0) {
      //   newWorkerDemand[t.latestFinish-i] += t.optimalWorkerCount;
      //   workersNeeded -= t.optimalWorkerCount;
      //   i++;
      // }
    // }

  //   double hiredWorkers = 0;
  //   double neededWorkers = 0;
  //   int dayOfDelay = 0;
  //   System.err.println(trade + ":");
  //   for (int i = day; i < scheduleLength; i++) {
  //     hiredWorkers += workerDemand[i];
  //     neededWorkers += newWorkerDemand[i];
  //     if (neededWorkers > hiredWorkers) {
  //       dayOfDelay = i;
  //       break;
  //     }
  //   }
  //   System.out.println(trade + ": On day " + dayOfDelay + ", Hired = " + hiredWorkers + " | Needed = " + neededWorkers);

  //   if (dayOfDelay > 0) {
  //     System.out.println(trade + " needs to hire another " + (neededWorkers - hiredWorkers) + " workers before day " + dayOfDelay);
  //     estimateDelayImpact(d);
  //   } else {
  //     d.resolve(); 
  //   }
  // }

  // public void estimateDelayImpact(Alarm d) {
  //   // Find new early/late start/finish of all tasks with delay of given task
    
  // }

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

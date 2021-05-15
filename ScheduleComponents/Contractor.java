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
      if (t.latestFinish > scheduleLength) scheduleLength = t.latestFinish+1;

    this.scheduledTasks = tasks;
    this.workerDemand = new double[scheduleLength];
    for (Task t : tasks) {
      double remainingQuantity = t.quantity;
      double productionRate = t.productionRate;
      int sufficientWorkers = 0;
      for (int i = t.earliestStart; i < t.earliestFinish; i++) {
        if (remainingQuantity >= productionRate) {
          workerDemand[i] += t.optimalWorkerCount;
          remainingQuantity -= productionRate;
        } else {
          sufficientWorkers = (int)Math.ceil((remainingQuantity / (double)t.productionRate) * (double)t.optimalWorkerCount);
          workerDemand[i] += sufficientWorkers;
        }
      }
    }
  }

  /**
   * Iterates over the contractor's tasks and assigns workers to the
   * active ones. A task is active if it is not finished and if all
   * predecessor tasks are finished.
   * @param today is the day of work
   */
  public void assignWorkers(int today, AlarmManager delays) {
    if (today >= workerDemand.length) return;
    availableWorkers = (int)workerDemand[today]; // TODO: safe from double to int?
    availableWorkers = checkForSickWorkers(today, availableWorkers, delays);

    // First go through all critical tasks
    for (Task t : scheduledTasks) {
      if (!t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted()) {
        assignWorkers(today, t, delays);
      }
    }

    // Then go through all non-critical tasks
    for (Task t : scheduledTasks) {
      if (t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted()) {  
        assignWorkers(today, t, delays);
      }
    }
  }

  /**
   * 
   * @param t
   * @param delays
   */
  private void assignWorkers(int day, Task t, AlarmManager delays) {
    int w = 0;
    // Find out whether we can supply a lower number of workers
    double remainingQuantity = (1 - (t.progress / 100)) * t.quantity;
    int sufficientWorkers = 0;
    if (remainingQuantity >= t.productionRate) {
      sufficientWorkers = t.optimalWorkerCount;
    } else {
      sufficientWorkers = (int)Math.ceil((remainingQuantity / (double)t.productionRate) * (double)t.optimalWorkerCount);
    }

    w = Math.min(sufficientWorkers, availableWorkers);

    // if (w < sufficientWorkers) {
    //   delays.addDelays(new Alarm(day, Alarm.Type.workerSick, trade, "supplying " + w + " workers instead of " + sufficientWorkers));
    // }

    t.assignWorkers(w); 
    availableWorkers -= w; // sørger for, de samme workers ikke kan assignes twice, samme dag. 
  }

  /**
   * Simulate workers being sick (each worker has a 3 sick days out of 200)
   */
  private int checkForSickWorkers(int day, int scheduledWorkers, AlarmManager delays) {
    int sickWorkers = 0;
    for (int i = 0; i < scheduledWorkers; i++) {
      if (r.nextInt(200) < 3) {
        sickWorkers++;
      }
    }
    if (sickWorkers > 0) {
      delays.addDelays(new Alarm(day, Alarm.Type.workerSick, trade, "has " + sickWorkers + " sick worker(s)"));
    }
    return scheduledWorkers - sickWorkers;
  }

  public void checkWorkerSupply(int day, AlarmManager delays, Alarm d) {
    double[] newWorkerDemand = new double[scheduleLength];
    for (Task t : scheduledTasks) {
      if (t.isFinished()) continue;
      double remainingQuantity = (1 - (t.progress / 100)) * t.quantity;
      double contributionPerWorker = (double)t.productionRate / (double)t.optimalWorkerCount;
      double workersNeeded = Math.ceil(remainingQuantity / contributionPerWorker);
      newWorkerDemand[t.latestFinish] += workersNeeded;
      // newWorkerDemand[t.latestFinish] += workersNeeded % t.optimalWorkerCount;
      // workersNeeded -= newWorkerDemand[t.latestFinish];
      // int i = 1;
      // while (workersNeeded > 0) {
      //   newWorkerDemand[t.latestFinish-i] += t.optimalWorkerCount;
      //   workersNeeded -= t.optimalWorkerCount;
      //   i++;
      // }
    }

    double hiredWorkers = 0;
    double neededWorkers = 0;
    int dayOfDelay = 0;
    System.err.println(trade + ":");
    for (int i = day; i < scheduleLength; i++) {
      hiredWorkers += workerDemand[i];
      neededWorkers += newWorkerDemand[i];
      if (neededWorkers > hiredWorkers) {
        dayOfDelay = i;
        break;
      }
    }
    System.out.println(trade + ": On day " + dayOfDelay + ", Hired = " + hiredWorkers + " | Needed = " + neededWorkers);

    if (dayOfDelay > 0) {
      System.out.println("Delay is critical...");
      estimateDelayImpact(d);
    } else {
     d.resolve(); 
    }
  }

  public void estimateDelayImpact(Alarm d) {

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

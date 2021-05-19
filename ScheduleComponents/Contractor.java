package ScheduleComponents;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Contractor {
  public String id;
  public String name;
  public String trade;
  public Random r = new Random();
  public int[] workerDemand;
  public int scheduleLength;
  public List<Task> scheduledTasks;
  public int availableWorkers;
  public int sickWorkers;

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
      double remainingQuantity = t.getRemainingQuantity();
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
    sickWorkers = checkForSickWorkers(today, availableWorkers, alarms);
    availableWorkers -= sickWorkers;

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

    while (availableWorkers > 0) {
      System.out.println(trade + " has " + availableWorkers + " idle workers!");
      Task t = scheduledTasks.get(0);
      for (Task t2 : scheduledTasks) {
        if (!t2.isFinished() && t2.scheduledFinish < t.scheduledFinish) {
          t = t2;
        }
      }
      assignWorkers(t, 1);
      System.out.println("Assigned an extra worker to " + t.location + t.id);
    }

    sickWorkers = 0;
  }

  /**
   * 
   * @param t
   * @param delays
   */
  private void assignWorkers(int day, Task t, AlarmManager alarms) {
    // Find out whether we can supply a lower number of workers
    int w = 0;
    double remainingQuantity = t.getRemainingQuantity();
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
      alarms.addDelays(new Alarm(day, t, trade, "supplying " + w + " workers instead of " + sufficientWorkers + " - sick workers : " + sickWorkers));
    }

    t.assignWorkers(w); 
    availableWorkers -= w; 
  }

  private void assignWorkers(Task t, int w) {
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
    return sickWorkers;
  }

  /**
   * Compares the worker demand with the worker supply. If there is a worker
   * shortage, it will be determined how many workers are needed by which day.
   * 
   * @param newWorkerDemand
   * @param day
   * @return true if worker schedule has been changed - false otherwise
   */
  public boolean checkWorkerSupply(Task t, int[] newWorkerDemand, int day) {
    double hiredWorkers = 0;
    double neededWorkers = 0;
    int dayOfDelay = 0;
    for (int i = day; i < scheduleLength; i++) {
      hiredWorkers += workerDemand[i];
      neededWorkers += newWorkerDemand[i];
      System.out.printf("%3d: %3.0f  | %3.0f%n", i, hiredWorkers, neededWorkers);
      if (neededWorkers > hiredWorkers) {
        dayOfDelay = i;
        System.err.println(trade + ":");
        System.out.println(trade + ": On day " + dayOfDelay + ", Hired = " + hiredWorkers + " | Needed = " + neededWorkers);
        break;
      }
    }

    System.out.println("Does this break too soon? What if there is a larger worker shortage the following day?");

    if (dayOfDelay > 0) return delayLatestTask(t, day, dayOfDelay, (int)(neededWorkers - hiredWorkers));
    return false;
  }

  /**
   * 
   * @param day
   * @param dayOfDelay
   * @param workerShortage
   * @return
   */
  public boolean delayLatestTask(Task t, int day, int dayOfDelay, int workerShortage) {
    // // To Mikkel: This is some pretty complex stuff - just ask me about it if u wanna know :)
    // Find out when the latest worker is supplied. This determines which task will be pushed back
    boolean otherContractorsNeedToReschedule = askContractorForMoreManpower(t, day, dayOfDelay, workerShortage);
    System.out.println("Do we need more info? What if the worker shortage actually delays multiple tasks?");
    // Find a task that is not critical and can be started before this time.
    // Among all the tasks that live up to these criteria, the task with the earliestFinish
    // furthest out in the future will have its earliestFinish extended (making the least possible impact) 
    // because the resources are not available before that day 
    // int latestEarliestFinish = 0;
    // Task t = null;
    // for (Task t2 : scheduledTasks) {
    //   if (!t2.isCritical && t2.earliestFinish < newEarliestFinish && t2.earliestFinish > latestEarliestFinish) {
    //     latestEarliestFinish = t2.earliestFinish;
    //     t = t2;
    //   }
    // }
    // System.out.println(t.location + t.id + " has gotten new earliest finish! (" + t.earliestFinish + " to " + newEarliestFinish + ")");
    // t.earliestFinish = newEarliestFinish;
    return otherContractorsNeedToReschedule;
  }

  /**
   * Informs the contractor about a worker shortage and asks when they will be 
   * able to supply the needed worker amount
   * @param day is todays date
   * @param latestDay is the latest day to supply workers without causing critical delays
   * @param workerShortage is the amount of workers needed before 'latestDay'
   * @return the day of the last worker supplied
   */
  public boolean askContractorForMoreManpower(Task t, int day, int latestDay, int workerShortage) {
    // Supply workers at latest this day to be able to keep project deadline intact
    int daysLeft = latestDay - day;
    // Supply workers at latest this day to be able to keep contractor schedules intact
    int startOfDependingTask = Integer.MAX_VALUE;
    for (Task t2 : t.successorTasks) {
      if (t2.scheduledStart < startOfDependingTask) startOfDependingTask = t2.scheduledStart;
    }
    int daysLeftBeforeReschedule = startOfDependingTask < Integer.MAX_VALUE ? startOfDependingTask - day : 0;
    String prompt = "\n\n To " + trade + ":\n" + 
    "You need to provide " + workerShortage + " more manpower (workers) from either" +
    " working overtime/weekends or by hiring more workers. \nIf the extra manpower is not" +
    " provided in the next " + daysLeft + " days, the construction project will be delayed!";
    if (daysLeftBeforeReschedule <= 0) {
      prompt += "\nThe other contractors will need to re-arrange their schedules no matter what...";
    } else {
      prompt += "\nIf the extra manpower is not provided in the next " + daysLeftBeforeReschedule + 
      " days, the other contractors will need to re-arrange their schedule!";
    }
    System.err.println(prompt);

    Scanner sc = new Scanner(System.in);
    int i = 0; 
    int lastWorker = 0;
    boolean otherContractorsNeedToReschedule = false;
    String resp = "";
    while (i < workerShortage) {
      System.out.println("\n\n In how many days can the contractor supply worker number " + (i+1) + "?");
      resp = sc.nextLine();
      try {
        int days = Integer.parseInt(resp);
        if (days > daysLeftBeforeReschedule) otherContractorsNeedToReschedule = true;
        if (days > daysLeft) throw new Exception("That is too late! Please try again...");
        int dayOfSupply = day + days - 1;
        workerDemand[dayOfSupply]++;
        if (dayOfSupply > lastWorker) lastWorker = dayOfSupply;
        i++;
      } catch (NumberFormatException e) {
        System.out.println("I don't understand that number! Please try again...");
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }

    return otherContractorsNeedToReschedule;
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

package ScheduleComponents;

import java.util.ArrayList;
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
   * Iterates over all tasks assigned to this contractor and assigns
   * workers to the tasks. A task will be considered if it is not 
   * finished and if all its preceding tasks have been finished. The
   * function prioritizes critical tasks.
   * @param today is the day, on which we want to assign workers
   * @param alarms is the AlarmManager, able to create alarms
   */
  public void assignWorkers(int today, AlarmManager alarms) {
    if (today >= workerDemand.length) return;
    availableWorkers = workerDemand[today];
    sickWorkers = checkForSickWorkers(today, availableWorkers, alarms);
    availableWorkers -= sickWorkers;

    List<Task> sortedTasks = new ArrayList<>();
    for (Task t : scheduledTasks) { if (!t.isFinished()) sortedTasks.add(t); }
    Collections.sort(sortedTasks, new SortByEarliestScheduledFinish());

    // First go through all critical tasks
    for (Task t : sortedTasks) {
      if (!t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted()) {
        assignWorkers(today, t, alarms, sickWorkers);
      }
    }

    // Then go through all non-critical tasks
    for (Task t : sortedTasks) {
      if (t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted()) {  
        assignWorkers(today, t, alarms, sickWorkers);
      }
    }

    while (availableWorkers > 0) {
      System.out.println(trade + " has " + availableWorkers + " idle workers!");
      Task t = sortedTasks.get(0);
      for (Task t2 : sortedTasks) {
        if (!t2.isFinished() && t2.scheduledFinish < t.scheduledFinish && t2.workersAssigned < t.workersAssigned) {
          t = t2;
        }
      }
      assignWorkers(t, 1, today);
      System.out.println("Assigned an extra worker to L" + t.location + "T" + t.id);
    }

    sickWorkers = 0;
  }

  /**
   * Assigns workers by finding the optimal number of workers for the given task
   * and determining whether this number of workers is available today.
   * 
   * Every day, a worker has a chance of calling in sick. If workers call in sick 
   * and the available worker amount is less than expected, an alarm will be created.
   * @param day is the day, on which we want to assign workers
   * @param t is the task, to which we want to assign workers
   * @param alarms is the Alarm Manager, which is able to create alarms
   * @param sickWorkers is the number of sick workers on the day
   */
  private void assignWorkers(int day, Task t, AlarmManager alarms, int sickWorkers) {
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
    if (w < sufficientWorkers && sickWorkers > 0) {
      alarms.addDelays(new Alarm(day, t, trade, "Has sick workers : " + sickWorkers));
    }

    assignWorkers(t, w, day);
  }

  private void assignWorkers(Task t, int w, int day) {
    t.assignWorkers(w);
    availableWorkers -= w;
    workerDemand[day] -= w;
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
        workerDemand[day]--;
        sickWorkers++;
      }
    }
    return sickWorkers;
  }

  /**
   * Compares the worker demand with the worker supply. If there is a worker
   * shortage, it will be determined how many workers are needed by which day.
   * @param newWorkerDemand is the int array of needed workers until project deadline
   * @param day is the given day, from which we want to estimate worker supply
   * @return true if worker schedule has been changed - false otherwise
   */
  public void checkWorkerSupply(Task t, int[] newWorkerDemand, int day) {
    double hiredWorkers = 0;
    double neededWorkers = 0;
    int dayOfDelay = 0;
    for (int i = day; i < scheduleLength; i++) {
      hiredWorkers += workerDemand[i];
      neededWorkers += newWorkerDemand[i];
      if (neededWorkers > hiredWorkers) {
        dayOfDelay = i;
        break;
      }
    }

    System.out.println("Does this break too soon? What if there is a larger worker shortage the following day?");

    if (dayOfDelay > 0) askContractorForMoreManpower(t, day, dayOfDelay, (int)(neededWorkers - hiredWorkers));
  }

  /**
   * Informs the contractor about a worker shortage and asks when they will be 
   * able to supply the needed worker amount
   * @param day is todays date
   * @param latestDay is the latest day to supply workers without causing critical delays
   * @param workerShortage is the amount of workers needed before 'latestDay'
   * @return the day of the last worker supplied
   */
  public void askContractorForMoreManpower(Task t, int day, int latestDay, int workerShortage) {
    // Supply workers at latest this day to be able to keep project deadline intact
    int daysLeft = latestDay - day;
    // Supply workers at latest this day to be able to keep contractor schedules intact
    int startOfDependingTask = Integer.MAX_VALUE;
    for (Task t2 : t.successorTasks) {
      if (t2.scheduledStart < startOfDependingTask) startOfDependingTask = t2.scheduledStart;
    }
    int daysLeftBeforeReschedule = startOfDependingTask < Integer.MAX_VALUE ? startOfDependingTask - day : t.latestFinish;
    String prompt = "\n\n # To " + trade + ":\n" + 
    "You need to provide " + workerShortage + " more manpower (workers) from either" +
    " working overtime/weekends or by hiring more workers. \nIf the extra manpower is not" +
    " provided in the next " + daysLeft + " days, the construction project will be delayed!";
    if (daysLeftBeforeReschedule <= 0) {
      prompt += "\nContractors will need to re-arrange their schedules no matter what...";
    } else {
      prompt += "\nIf the extra manpower is not provided in the next " + daysLeftBeforeReschedule + 
      " days, contractors will need to re-arrange their schedule!";
    }
    prompt += "\nWriting '1' means supplying a worker tomorrow.";
    System.err.println(prompt);

    if (daysLeftBeforeReschedule > daysLeft) {
      System.out.println("");
    }

    Scanner sc = new Scanner(System.in);  // Do not close - if you do, the program crashes :(
    int i = 0; 
    int lastWorker = 0;
    String resp = "";
    while (i < workerShortage) {
      System.out.println("\n\nIn how many days can the contractor supply worker number " + (i+1) + "?");
      resp = sc.nextLine();
      try {
        int days = Integer.parseInt(resp);
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

  private class SortByEarliestScheduledFinish implements Comparator<Task> {
    @Override
    public int compare(Task t1, Task t2) {
      return t1.scheduledFinish - t2.scheduledFinish;
    }
  }
}

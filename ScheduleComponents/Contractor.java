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
      double remainingQuantity = t.quantity * (100 - t.progress) / 100;
      if (Math.abs(remainingQuantity % 1) < 0.000001) remainingQuantity = Math.round(remainingQuantity);
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

    if (availableWorkers > 0) { 
      System.out.println(trade + " has " + availableWorkers + " idle workers!");
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
      alarms.addDelays(new Alarm(day, t, trade, "supplying " + w + " workers instead of " + sufficientWorkers + " - sick workers : " + sickWorkers));
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
    return sickWorkers;
  }

  public boolean checkWorkerSupply(int[] newWorkerDemand, int day) {
    double hiredWorkers = 0;
    double neededWorkers = 0;
    int dayOfDelay = 0;
    for (int i = day; i < scheduleLength; i++) {
      hiredWorkers += workerDemand[i];
      neededWorkers += newWorkerDemand[i];
      // System.out.printf("%3d: %3.0f  | %3-.0f%n", i, hiredWorkers, neededWorkers);
      if (neededWorkers > hiredWorkers) {
        dayOfDelay = i;
        // if (dayOfDelay == 0) {
        //   System.out.println("");
        // }
        System.err.println(trade + ":");
        System.out.println(trade + ": On day " + dayOfDelay + ", Hired = " + hiredWorkers + " | Needed = " + neededWorkers);
        break;
      }
    }

    if (dayOfDelay > 0) {
      int newEarliestFinish = estimateDelayImpact(day, dayOfDelay, (int)(neededWorkers - hiredWorkers), workerDemand);
      // To Mikkel: This is some pretty complex stuff - just ask me about it if u wanna know :)
      int latestEarliestFinish = 0;
      Task t = null;
      for (Task t2 : scheduledTasks) {
        if (!t2.isCritical && t2.earliestFinish < newEarliestFinish && t2.earliestFinish > latestEarliestFinish) {
          latestEarliestFinish = t2.earliestFinish;
          t = t2;
        }
      }
      System.out.println(t.location + t.id + " has gotten new earliest finish! (" + t.earliestFinish + " to " + newEarliestFinish + ")");
      t.earliestFinish = newEarliestFinish;
      return true;
    } //else {
      // No need to do anything?
    // }

    return false;
  }

  public int estimateDelayImpact(int today, int latestDay, int workerShortage, int[] workerDemand) {
    Scanner sc = new Scanner(System.in);
    int daysLeft = latestDay - today;
    String prompt = "\n\n" +
      trade + " need to provide " + workerShortage + " more manpower (workers) -" +
      " or else the project is not able to be finished. The manpower may come from working" + 
      " overtime/weekends or by hiring more workers. If the manpower is not provided in the" + 
      " next " + daysLeft + " days, the project deadline will be pushed back!" + 
      " Can the contractor supply this by means of overtime or more workers? (Y/N)";
    System.err.println(prompt);
    String resp = sc.nextLine().toLowerCase();
    int attempt = 1;
    while (!resp.equals("y") && !resp.equals("n")) {
      System.out.println("I did not understand that - please try again!");
      resp = sc.nextLine().toLowerCase();
      if (attempt >= 2) {
        System.err.println("I was unable to read any of that... Without proper input, I have to shut down");
        System.exit(1);
      }
      attempt++;
    }
    if (!resp.equals("y")) { 
      System.out.println("\n\nYou can restart the project if you find the required resources..."); 
      System.exit(1);
    }

    int i = 0;
    int lastWorker = 0;
    while (i < workerShortage) {
      System.out.println("\n\nIn how many days can the contractor supply worker number " + (i+1) + "?");
      resp = sc.nextLine();
      try {
        int days = Integer.parseInt(resp);
        if (days > daysLeft) throw new Exception("That is too late! Please try again...");
        // if (days < today) throw new Exception("We are already past day " + days + "! Please try again...");
        workerDemand[today + days]++;
        if (today + days > lastWorker) lastWorker = today + days;
        i++;
      } catch (NumberFormatException e) {
        System.out.println("I don't understand that number! Please try again...");
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }

    // Change earliest start/finish for task

    // sc.close();
    return lastWorker;
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

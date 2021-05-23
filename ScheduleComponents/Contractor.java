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
  public Trade trade;
  public Random r = new Random();
  public int[] workerDemand;
  public int scheduleLength;
  public List<Task> scheduledTasks;
  public int availableWorkers;
  public int sickWorkers;

  public enum Trade {
    Carpenter,
    CementOrConcreteFinisher,
    Electrician,
    FlooringInstaller,
    Glazier,
    HVACTech,
    InsulationWorker,
    Plumber,
    RoofingMechanic,
    Painter
  }

  public Contractor(String id, Trade trade) {
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
  public boolean assignWorkers(int today, AlarmManager alarms, boolean alreadySickWorkers) {
    if (today >= workerDemand.length) return alreadySickWorkers;
    sickWorkers = 0;
    if (!alreadySickWorkers) sickWorkers = checkForSickWorkers(today, workerDemand[today], alarms);
    availableWorkers = workerDemand[today];
    if (sickWorkers > 0) {
      alarms.addDelays(new Alarm(today, scheduledTasks.get(0), trade, "Has sick workers : " + sickWorkers));
      System.out.println(trade + " has " + sickWorkers + " sick workers!");
      alreadySickWorkers = true;
    }

    List<Task> sortedTasks = new ArrayList<>();
    for (Task t : scheduledTasks) { if (!t.isFinished()) sortedTasks.add(t); }
    Collections.sort(sortedTasks, new SortByEarliestScheduledFinish());

    // First go through all critical tasks
    for (Task t : sortedTasks) {
      if (!t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted()) {
        assignWorkers(today, t, alarms);
        if (t.workerShortage > 0 ) {
          while (availableWorkers > 0 && t.workerShortage > 0) {
            t.assignExtraWorker(1, today);
            availableWorkers--;
          }
        }
      }
    }
    for (Task t : sortedTasks) {
      if (t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted() && t.workerShortage > 0) {
        assignWorkers(today, t, alarms);
        if (t.workerShortage > 0 ) {
          while (availableWorkers > 0 && t.workerShortage > 0) {
            t.assignExtraWorker(1, today);
            availableWorkers--;
          }
        }
      }
    }
    for (Task t : sortedTasks) {
      if (t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted() && t.workerShortage <= 0) {
        assignWorkers(today, t, alarms);
        if (t.workerShortage > 0 ) {
          while (availableWorkers > 0 && t.workerShortage > 0) {
            t.assignExtraWorker(1, today);
            availableWorkers--;
          }
        }
      }
    }

    while (availableWorkers > 0) {
      System.out.println(trade + " has " + availableWorkers + " idle workers!");
      Task t = null;
      int scheduledFinish = Integer.MAX_VALUE;
      double progress = 100;
      for (Task t2 : sortedTasks) {
        // double t2ProductionPerWorker = (double)t2.productionRate / (double)t2.optimalWorkerCount;
        double t2Progress = t2.progress + t2.transformWorkersToProgress(t2.workersAssigned);
        if (!t2.isFinished() && t2.canBeStarted() && t2.scheduledFinish <= scheduledFinish && t2Progress < progress) {
          scheduledFinish = t2.scheduledFinish;
          progress = t2Progress;
          t = t2;
        }
      }
      if (t == null) {
        System.out.println(trade + " not assigning an idle worker!");
        for (Task t3 : sortedTasks) {
          double q = t3.getRemainingQuantity();
          double p = t3.productionRate;
          double missing = Math.ceil(q / p);
          System.out.printf("L%dT%d: Scheduled %3d  - %3d  (progress = %f, %f)%n", t3.location, t3.id, t3.scheduledStart, t3.scheduledFinish, t3.progress, missing);
        }
        availableWorkers--;
      } else {
        assignWorkers(t, 1, today);
        System.out.println(trade + " assigned an extra worker to L" + t.location + "T" + t.id + "!");
      }
    }

    return alreadySickWorkers;
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
  private void assignWorkers(int day, Task t, AlarmManager alarms) {
    // Find out whether we can supply a lower number of workers
    int w = 0;
    double remainingQuantity = t.getRemainingQuantity();
    int sufficientWorkers = 0;
    if (remainingQuantity >= (double)t.productionRate) {
      sufficientWorkers = t.optimalWorkerCount;
      // if (t.workerShortage > 0) {
      //   sufficientWorkers += t.workerShortage;
      // }
    } else {
      double rate = (double)t.productionRate;
      double optimalWorkers = (double)t.optimalWorkerCount;
      sufficientWorkers = (int)Math.ceil((remainingQuantity / rate * optimalWorkers)) - t.workersAssigned;
      // System.out.printf("%f / %f * %f = %d%n", remainingQuantity, rate, optimalWorkers, sufficientWorkers);
    }

    // Find the number of workers assigned
    w = Math.min(sufficientWorkers, availableWorkers);

    assignWorkers(t, w, day);
    // if (t.workerShortage > 0 && availableWorkers > 0) {
    //   System.out.println();
    // }
  }

  private void assignWorkers(Task t, int w, int day) {
    System.out.println(trade + " supplying " + w + " workers out of " + availableWorkers + " to L" + t.location + "T" + t.id + " (optimal: " + t.optimalWorkerCount + ")");
    t.assignWorkers(w, day);
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
  public void checkWorkerSupply(int day) {
    // Go through tasks, check how much their scheduled finishes will be delayed
    boolean affectsProjectDeadline = false;
    int firstDelay = Integer.MAX_VALUE;
    List<Task> delayedTasks = new ArrayList<>();
    List<Task> unfinishedTasks = new ArrayList<>();
    int workersMissing = 0;
    int[] currentWorkerSupply = workerDemand.clone();
    for (Task t : scheduledTasks) {
      if (!t.isFinished()) {
        double remainingQuantity = t.getRemainingQuantity();
        double quantityPerWorker = (double)t.productionRate / (double)t.optimalWorkerCount;
        double workersNeeded = remainingQuantity / quantityPerWorker;
        if (Math.abs(workersNeeded % 1) < 0.000001) workersNeeded = Math.round(workersNeeded);
        int w = 0;
        int i = t.scheduledStart;
        while (workersNeeded > 0 && i >= t.scheduledStart) {
          if (workersNeeded >= t.optimalWorkerCount) {
            w = t.optimalWorkerCount;
          } else {
            w = (int) Math.ceil(workersNeeded);
          }
          
          w = Math.min(w, currentWorkerSupply[i]);
          if (w > 0 && remainingQuantity == t.getRemainingQuantity()) t.scheduledStart = i;
          workersNeeded -= w;
          remainingQuantity -= w*quantityPerWorker;
          currentWorkerSupply[i] -= w;

          if (t.scheduledFinish-1 <= i && t.scheduledFinish != 0 && remainingQuantity > 0) {
            int d = t.scheduledStart;
            while (workersNeeded > 0) {
              if (currentWorkerSupply[d] > 0) {
                remainingQuantity -= quantityPerWorker;
                workersNeeded--;
                currentWorkerSupply[d]--;
              } else {
                d++;
              }
              if (d > i || d >= currentWorkerSupply.length) break;
            }
          }

          if (i >= currentWorkerSupply.length-1) {
            unfinishedTasks.add(t);
            if (Math.abs(workersNeeded % 1) < 0.000001) workersNeeded = Math.round(workersNeeded);
            workersMissing += Math.ceil(workersNeeded);
            affectsProjectDeadline = true;
            if (i <= firstDelay) firstDelay = i-1;
            break;
          }
          if (i > t.scheduledFinish) {
            if (i <= firstDelay) firstDelay = i-1;
          }
          i++;
        }

        if (i > t.scheduledFinish && i < currentWorkerSupply.length) {
          delayedTasks.add(t);
          if (i <= firstDelay) firstDelay = i-1;
        }
        if (i > t.latestFinish && i < currentWorkerSupply.length) affectsProjectDeadline = true;
      }
    }

    if (workersMissing > 1) {
      System.out.println();
    }

    if (delayedTasks.size() > 0 || unfinishedTasks.size() > 0) askContractorForMoreManpower(day, firstDelay, workersMissing, affectsProjectDeadline, delayedTasks, unfinishedTasks);
  }

  private int[] resizeArray(int[] arr, int sz) {
    int[] arr2 = new int[sz];
    for (int i = 0; i < arr.length; i++) {
      arr2[i] = arr[i];
    }
    return arr2;
  }

  public void askContractorForMoreManpower(int day, int firstDelay, int workersMissing, boolean affectsProjectDeadline, List<Task> delayedTasks, List<Task> unfinishedTasks) {
    if (firstDelay == Integer.MAX_VALUE) firstDelay = -1;
    String prompt = "\n\n # To " + trade + ":\n" +
      "You need to provide " + workersMissing + " more manpower (workers) from " +
      "working overtime/weekends or by hiring more workers. \n";
    int daysLeftBeforeDelay = firstDelay - day+1;
    String delayedTask = "[";
    for (Task t : delayedTasks) {
      delayedTask += "L" + t.location + "T" + t.id + ";";
    }
    delayedTask = delayedTask.substring(0, delayedTask.length()-1) + "]";
    if (daysLeftBeforeDelay <= 0) {
      prompt += "There is not enough time to avoid a delay... \n";
    } else {
      if (delayedTask.length() > 1) {
        prompt += "If the extra manpower is " +
        "not provided in the next " + daysLeftBeforeDelay + " days, the following tasks will " +
        "be delayed: " + delayedTask + ".\n";
      } else {
        prompt += "The extra manpower needs to be provided in the next " + daysLeftBeforeDelay + " days! \n";
      }
    }
    if (unfinishedTasks.size() > 0) {
      String unfinished = "[";
      for (Task t : unfinishedTasks) {
        unfinished += "L" + t.location + "T" + t.id + ";";
      }
      unfinished = unfinished.substring(0, unfinished.length()-1) + "]";
      prompt += "There will not be enough manpower to finish the following tasks: " + unfinished;
    } else {
      prompt += "All tasks should still be able to be finished.";
    }
    // if (affectsProjectDeadline) {
    //   prompt += "\nThe total project deadline will also be pushed back.";
    // } else {
    //   prompt += "\nThe total project deadline should not be affected.";
    // }
    prompt += "\nWriting '1' means supplying a worker tomorrow.";
    System.err.println(prompt);

    int dayOfSupply = 0;
    for (int i = 0; i < workersMissing; i++) {
      // dayOfSupply = getResponse(day, i);
      dayOfSupply = getRandomResponse(day, i, daysLeftBeforeDelay);
      addWorkerToSchedule(dayOfSupply);
    }
  }

  private int getResponse(int day, int worker) {
    Scanner sc = new Scanner(System.in);  // Do not close - if you do, the program crashes :(
    String resp = "";
    int dayOfSupply = 0;
    System.out.println("\n\nIn how many days can the contractor supply worker number " + worker + "?");
    resp = sc.nextLine();
    try {
      int days = Integer.parseInt(resp);
      if (days <= 0) throw new Exception("It is not possible to supply workers for today or earlier! Please try again...");
      dayOfSupply = day + days - 1;
    } catch (NumberFormatException e) {
      System.out.println("I don't understand that number! Please try again...");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return dayOfSupply;
  }

  private int getRandomResponse(int day, int worker, int daysLeftBeforeDelay) {
    int dayOfSupply = 0;
    if (daysLeftBeforeDelay > 1) {
      if (r.nextInt(100) > 25) {
        dayOfSupply = r.nextInt(daysLeftBeforeDelay-1)+1;
      } else if (daysLeftBeforeDelay == 1) {
        if (r.nextInt(1) > 0) {
          dayOfSupply = 1;
        } else {
          dayOfSupply = r.nextInt(5)+1;
        }
      } else {
        dayOfSupply = r.nextInt(10)+1;
      }
    } else {
      dayOfSupply = r.nextInt(10)+1;
    }
    dayOfSupply += day;
    return dayOfSupply;
  }

  public void addWorkerToSchedule(int day) {
    if (day >= workerDemand.length) {
      int[] newWorkerDemand = new int[day+1];
      for (int i = 0; i < workerDemand.length; i++) {
        newWorkerDemand[i] = workerDemand[i];
      }
      workerDemand = newWorkerDemand;
    }
    workerDemand[day]++;
    System.out.println(trade + ": added a worker at day " + day);
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

package ScheduleComponents;

import java.util.ArrayList;
import java.util.Arrays;
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
    if (availableWorkers < 0) {
      System.out.println();
    }
    if (sickWorkers > 0) {
      alarms.addDelays(new Alarm(today, scheduledTasks.get(0), trade, "Has sick workers : " + sickWorkers));
      System.out.println(trade + " has " + sickWorkers + " sick workers!");
      alreadySickWorkers = true;
    }

    List<Task> sortedTasks = new ArrayList<>();
    for (Task t : scheduledTasks) { if (!t.isFinished()) sortedTasks.add(t); }
    Collections.sort(sortedTasks, new SortByEarliestScheduledFinish());

    
    for (Task t : sortedTasks) {
      if (!t.isFinished() && t.canBeStarted() && t.workerShortage > 0) {
        int workersSupplied = 0;
        while (workersSupplied < t.workerShortage && availableWorkers > 0) {
          assignWorkers(t, 1, today);
          workersSupplied++;
        }
      }
    }
    // Go through all critical tasks
    for (Task t : sortedTasks) {
      if (!t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted()) {
        assignWorkers(today, t, alarms);
      }
    }
    for (Task t : sortedTasks) {
      if (t.isCritical) continue;
      if (!t.isFinished() && t.canBeStarted()) {
        assignWorkers(today, t, alarms);
      }
    }

    // Re-assign workers, if tasks are not being finished on time
    for (Task t : sortedTasks) {
      if (t.canBeStarted() && t.scheduledFinish == today+1) {
        boolean move = false;
        while (t.progress + t.transformWorkersToProgress(t.workersAssigned) < 100) {
          for (Task t2 : sortedTasks) {
            if (t.equals(t2)) continue;
            if (t2.workersAssigned > 0 && t2.scheduledFinish > today+1) {
              t2.workersAssigned--;
              t.workersAssigned++;
              move = true;
              System.out.println("Moving a worker from L" + t2.location + "T" + t2.id + " to L" + t.location + "T" + t.id);
            }
          }
          if (!move) break;
        }
      }
    }

    while (availableWorkers > 0) {
      // System.out.println(trade + " has " + availableWorkers + " idle workers!");
      Task t = null;
      int scheduledFinish = Integer.MAX_VALUE;
      double progress = 100;
      int nextTaskStart = Integer.MAX_VALUE;
      for (Task t2 : sortedTasks) {
        double t2Progress = t2.progress + t2.transformWorkersToProgress(t2.workersAssigned);
        if (t2.scheduledStart < nextTaskStart) nextTaskStart = t2.scheduledStart;
        if (!t2.isFinished() && t2.canBeStarted() && t2.scheduledFinish <= scheduledFinish && t2Progress < progress) {
          scheduledFinish = t2.scheduledFinish;
          progress = t2Progress;
          t = t2;
        }
      }
      if (t == null) {
        System.out.println("Not enough work to be performed by " + trade + " worker!");
        workerDemand[today]--;
        workerDemand[nextTaskStart]++;
        // System.out.println(trade + " not assigning an idle worker!");
        // for (Task t3 : sortedTasks) {
        //   double q = t3.getRemainingQuantity();
        //   double p = t3.productionRate;
        //   double missing = Math.ceil(q / p);
        //   System.out.printf("L%dT%d: Scheduled %3d  - %3d  (progress = %f, %f)%n", t3.location, t3.id, t3.scheduledStart, t3.scheduledFinish, t3.progress, missing);
        // }
        availableWorkers--;
      } else {
        assignWorkers(t, 1, today);
        // System.out.println(trade + " assigned an extra worker to L" + t.location + "T" + t.id + "!");
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
    // int w = 0;
    double remainingQuantity = t.getRemainingQuantity();
    double rate = (double) t.productionRate;
    double optimalWorkers = (double) t.optimalWorkerCount;
    int neededWorkers = (int) Math.ceil(remainingQuantity / rate * optimalWorkers);

    int workersToBeAssigned = 0;
    if (neededWorkers >= optimalWorkers) {
      workersToBeAssigned = (int) optimalWorkers;
      int assignedWorkers = t.workersAssigned;
      if (workersToBeAssigned < 0 || availableWorkers < 0) {
        System.out.println();
      }

      int extraWorkers = 0;
      while (extraWorkers < t.workerShortage) {
        extraWorkers++;
      }
      workersToBeAssigned += extraWorkers;
      if (workersToBeAssigned < 0 || availableWorkers < 0) {
        System.out.println();
      }
      while (workersToBeAssigned + assignedWorkers - 1 >= neededWorkers) {
        workersToBeAssigned--;
      }
      if (workersToBeAssigned < 0 || availableWorkers < 0) {
        System.out.println();
      }
    } else {
      workersToBeAssigned = neededWorkers;
      int assignedWorkers = t.workersAssigned;
      if (workersToBeAssigned < 0 || availableWorkers < 0) {
        System.out.println();
      }

      while (workersToBeAssigned + assignedWorkers > neededWorkers) {
        workersToBeAssigned--;
      }
      if (workersToBeAssigned < 0 || availableWorkers < 0) {
        System.out.println();
      }
    }


    workersToBeAssigned = Math.min(workersToBeAssigned, availableWorkers);
    assignWorkers(t, workersToBeAssigned, day);
  }

  private void assignWorkers(Task t, int w, int day) {
    // System.out.println(trade + " supplying " + w + " workers out of " + availableWorkers + " to L" + t.location + "T" + t.id + " (optimal: " + t.optimalWorkerCount + ")");
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
        // workerDemand[day]--;
        sickWorkers++;
      }
    }
    if (sickWorkers > 0) {
      workerDemand[day]--;
      return 1;
    }
    return 0;
    // return sickWorkers;
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
    WorkerForecast wf = new WorkerForecast();
    wf.contractorSchedule = workerDemand.clone();

    for (Task t : scheduledTasks) {
      if (t.isFinished()) continue;
      if (!t.isCritical) continue;

      WorkerForecast wf2 = forecastWorkerSupply(day, t, wf.contractorSchedule);
      wf2.reschedules.forEach(r -> wf.addReschedule(r));
      wf2.delayedTasks.forEach(d -> wf.addDelayedTask(d));
      wf2.unfinishedTasks.forEach(u -> wf.addUnfinishedTask(t));
      // System.out.println(Arrays.toString(wf.contractorSchedule));
      wf.contractorSchedule = wf2.contractorSchedule;
      // System.out.println(Arrays.toString(wf.contractorSchedule));
      if (wf2.firstDelay < wf.firstDelay) wf.firstDelay = wf2.firstDelay;
      wf.workersNeeded += wf2.workersNeeded;
    }

    for (Task t : scheduledTasks) {
      if (t.isFinished()) continue;
      if (t.isCritical) continue;

      WorkerForecast wf2 = forecastWorkerSupply(day, t, wf.contractorSchedule);
      wf2.reschedules.forEach(r -> wf.addReschedule(r));
      wf2.delayedTasks.forEach(d -> wf.addDelayedTask(d));
      wf2.unfinishedTasks.forEach(u -> wf.addUnfinishedTask(t));
      // System.out.println(Arrays.toString(wf.contractorSchedule));
      wf.contractorSchedule = wf2.contractorSchedule;
      // System.out.println(Arrays.toString(wf.contractorSchedule));
      if (wf2.firstDelay < wf.firstDelay) wf.firstDelay = wf2.firstDelay;
      wf.workersNeeded += wf2.workersNeeded;
    }

    for (WorkerReschedule r : wf.reschedules) {
      while (r.toDay >= workerDemand.length) {
        int[] newWorkerDemand = new int[Math.max(workerDemand.length*2, r.toDay)];
        for (int i = 0; i < workerDemand.length; i++) {
          newWorkerDemand[i] = workerDemand[i];
        }
        workerDemand = newWorkerDemand;
      }
      workerDemand[r.fromDay]--;
      workerDemand[r.toDay]++;
    }
    // Fix necessary worker reschedules!!

    if (wf.workersNeeded > 1) {
      System.out.println();
    }

    if (wf.workersNeeded == 0) {
      System.out.println();
    }

    if (wf.delayedTasks.size() > 0 || wf.unfinishedTasks.size() > 0) askContractorForMoreManpower(day, wf.firstDelay, wf.workersNeeded, wf.delayedTasks, wf.unfinishedTasks);
  }

  private WorkerForecast forecastWorkerSupply(int today, Task t, int[] currentWorkerSupply) {
    double remainingQuantity = t.getRemainingQuantity();
    double production = t.productionRate;
    double optimalWorkers = t.optimalWorkerCount;
    double roundOff = 0;
    WorkerForecast wf = new WorkerForecast();
    // List<WorkerReschedule> wr = new ArrayList<>();
    int[] understaffedDay = new int[currentWorkerSupply.length*2];
    int canBeStarted = Math.max(t.scheduledStart, today);
    for (int i = canBeStarted; i < understaffedDay.length; i++) understaffedDay[i] = (int) optimalWorkers;
    int day = t.scheduledStart;
    while (remainingQuantity > 0) {
      // Go through schedule, assigning workers
      int workersOnDay = 0;
      if (remainingQuantity >= t.productionRate) {
        workersOnDay = t.optimalWorkerCount;
      } else {
        double workersNeeded = Math.ceil(remainingQuantity / production * optimalWorkers);
        workersOnDay = (int) workersNeeded;
      }

      workersOnDay = Math.min(workersOnDay, currentWorkerSupply[day]);

      if (workersOnDay > 0 && remainingQuantity == t.getRemainingQuantity()) {
        // Set the scheduled finish to first day of supplying workers
        t.scheduledStart = day;
      }

      currentWorkerSupply[day] -= workersOnDay;
      understaffedDay[day] -= workersOnDay;
      remainingQuantity -= workersOnDay / optimalWorkers * production;
      roundOff = Math.floor(remainingQuantity * 1000.0) / 1000.0;
      if (roundOff != remainingQuantity) { remainingQuantity = roundOff; }

      if (day == t.scheduledFinish) {
        wf.addDelayedTask(t);
        if (day <= wf.firstDelay) wf.firstDelay = day-1;
      }

      day++;

      // Check if we can supply more workers to get task done within scheduled finish
      if (day == t.scheduledFinish && remainingQuantity > 0 && t.scheduledFinish > 0) {
        int earlierDay = t.scheduledStart;
        while (remainingQuantity > 0) {
          while (currentWorkerSupply[earlierDay] > 0) {
            remainingQuantity -= 1.0 / optimalWorkers * production;
            roundOff = Math.floor(remainingQuantity * 1000.0) / 1000.0;
            if (roundOff != remainingQuantity) { remainingQuantity = roundOff; }
            currentWorkerSupply[earlierDay]--;
            if (remainingQuantity <= 0) break;
          }
          earlierDay++;
          if (earlierDay > day || earlierDay >= currentWorkerSupply.length) break;
        }
      }

      // Check if we need to re-arrange workers to fulfill tasks
      if (day >= currentWorkerSupply.length) {
        int earlierDay = 0;
        while (remainingQuantity > 0) {
          while (currentWorkerSupply[earlierDay] > 0) {
            remainingQuantity -= 1.0 / optimalWorkers * production;
            roundOff = Math.floor(remainingQuantity * 1000.0) / 1000.0;
            if (roundOff != remainingQuantity) { remainingQuantity = roundOff; }
            currentWorkerSupply[earlierDay]--;
            for (int i = 0; i < understaffedDay.length; i++) {
              if (understaffedDay[i] > 0) {
                WorkerReschedule wr = new WorkerReschedule(trade, earlierDay, i);
                wf.addReschedule(wr);
                break;
              }
            }
            if (remainingQuantity <= 0) break;
          }
          earlierDay++;
          if (earlierDay >= currentWorkerSupply.length) break;
        }
      }
      if (day >= currentWorkerSupply.length) break;
    }

    if (remainingQuantity > 0) {
      wf.addUnfinishedTask(t);
      int workersNeeded = (int) Math.ceil(remainingQuantity / production * optimalWorkers);
      // System.out.println("Workers missing: " + workersNeeded);
      wf.workersNeeded += workersNeeded;
    }

    if (wf.workersNeeded == 0) {
      System.out.println();
    }

    wf.contractorSchedule = currentWorkerSupply;
    return wf;
  }

  private int[] resizeArray(int[] arr, int sz) {
    int[] arr2 = new int[sz];
    for (int i = 0; i < arr.length; i++) {
      arr2[i] = arr[i];
    }
    return arr2;
  }

  public void askContractorForMoreManpower(int day, int firstDelay, int workersMissing, List<Task> delayedTasks, List<Task> unfinishedTasks) {
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

  private class WorkerForecast {
    public List<WorkerReschedule> reschedules;
    public List<Task> delayedTasks;
    public List<Task> unfinishedTasks;
    public int[] contractorSchedule;
    public int firstDelay;
    public int workersNeeded;

    public WorkerForecast() {
      this.reschedules = new ArrayList<>();
      this.delayedTasks = new ArrayList<>();
      this.unfinishedTasks = new ArrayList<>();
      this.firstDelay = Integer.MAX_VALUE;
      this.workersNeeded = 0;
    }

    public void addReschedule(WorkerReschedule wr) {
      reschedules.add(wr);
    }

    public void addDelayedTask(Task t) {
      delayedTasks.add(t);
    }

    public void addUnfinishedTask(Task t) {
      unfinishedTasks.add(t);
    }
  }

  public class WorkerReschedule {
    Trade trade;
    int fromDay;
    int toDay;
    boolean implemented;

    public WorkerReschedule(Trade trade, int from, int to) {
      this.trade = trade;
      this.fromDay = from;
      this.toDay = to;
      this.implemented = false;
    }

    private int[] implement(int[] contractorSchedule) {
      while (toDay >= contractorSchedule.length) {
        int[] newContractorSchedule = new int[Math.max(contractorSchedule.length*2, toDay)];
        for (int i = 0; i < contractorSchedule.length; i++) {
          newContractorSchedule[i] = contractorSchedule[i];
        }
        contractorSchedule = newContractorSchedule;
      }
      contractorSchedule[fromDay]--;
      contractorSchedule[toDay]++;
      this.implemented = true;
      System.out.println(this);
      return contractorSchedule;
    }

    public boolean isImplemented() {
      return this.implemented;
    }

    public String toString() {
      return trade + ": from " + fromDay + " to " + toDay;
    }
  }
}

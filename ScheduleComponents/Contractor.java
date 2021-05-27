package ScheduleComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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
  public int additionalWorkers;

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
          t.scheduleWorkerAtDay(i, t.optimalWorkerCount);
        } else {
          sufficientWorkers = (int)Math.ceil((remainingQuantity / (double)t.productionRate) * (double)t.optimalWorkerCount);
          workerDemand[i] += sufficientWorkers;
          t.scheduleWorkerAtDay(i, sufficientWorkers);
          break;
        }
      }
    }
    return workerDemand;
  }

  public void addExtraWorkers(int extraWorkers) {
    this.additionalWorkers = extraWorkers;
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
    for (Task t : scheduledTasks) {
      if (workerDemand.length > t.scheduledWorkers.length) {
        t.resizeSchedule(workerDemand.length);
      }
    }
    if (today >= workerDemand.length) return alreadySickWorkers;
    sickWorkers = 0;
    if (!alreadySickWorkers) sickWorkers = checkForSickWorkers(today, workerDemand[today], alarms);
    availableWorkers = workerDemand[today];
    if (sickWorkers > 0) {
      alarms.addDelays(new Alarm(today, scheduledTasks.get(0), trade, "Has sick workers : " + sickWorkers));
      alreadySickWorkers = true;
    }

    List<Task> sortedTasks = new ArrayList<>();
    for (Task t : scheduledTasks) { if (!t.isFinished()) sortedTasks.add(t); }
    Collections.sort(sortedTasks, new SortByWorkerShortage(today));

    for (Task t : sortedTasks) {
      if (!t.isCritical) continue;
      if (t.isFinished() || !t.canBeStarted()) continue;

      int assignedWorkers = 0;
      while (availableWorkers > 0 && assignedWorkers < t.scheduledWorkers[today]) {
        assignWorkers(t, 1, today);
        assignedWorkers++;
      }
    }

    for (Task t : sortedTasks) {
      if (t.isCritical) continue;
      if (t.isFinished() || !t.canBeStarted()) continue;

      int assignedWorkers = 0;
      while (availableWorkers > 0 && assignedWorkers < t.scheduledWorkers[today]) {
        assignWorkers(t, 1, today);
        assignedWorkers++;
      }
    }

    if (availableWorkers > 0) {
      for (Task t : scheduledTasks) {
        if (!t.isFinished() && t.canBeStarted()) {
          if (t.scheduledFinish <= today+1) {
            double progressAfterToday = t.progress + t.transformWorkersToProgress(t.workersAssigned);
            while (progressAfterToday < 100 && availableWorkers > 0) {
              assignWorkers(t, 1, today);
              progressAfterToday = t.progress + t.transformWorkersToProgress(t.workersAssigned);
            }
          }
        }
      }
      if (availableWorkers > 0) {
        while (workerDemand.length <= today+2) workerDemand = resizeArray(workerDemand, today+5);
        workerDemand[today] -= availableWorkers;
        workerDemand[today+1] += availableWorkers;
      }
    }

    for (Task t : scheduledTasks) {
      if (t.scheduledFinish < today-2 && !t.isFinished()) {
        workerDemand[today+2]++;
      }
    }

    return alreadySickWorkers;
  }

  private void assignWorkers(Task t, int w, int day) {
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
        sickWorkers++;
      }
    }
    if (sickWorkers > 0) {
      if (additionalWorkers > 0) {
        additionalWorkers--;
        return 0;
      } else {
        workerDemand[day]--;
        return 1;
      }
    }
    return 0;
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
      wf.contractorSchedule = wf2.contractorSchedule;
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
      wf.contractorSchedule = wf2.contractorSchedule;
      if (wf2.firstDelay < wf.firstDelay) wf.firstDelay = wf2.firstDelay;
      wf.workersNeeded += wf2.workersNeeded;
    }

    for (WorkerReschedule r : wf.reschedules) {
      while (r.toDay >= workerDemand.length) {
        int[] newWorkerDemand = new int[r.toDay+2];
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
      System.out.printf("");
    }

    if (wf.workersNeeded == 0) {
      System.out.printf("");
    }

    if (wf.delayedTasks.size() > 0 || wf.unfinishedTasks.size() > 0) askContractorForMoreManpower(day, wf.firstDelay, wf.workersNeeded, wf.delayedTasks, wf.unfinishedTasks);
  }

  private WorkerForecast forecastWorkerSupply(int today, Task t, int[] currentWorkerSupply) {
    double remainingQuantity = t.getRemainingQuantity();
    double production = t.productionRate;
    double optimalWorkers = t.optimalWorkerCount;
    double roundOff = 0;
    WorkerForecast wf = new WorkerForecast();
    int[] understaffedDay = new int[currentWorkerSupply.length+2];
    int canBeStarted = Math.max(t.scheduledStart, today);
    for (int i = canBeStarted; i < understaffedDay.length; i++) understaffedDay[i] = (int) optimalWorkers;
    int day = t.scheduledStart;
    int lastDayOfWorkForTask = 0;
    t.resetScheduledWorkers(today);
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
      t.scheduleWorkerAtDay(day, workersOnDay);
      if (day > lastDayOfWorkForTask && workersOnDay > 0) lastDayOfWorkForTask = day;

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
            t.scheduleWorkerAtDay(day, 1);
            if (day > lastDayOfWorkForTask) lastDayOfWorkForTask = day;
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
            t.scheduleWorkerAtDay(day, 1);
            if (earlierDay > lastDayOfWorkForTask) lastDayOfWorkForTask = earlierDay; 
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

    t.scheduledFinish = lastDayOfWorkForTask+1;

    if (remainingQuantity > 0) {
      wf.addUnfinishedTask(t);
      int workersNeeded = (int) Math.ceil(remainingQuantity / production * optimalWorkers);
      wf.workersNeeded += workersNeeded;
    }

    if (wf.workersNeeded == 0) {
      System.out.printf("");
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
    // String prompt = "\n\n # To " + trade + ":\n" +
      // "You need to provide " + workersMissing + " more manpower (workers) from " +
      // "working overtime/weekends or by hiring more workers. \n";
    int daysLeftBeforeDelay = firstDelay - day+1;
    String delayedTask = "[";
    for (Task t : delayedTasks) {
      delayedTask += "L" + t.location + "T" + t.id + ";";
    }
    delayedTask = delayedTask.substring(0, delayedTask.length()-1) + "]";
    if (daysLeftBeforeDelay <= 0) {
      // prompt += "There is not enough time to avoid a delay... \n";
    } else {
      if (delayedTask.length() > 1) {
        // prompt += "If the extra manpower is " +
        // "not provided in the next " + daysLeftBeforeDelay + " days, the following tasks will " +
        // "be delayed: " + delayedTask + ".\n";
      } else {
        // prompt += "The extra manpower needs to be provided in the next " + daysLeftBeforeDelay + " days! \n";
      }
    }
    if (unfinishedTasks.size() > 0) {
      String unfinished = "[";
      for (Task t : unfinishedTasks) {
        unfinished += "L" + t.location + "T" + t.id + ";";
      }
      unfinished = unfinished.substring(0, unfinished.length()-1) + "]";
      // prompt += "There will not be enough manpower to finish the following tasks: " + unfinished;
    } else {
      // prompt += "All tasks should still be able to be finished.";
    }
    // prompt += "\nWriting '1' means supplying a worker tomorrow.";
    // System.err.println(prompt);

    int dayOfSupply = 0;
    for (int i = 0; i < workersMissing; i++) {
      // dayOfSupply = getResponse(day, i);
      dayOfSupply = getRandomResponse(day, i, daysLeftBeforeDelay);
      addWorkerToSchedule(dayOfSupply);
    }
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
  }

  public void alignSchedule(int today) {
    int longestSchedule = 0;
    for (Task t : scheduledTasks) {
      if (t.scheduledWorkers.length > longestSchedule) longestSchedule = t.scheduledWorkers.length;
    }

    if (longestSchedule > workerDemand.length) workerDemand = resizeArray(workerDemand, longestSchedule);

    int[] sumByDay = new int[longestSchedule];
    for (Task t : scheduledTasks) {
      double remainingQuantity = t.getRemainingQuantity();
      double workersNeeded = remainingQuantity / (double) t.productionRate * (double) t.optimalWorkerCount;
      double workersScheduled = 0;
      for (int i = today; i < t.scheduledWorkers.length; i++) {
        sumByDay[i] += t.scheduledWorkers[i];
        workersScheduled += t.scheduledWorkers[i];
      }
      while (workersScheduled < workersNeeded) {
        if (t.scheduledWorkers.length <= today+5) resizeArray(t.scheduledWorkers, today+10);
        if (today+1 > t.scheduledWorkers.length) {
          System.out.println("");
        }
        t.scheduledWorkers[today+1]++;
        t.scheduledFinish = today+2;
        sumByDay[today+1]++;
        workersScheduled++;
      }
    }

    int[] tooMany = new int[workerDemand.length];
    int surplus = 0;
    int[] tooFew = new int[workerDemand.length];
    int shortage = 0;
    for (int i = today; i < workerDemand.length; i++) {
      while (sumByDay.length <= i+2) sumByDay = resizeArray(sumByDay, i+5);
      if (workerDemand[i] < sumByDay[i]) {
        tooFew[i] = sumByDay[i] - workerDemand[i];
        shortage += tooFew[i];
      } else if (workerDemand[i] > sumByDay[i]) {
        tooMany[i] = workerDemand[i] - sumByDay[i];
        surplus += tooMany[i];
      }
    }

    while (surplus - shortage != 0) {
      if (surplus > shortage) {
        for (int i = workerDemand.length-1; i >= today; i--) {
          if (workerDemand[i] > sumByDay[i]) {
            workerDemand[i]--;
            surplus--;
            break;
          }
        }
      } else if (shortage > surplus) {
        for (int i = workerDemand.length-1; i >= today; i--) {
          if (workerDemand[i] < sumByDay[i]) {
            workerDemand[i]++;
            shortage--;
            break;
          }
        }
      }
    }

    List<WorkerReschedule> reschedules = new ArrayList<>();
    if (surplus != 0 || shortage != 0) {
      for (int i = 0; i < tooFew.length; i++) {
        while (tooFew[i] > 0) {
          for (int j = 0; j < tooMany.length; j++) {
            if (tooMany[j] > 0) {
              reschedules.add(new WorkerReschedule(trade, j, i));
              tooFew[i]--;
              tooMany[i]--;
            }
          }
        }
      }
    }

    for (WorkerReschedule r : reschedules) {
      Task t = null;
      for (Task t2 : scheduledTasks) {
        if (t2.scheduledStart <= r.fromDay && t2.scheduledStart <= r.toDay && 
            t2.scheduledFinish > r.fromDay && t2.scheduledFinish > r.toDay &&
            t2.scheduledWorkers[r.toDay] > 0) {
          t = t2;
          t2.scheduledWorkers[r.fromDay]++;
          t2.scheduledWorkers[r.toDay]--;
          r.implemented = true;
          break;
        }
      }

      if (t == null) {
        workerDemand[r.fromDay]--;
        workerDemand[r.toDay]++;
        r.implemented = true;
        continue;
      }
    }
  }

  public void printScheduleAndTasks(int day) {
    String workersSchedule = "";

    for (int i = 0; i < workerDemand.length; i++) {
      if (i == day+1) {
        workersSchedule += "  || ";
      }
      workersSchedule += String.format(" %3d ", workerDemand[i]);
    }

    String taskSchedules = "";
    int longestSchedule = 0;
    int[] sumByDay = new int[workerDemand.length];
    for (Task t : scheduledTasks) {
      int neededWorkers = (int) Math.ceil(t.getRemainingQuantity() / (double)t.productionRate * (double)t.optimalWorkerCount);
      taskSchedules += String.format("%n%23s:  (%3d) ", "L" + t.location + "T" + t.id, neededWorkers);
      for (int i = 0; i < t.scheduledWorkers.length; i++) {
        if (t.scheduledWorkers.length > sumByDay.length) sumByDay = resizeArray(sumByDay, t.scheduledWorkers.length);
        if (i == day+1) {
          taskSchedules += "  || ";
        }
        if (i == t.scheduledStart) {
          taskSchedules += String.format("<%3d ", t.scheduledWorkers[i]);
          sumByDay[i] += t.scheduledWorkers[i];
        } else if (i == t.scheduledFinish-1) {
          taskSchedules += String.format(" %3d>", t.scheduledWorkers[i]);
          sumByDay[i] += t.scheduledWorkers[i];
        } else {
          taskSchedules += String.format(" %3d ", t.scheduledWorkers[i]);
          sumByDay[i] += t.scheduledWorkers[i];
        }
      }
      if (t.scheduledWorkers.length > longestSchedule) longestSchedule = t.scheduledWorkers.length;
    }
    String indices = "";
    for (int i = 0; i < longestSchedule; i++) {
      if (i == day+1) {
        indices += "  || ";
      }
      indices += String.format(" %3d ", i); 
    }
    System.out.printf("%n%30s: " + indices, "indices");
    System.out.printf("%n%30s: " + workersSchedule, trade);
    System.out.println(taskSchedules);
    String sumString = "";
    for (int i = 0; i < sumByDay.length; i++) {
      if (i == day+1) {
        sumString += "  || ";
        sumString += String.format(" %3d ", sumByDay[i]);
      } else {
        sumString += String.format(" %3d ", sumByDay[i]);
      }
    }
    System.out.printf("%30s: " + sumString + "%n", "sum");
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

  private class SortByWorkerShortage implements Comparator<Task> {
    private int day;

    public SortByWorkerShortage(int day) {
      this.day = day;
    }
    @Override
    public int compare(Task t1, Task t2) {
      int t1WorkerShortage = 0;
      for (int i = 0; i < day && i < t1.scheduledWorkers.length; i++) {
        t1WorkerShortage += t1.scheduledWorkers[i];
      }
      int t2WorkerShortage = 0;
      for (int i = 0; i < day && i < t2.scheduledWorkers.length; i++) {
        t2WorkerShortage += t2.scheduledWorkers[i];
      }

      if (t1WorkerShortage == t2WorkerShortage) return t1.scheduledFinish - t2.scheduledFinish;
      return t1WorkerShortage - t2WorkerShortage;
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

    public boolean isImplemented() {
      return this.implemented;
    }

    public String toString() {
      return trade + ": from " + fromDay + " to " + toDay;
    }
  }
}

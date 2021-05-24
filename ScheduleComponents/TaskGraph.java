package ScheduleComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ScheduleComponents.Contractor.Trade;

public class TaskGraph {
  
  public List<Task> tasks;
  public int locations;
  public int tasksPerLocation;
  public int estimatedDeadline;
  public int scheduledDeadline;

  public TaskGraph(int locations, int tasksPerLocation) {
    this.tasks = new ArrayList<>();
    this.locations = locations;
    this.tasksPerLocation = tasksPerLocation;
  }

  /**
   * Adds a task to the allTask list and returns the task.
   * @param taskParameters is an array of strings with task metadata
   * @return the task as a Task object
   */
  public Task addTask(String[] taskParameters) {
    Task t = new Task(taskParameters);
    tasks.add(t);
    return t;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  /**
   * Adds a dependency between two tasks; adds task 1 to the other task's
   * successor list and task 2 to the first task's predecessor list. 
   * @param predecessor is the task preceding the other task
   * @param successor is the task succeeding the first task
   */
  public void addTaskDependency(Task predecessor, Task successor) {
    predecessor.addSuccessor(successor);
    successor.addPredecessor(predecessor);
  }

  /**
   * This function is called when the schedule needs to be reset from a 
   * given day (the day before tomorrow). All timings will be reset and 
   * recalculated, so ES/EF/LS/LF timings are correct.
   * 
   * Subsequently, it will be determined whether we have a new critical path.
   * @param tomorrow
   */
  public void calculateTimingsAndFloats(int tomorrow) {
    for (Task t : tasks) {
      if (!t.isFinished() && t.progress > 0) 
        t.recalculateDuration();
    }
    resetTimingsOfTasks();
    forwardPass(tomorrow);
    backwardPass();
    calculateFloat();
  }

  public void resetTimingsOfTasks() {
    for (Task t : tasks) {
      t.latestStart = Integer.MAX_VALUE;
      t.latestFinish = Integer.MAX_VALUE;
    }
  }

  /**
   * Iterates through all tasks, starting with task 0, and determines the 
   * earliest start and earliest finish variables. 
   */
  public void forwardPass(int day) {
    int estimate = 0;
    for (Task t : tasks) {
      if (t.isFinished()) continue;
      boolean nextTask = true;
      for (Task t2 : t.predecessorTasks) {
        if (!t2.isFinished()) nextTask = false;
      }
      if (nextTask) {
        estimate = forwardPass(t, day);
        if (estimate > estimatedDeadline) {
          estimatedDeadline = estimate;
        }
      }
    }
  }

  public int forwardPass(Task t, int day) {
    if (day > t.earliestStart) {
      t.earliestStart = day;
    }
    if (day + t.meanDuration > t.earliestFinish) {
      t.earliestFinish = day + t.meanDuration;
    }
    int estimate = 0;
    int latestFinish = t.earliestFinish;
    for (Task t2 : t.successorTasks) {
      estimate = forwardPass(t2, t.earliestFinish);
      if (estimate > latestFinish) latestFinish = estimate;
    }
    return latestFinish;
  }

  /**
   * Iterates through all tasks reversed, starting with the last task. 
   * Here, latest start and latest finish is calculated. 
   */
  public void backwardPass() {
    for (Task t : tasks) {
      if (t.successorTasks.size() == 0) {
        backwardPass(t, estimatedDeadline);
      }
    }
  }

  public void backwardPass(Task t, int deadline) {
    if (deadline < t.latestFinish) {
      t.latestFinish = deadline;
    }
    if (deadline - t.meanDuration < t.latestStart) {
      t.latestStart = deadline - t.meanDuration;
    }
    for (Task t2 : t.predecessorTasks) {
      backwardPass(t2, t.latestStart);
    }
  }

  /**
   * Calculates the maximum time available and the float of a task.
   */
  public void calculateFloat() {
    for (Task t : tasks) {
      if (t.isFinished()) {
        t.isCritical = false;
        continue;
      }
      t.maximumTime = t.latestFinish - t.earliestStart;
      t.taskFloat = t.maximumTime - t.meanDuration;
      if (t.taskFloat == 0) {
        if (!t.isCritical) {
        }
        t.isCritical = true;
      } else {
        t.isCritical = false;
        if (t.isCritical) {
        }
      }
    }
  }
  
  public int scheduledDeadline() {
    scheduledDeadline = 0;
    for (Task t : tasks) {
      if (t.scheduledFinish > scheduledDeadline) scheduledDeadline = t.scheduledFinish;
    }
    return scheduledDeadline;
  }

  /**
   * This function goes through all tasks and compares with the scheduled workforce
   * in the associated contractors. Once all worker reschedules have been determined
   * and the scheduled finishes of tasks have been found, the function implements
   * the worker reschedules in the contractor schedules and returns the updated schedule.
   * @param contractorSchedules is the map with contractor types and their schedules
   * @param tomorrow is the day, from which we need to create a new overview of when tasks are scheduled to finish
   * @return an updated map with contractor types and their worker schedules
   */
  public Map<Trade, int[]> determineScheduledTimings(Map<Trade, int[]> contractorSchedules, int tomorrow, Workforce w) {
    Map<Trade, int[]> copiedSchedules = new HashMap<>();
    for (Entry<Trade, int[]> e : contractorSchedules.entrySet()) {
      copiedSchedules.put(e.getKey(), e.getValue().clone());
    }

    int[][] adj = new int[locations][tasksPerLocation];
    List<WorkerReschedule> wr = new ArrayList<>();
    for (Task t : tasks) {
      if (t.isCritical && adj[t.location][t.id] == 0 && !t.isFinished()) {
        boolean nextTask = true;
        for (Task t2 : t.predecessorTasks) {
          if (!t2.isFinished()) {
            nextTask = false;
          }
        }
        if (nextTask) {
          Tuple tuple = calculateTaskProduction(t, copiedSchedules, tomorrow, adj);
          for (Trade trade : copiedSchedules.keySet()) {
            copiedSchedules.put(trade, tuple.constructorSchedules.get(trade));
          }
          tuple.reschedules.forEach(tup -> wr.add(tup));
        }
      }
    }
    for (Task t : tasks) {
      if (!t.isCritical && adj[t.location][t.id] == 0 && !t.isFinished()) {
        boolean nextTask = true;
        for (Task t2 : t.predecessorTasks) {
          if (!t2.isFinished()) {
            nextTask = false;
          }
        }
        if (nextTask) {
          Tuple tuple = calculateTaskProduction(t, copiedSchedules, tomorrow, adj);
          for (Trade trade : copiedSchedules.keySet()) {
            copiedSchedules.put(trade, tuple.constructorSchedules.get(trade));
          }
          tuple.reschedules.forEach(tup -> wr.add(tup));
        }
      }
    }

    // reduceReschedules(wr);
    for (Trade t : contractorSchedules.keySet()) {
      for (WorkerReschedule r : wr) {
        if (r.trade.equals(t) && !r.implemented) {
          int[] contractorSchedule = contractorSchedules.get(t);
          while (r.toDay >= contractorSchedule.length) {
            int[] newContractorSchedule = new int[Math.max(contractorSchedule.length*2, r.toDay)];
            for (int i = 0; i < contractorSchedule.length; i++) {
              newContractorSchedule[i] = contractorSchedule[i];
            }
            contractorSchedule = newContractorSchedule;
          }

          contractorSchedules.put(t, contractorSchedule);
          contractorSchedules.get(t)[r.fromDay]--;
          if (contractorSchedules.get(t)[r.fromDay] < 0) {
            System.out.printf("");
          }
          contractorSchedules.get(t)[r.toDay]++;
          r.implemented = true;
        }
      }
    }

    scheduledDeadline();
    return contractorSchedules;
  }

  public void reduceReschedules(List<WorkerReschedule> wr) {
    List<WorkerReschedule> wrToBeDeleted = new ArrayList<>();

    for (int i = 0; i < wr.size(); i++) {
      WorkerReschedule r1 = wr.get(i);
      for (int j = i+1; j < wr.size(); j++) {
        WorkerReschedule r2 = wr.get(j);
        if (r1.trade == r2.trade) {
          if (r1.toDay == r2.fromDay) {
            wr.add(new WorkerReschedule(r1.trade, r1.fromDay, r2.toDay));
            wrToBeDeleted.add(r1);
            wrToBeDeleted.add(r2);
          } else if (r2.toDay == r1.fromDay) {
            wr.add(new WorkerReschedule(r1.trade, r2.fromDay, r1.toDay));
            wrToBeDeleted.add(r1);
            wrToBeDeleted.add(r2);
          }
        }
      }
    }

    for (WorkerReschedule r : wrToBeDeleted) {
      wr.remove(r);
    }
  }

  /**
   * Goes through all tasks, the associated contractors and their schedules. The point
   * is to estimate (based on the scheduled workforce) when the tasks will actually
   * be finished. The function goes through tasks from preceding tasks to succeeding
   * tasks.
   * @param t is the task, we want to calculate scheduled finish for
   * @param copiedSchedules is a map structure with all trades and their schedules
   * @param tomorrow is the coming day, from which we want to predict task finishes
   * @param adj is the adjacency matrix, keeping track of the visited tasks
   * @return a tuple, where the interesting part is the list of worker reschedules (wr)
   */
  private Tuple calculateTaskProduction(Task t, Map<Trade, int[]> copiedSchedules, int tomorrow, int[][] adj) {
    // First, check if there are predecessor tasks, who we need to calculate production
    // for before analysing this task (t)
    double roundOff = 0;
    int[] contractorSchedule = copiedSchedules.get(t.trade);
    int earliestScheduledStart = tomorrow;
    int predecessorFinish = 0;
    List<WorkerReschedule> wr = new ArrayList<>();
    if (t.trade == Trade.Carpenter) {
      System.out.println();
    }
    for (Task t2 : t.predecessorTasks) {
      if (t2.isFinished()) continue;
      if (adj[t2.location][t2.id] == 0) {
        Tuple p = calculateTaskProduction(t2, copiedSchedules, 0, adj);
        p.reschedules.forEach(tup -> wr.add(tup));
        predecessorFinish = p.scheduledFinish;
        copiedSchedules = p.constructorSchedules;
        contractorSchedule = copiedSchedules.get(t.trade);
        adj[t2.location][t2.id] = 1;
        if (predecessorFinish > earliestScheduledStart) earliestScheduledStart = predecessorFinish;
      } else {
        if (t2.scheduledFinish > earliestScheduledStart) earliestScheduledStart = t2.scheduledFinish;
      }
    }

    // If this task has not already been gone through (adj[] == 1), then go
    // through the worker supply of the contractor to determine the scheduled finish
    t.scheduledStart = earliestScheduledStart;
    double remainingQuantity = 0;
    double production = 0;
    if (adj[t.location][t.id] == 0) {
      remainingQuantity = t.getRemainingQuantity();
      int i = t.scheduledStart;
      int taskFinish = 0;
      while (i >= contractorSchedule.length) {
        int[] newContractorSchedule = new int[contractorSchedule.length*2];
        for (int j = 0; j < contractorSchedule.length; j++) {
          newContractorSchedule[j] = contractorSchedule[j];
        }
        contractorSchedule = newContractorSchedule;
      }
      int[] shortStaffedDays = new int[contractorSchedule.length*2];
      for (int j = tomorrow; j < shortStaffedDays.length; j++) {
        shortStaffedDays[j] = t.optimalWorkerCount;
      }

      while (remainingQuantity > 0) {
        roundOff = Math.floor(remainingQuantity * 1000.0) / 1000.0;
        if (roundOff != remainingQuantity) { remainingQuantity = roundOff; }

        if (i < contractorSchedule.length) {
          int sufficientWorkers = (int)Math.ceil(remainingQuantity / (double)t.productionRate * (double)t.optimalWorkerCount);
          sufficientWorkers = Math.min(sufficientWorkers, t.optimalWorkerCount);
          sufficientWorkers = Math.min(sufficientWorkers, contractorSchedule[i]);
          production = (double)sufficientWorkers / (double)t.optimalWorkerCount * (double)t.productionRate;
          contractorSchedule[i] -= sufficientWorkers;
          shortStaffedDays[i] -= sufficientWorkers;
          if (sufficientWorkers > 0 && i > taskFinish) taskFinish = i;
          remainingQuantity -= production;
          
          if (i >= t.scheduledFinish && t.scheduledFinish != 0 && remainingQuantity > 0) {
            int d = t.scheduledStart;
            while (remainingQuantity > 0) {
              roundOff = Math.floor(remainingQuantity * 1000.0) / 1000.0;
              if (roundOff != remainingQuantity) { remainingQuantity = roundOff; }
              if (contractorSchedule[d] > 0) {
                production = 1.0 / (double)t.optimalWorkerCount * (double)t.productionRate;
                remainingQuantity -= production;
                contractorSchedule[d]--;
              } else {
                d++;
              }
              if (d > i) break;
            }
          }

        } else {
          // If the task has not been finished, check if we can re-arrange workers
          // so the remaining quantity can be completed
          double productionPerWorker = (double)t.productionRate / (double)t.optimalWorkerCount;
          int understaffedDay = 0;
          for (int day = 0; day < contractorSchedule.length; day++) {
            if (contractorSchedule[day] > 0) {
              for (int j = day; j < shortStaffedDays.length; j++) {
                if (shortStaffedDays[j] > 0) {
                  understaffedDay = j;
                  break;
                }
              }
              wr.add(new WorkerReschedule(t.trade, day, understaffedDay));
              remainingQuantity -= productionPerWorker;
              contractorSchedule[day]--;
              shortStaffedDays[understaffedDay]--;
              if (contractorSchedule[day] > 0) {
                day--;
              }
            }
            if (understaffedDay > taskFinish) taskFinish = understaffedDay;
            if (remainingQuantity <= 0) { break; }
          }

          if (remainingQuantity > 0) { 
            System.out.println(t.trade + " not finishing task L" + t.location + "T" + t.id + "!!");
          }
        }

        i++;
      }
      t.scheduledFinish = taskFinish+1;
      t.scheduledDuration = t.scheduledFinish - t.scheduledStart;
    }

    // Go through succeeding tasks and retrieve their worker reschedules
    adj[t.location][t.id] = 1;
    for (Task t2 : t.successorTasks) {
      if (!t2.isCritical) continue;
      Tuple p = calculateTaskProduction(t2, copiedSchedules, t.scheduledFinish, adj);
      p.reschedules.forEach(tup -> wr.add(tup));
      copiedSchedules = p.constructorSchedules;
      contractorSchedule = copiedSchedules.get(t.trade);
    }
    for (Task t2 : t.successorTasks) {
      if (t2.isCritical) continue;
      Tuple p = calculateTaskProduction(t2, copiedSchedules, t.scheduledFinish, adj);
      p.reschedules.forEach(tup -> wr.add(tup));
      copiedSchedules = p.constructorSchedules;
      contractorSchedule = copiedSchedules.get(t.trade);
    }
    
    // scheduledFinish and copiedSchedules is used in the recursive calls of
    // this function. The worker reschedules (wr) is used to collect the
    // information found throughout the recursive calls.
    copiedSchedules.put(t.trade, contractorSchedule);
    return new Tuple(t.scheduledFinish, copiedSchedules, wr);
  }

  /**
   * Iterates over all tasks, finding the ones assigned to a given trade. 
   * Once the relevant tasks have been found, the function determines when
   * (at the latest) the contractor needs to put a certain amount of workers
   * in order to not postpone the project deadline.
   * @param trade is the trade whose worker demand we want
   * @return an int array, denoting how many workers should be assigned each day
   */
  public int[] forecastWorkerDemand(Trade trade) {
    int[] workerDemand = new int[estimatedDeadline+1];
    for (Task t : tasks) {
      if (t.isFinished()) continue;
      if (t.trade.equals(trade)) {
        double remainingQuantity = t.getRemainingQuantity();
        double productionRate = t.productionRate;
        double optimalWorkers = t.optimalWorkerCount;
        int sufficientWorkers = 0;
        int i = 0;
        while (remainingQuantity > 0) {
          double roundOff = Math.floor(remainingQuantity * 1000.0) / 1000.0;
          if (roundOff != remainingQuantity) { remainingQuantity = roundOff; }
          if (remainingQuantity >= productionRate) {
            workerDemand[t.latestFinish-i] += optimalWorkers;
            remainingQuantity -= productionRate;
          } else {
            double neededWorkers = (remainingQuantity / productionRate) * optimalWorkers;
            if (Math.ceil(neededWorkers) != Math.ceil(Math.round(neededWorkers)))
            if (Math.abs(neededWorkers % 1) < 0.001) neededWorkers = Math.round(neededWorkers);
            neededWorkers = Math.ceil(neededWorkers);
            sufficientWorkers = (int)Math.ceil((remainingQuantity / productionRate) * optimalWorkers);
            if (neededWorkers != sufficientWorkers) {
              System.out.println("");
            }
            workerDemand[t.latestFinish-i] += sufficientWorkers;
            remainingQuantity = 0;
          }
          i++;
        }
      }
    }

    return workerDemand;
  }

  /**
   * Iterates over all tasks, incrementing a counter for each task
   * that has not yet been finished. 
   * @return the incremented counter, equal to the number of tasks remaining
   */
  public int numberOfRemainingTasks() {
    int remainingTasks = 0;
    for (Task t : tasks) {
      if (!t.isFinished()) remainingTasks++;
    }

    return remainingTasks;
  }

  public void printTasksWithDependencies(int locations, int tasksPerLocation) {
    int[][] adj = new int[locations][tasksPerLocation];
    for (Task t : tasks) t.printWithDependencies(adj, 1);
  }
  
  /**
   * Prints the current critical path
   */
  public void printCriticalPath() {
    System.out.println("Critical path activities:");
    for (int i = 0; i < tasks.size(); i++) {
      Task t = tasks.get(i);
      if (!t.isCritical) continue;
      boolean nextTask = true;
      for (Task t2 : t.predecessorTasks) {
        if (t2.isCritical && !t2.isFinished()) nextTask = false;
      }
      if (!nextTask) continue;
      System.out.println("L" + t.location + "T" + t.id + ": " + t.activity);
      while (t != null) {
        boolean end = true;
        for (Task t2 : t.successorTasks) {
          if (t2.isCritical) {
            t = t2;
            end = false;
            System.out.println("L" + t.location + "T" + t.id + ": " + t.activity);
          }
        }
  
        if (end) break;
      }
    }
  }

  private class Tuple {
    public int scheduledFinish;
    public Map<Trade, int[]> constructorSchedules;
    public List<WorkerReschedule> reschedules;

    public Tuple(int scheduledFinish, Map<Trade, int[]> constructorSchedules, List<WorkerReschedule> wr) {
      this.scheduledFinish = scheduledFinish;
      this.constructorSchedules = constructorSchedules;
      if (reschedules == null) reschedules = new ArrayList<>();
      if (wr.size() > 0) wr.forEach(r -> reschedules.add(r));
    }
  }

  private class WorkerReschedule {
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

    public int[] implement(int[] contractorSchedule) {
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

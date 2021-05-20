package ScheduleComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TaskGraph {
  
  public List<Task> tasks;
  public int locations;
  public int tasksPerLocation;
  public int estimatedDeadline;

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
      // t.earliestStart = 0;
      // t.earliestFinish = 0;
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
          System.out.println("New estimated project deadline!");
          estimatedDeadline = estimate;
        }
      }
    }
  }

  public int forwardPass(Task t, int day) {
    if (day > t.earliestStart) {
      // System.out.println(t.location + t.id + ": earliestStart from " + t.earliestStart + " to " + day);
      t.earliestStart = day;
    }
    if (day + t.meanDuration > t.earliestFinish) {
      // System.out.println(t.location + t.id + ": earliestFinish from " + t.earliestFinish + " to " + (day + t.meanDuration));
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

  public void forwardPassWithScheduledTimings(int day) {
    int estimate = 0;
    for (Task t : tasks) {
      if (t.isFinished()) continue;
      boolean nextTask = true;
      for (Task t2 : t.predecessorTasks) {
        if (!t2.isFinished()) nextTask = false;
      }
      if (nextTask) {
        estimate = forwardPassWithScheduledTimings(t, day);
        if (estimate > estimatedDeadline) {
          System.out.println("New estimated project deadline!");
          estimatedDeadline = estimate;
        }
      }
    }
    System.out.println("With scheduled timings!");
  }

  private int forwardPassWithScheduledTimings(Task t, int day) {
    if (day > t.earliestStart) {
      // System.out.println(t.location + t.id + ": earliestStart from " + t.earliestStart + " to " + day);
      t.earliestStart = day;
    }
    if (day + t.meanDuration > t.earliestFinish) {
      // System.out.println(t.location + t.id + ": earliestFinish from " + t.earliestFinish + " to " + (day + t.meanDuration));
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
      // System.out.println(t.location + t.id + ": latestFinish from " + t.latestFinish + " to " + deadline);
      t.latestFinish = deadline;
    }
    if (deadline - t.meanDuration < t.latestStart) {
      // System.out.println(t.location + t.id + ": latestStart from " + t.latestStart + " to " + (deadline - t.meanDuration));
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
          System.out.println("L" + t.location + "T" + t.id + " was not critical but just became critical!");
        }
        t.isCritical = true;
      } else {
        t.isCritical = false;
        if (t.isCritical) {
          System.out.println("L" + t.location + "T" + t.id + " was critical but is not any longer!");
        }
      }
    }
  }

  /**
   * 
   * @param contractorSchedules
   */
  public void determineScheduledTimings(Map<String, int[]> contractorSchedules, int tomorrow) {
    Map<String, int[]> copiedSchedules = new HashMap<>();
    for (Entry<String, int[]> e : contractorSchedules.entrySet()) {
      copiedSchedules.put(e.getKey(), e.getValue().clone());
    }

    String[] startNfinish = new String[tasks.size()];
    int[][] adj = new int[locations][tasksPerLocation];
    for (Task t : tasks) {
      if (t.isCritical) {
        boolean nextTask = true;
        for (Task t2 : t.predecessorTasks) {
          if (!t2.isFinished()) {
            nextTask = false;
          }
        }
        if (nextTask) {
          calculateTaskProduction(t, copiedSchedules, tomorrow, adj, startNfinish);
        }
      }
    }
    for (Task t : tasks) {
      if (!t.isCritical) {
        boolean nextTask = true;
        for (Task t2 : t.predecessorTasks) {
          if (!t2.isFinished()) {
            nextTask = false;
          }
        }
        if (nextTask) {
          calculateTaskProduction(t, copiedSchedules, tomorrow, adj, startNfinish);
        }
      }
    }
  }

  /**
   * 
   * @param t
   * @param critical
   * @param contractorSchedule
   */
  private Pair calculateTaskProduction(Task t, Map<String, int[]> copiedSchedules, int tomorrow, int[][] adj, String[] SF) {
    int[] contractorSchedule = copiedSchedules.get(t.trade);
    int earliestScheduledStart = tomorrow;
    int predecessorFinish = 0;
    for (Task t2 : t.predecessorTasks) {
      if (t2.isFinished()) continue;
      if (adj[t2.location][t2.id] == 0) {
        Pair p = calculateTaskProduction(t2, copiedSchedules, 0, adj, SF);
        predecessorFinish = p.scheduledFinish;
        copiedSchedules = p.constructorSchedules;
        adj[t2.location][t2.id] = 1;
        if (predecessorFinish > earliestScheduledStart) earliestScheduledStart = predecessorFinish;
      } else {
        if (t2.scheduledFinish > earliestScheduledStart) earliestScheduledStart = t2.scheduledFinish;
      }
    }
    t.scheduledStart = earliestScheduledStart;
    double remainingQuantity = 0;
    int[] shortStaffedDays = new int[contractorSchedule.length];
    if (adj[t.location][t.id] == 0) {
      remainingQuantity = t.getRemainingQuantity();
      int i = t.scheduledStart;
      SF[t.location * 5 + t.id] += "|S" + t.scheduledStart;
      while (remainingQuantity > 0 && i < contractorSchedule.length) {
        int sufficientWorkers = (int)Math.ceil(remainingQuantity / t.productionRate * t.optimalWorkerCount);
        sufficientWorkers = Math.min(sufficientWorkers, t.optimalWorkerCount);
        sufficientWorkers = Math.min(sufficientWorkers, contractorSchedule[i]);
        if (sufficientWorkers < t.optimalWorkerCount) shortStaffedDays[i] = t.optimalWorkerCount - sufficientWorkers;
        double production = (double)sufficientWorkers / (double)t.optimalWorkerCount * (double)t.productionRate;
        contractorSchedule[i] -= sufficientWorkers;
        remainingQuantity -= production;
        i++;
        if (remainingQuantity <= 0) { 
          t.scheduledFinish = i; 
          SF[t.location * 5 + t.id] += "|F" + t.scheduledFinish;
        }
      }
    }

    if (remainingQuantity > 0) {
      double productionPerWorker = (double)t.productionRate / (double)t.optimalWorkerCount;
      int understaffedDay = 0;
      for (int day = 0; day < contractorSchedule.length; day++) {
        if (contractorSchedule[day] > 0) {
          for (int j = day; j < shortStaffedDays.length; j++) {
            if (shortStaffedDays[j] > 0) {
              understaffedDay = j;
              shortStaffedDays[j]--;
              break;
            }
          }
          System.out.println(t.trade + ": A worker will be idle on day " + day + " and should be " +
          "rescheduled to day " + understaffedDay);
          remainingQuantity -= productionPerWorker;
          contractorSchedule[day]--;
          contractorSchedule[understaffedDay]++;
          day--;
        }
        if (remainingQuantity <= 0) {
          t.scheduledFinish = understaffedDay+1;
          SF[t.location * 5 + t.id] += "|F" + t.scheduledFinish;
          break;
        }
      }
    }

    adj[t.location][t.id] = 1;
    for (Task t2 : t.successorTasks) {
      if (!t2.isCritical) continue;
      calculateTaskProduction(t2, copiedSchedules, t.scheduledFinish, adj, SF);
    }
    for (Task t2 : t.successorTasks) {
      if (t2.isCritical) continue;
      calculateTaskProduction(t2, copiedSchedules, t.scheduledFinish, adj, SF);
    }

    return new Pair(t.scheduledFinish, copiedSchedules);
    // return t.scheduledFinish;











    // if (t.isFinished()) return;
    // if (t.isCritical != critical) return;
    // double remainingQuantity = t.getRemainingQuantity();
    
    // int day = t.earliestStart;
    // t.scheduledStart = Integer.MAX_VALUE;
    // t.scheduledFinish = Integer.MAX_VALUE;
    // while (remainingQuantity > 0) {
    //   if (day >= contractorSchedule.length) {
    //     System.out.println("");
    //   }
    //   if (day < t.scheduledStart) t.scheduledStart = day;
    //   int sufficientWorkers = (int)Math.ceil(remainingQuantity / t.productionRate * t.optimalWorkerCount);
    //   sufficientWorkers = Math.min(sufficientWorkers, t.optimalWorkerCount);
    //   int workersScheduled = Math.min(contractorSchedule[day], sufficientWorkers);
    //   double production = (double)workersScheduled / (double)t.optimalWorkerCount * (double)t.productionRate;
    //   remainingQuantity -= production;
    //   contractorSchedule[day] -= workersScheduled;
    //   day++;
    //   if (remainingQuantity <= 0) t.scheduledFinish = day;
    // }

    // if (cascade) {
    //   for (Task t2 : t.successorTasks) {
    //     if (t2.trade.equals(t.trade)) calculateTaskProduction(t2, t2.isCritical, contractorSchedule, cascade);
    //   }
    // }
  }

  public void determineScheduledTimings(Contractor c) {
    
  }

  /**
   * 
   * @param trade
   * @return
   */
  public int[] forecastWorkerDemand(String trade) {
    int[] workerDemand = new int[estimatedDeadline+1];
    for (Task t : tasks) {
      if (t.isFinished()) continue;
      if (t.trade.equals(trade)) {
        double remainingQuantity = t.getRemainingQuantity();
        double productionRate = t.productionRate;
        int sufficientWorkers = 0;
        int i = 0;
        while (remainingQuantity > 0) {
          if (remainingQuantity >= productionRate) {
            workerDemand[t.latestFinish-i] += t.optimalWorkerCount;
            remainingQuantity -= productionRate;
          } else {
            sufficientWorkers = (int)Math.ceil((remainingQuantity / (double)t.productionRate) * (double)t.optimalWorkerCount);
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
   * 
   * @return
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

  private class Pair {
    public int scheduledFinish;
    public Map<String, int[]> constructorSchedules;

    public Pair(int scheduledFinish, Map<String, int[]> constructorSchedules) {
      this.scheduledFinish = scheduledFinish;
      this.constructorSchedules = constructorSchedules;
    }
  }
}

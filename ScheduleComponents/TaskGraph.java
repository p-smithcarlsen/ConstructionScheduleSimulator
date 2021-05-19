package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class TaskGraph {
  
  public List<Task> tasks;
  public int estimatedDeadline;

  public TaskGraph() {
    this.tasks = new ArrayList<>();
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
    if (day > t.earliestStart) t.earliestStart = day;
    if (day + t.meanDuration > t.earliestFinish) t.earliestFinish = day + t.meanDuration;
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
    if (deadline < t.latestFinish) t.latestFinish = deadline;
    if (deadline - t.meanDuration < t.latestStart) t.latestStart = deadline - t.meanDuration;
    for (Task t2 : t.predecessorTasks) {
      backwardPass(t2, t.latestStart);
    }
  }

  /**
   * Calculates the maximum time available and the float of a task.
   */
  public void calculateFloat() {
    boolean newCriticalPath = false;
    for (Task t : tasks) {
      if (t.isFinished()) {
        t.isCritical = false;
        continue;
      }
      t.maximumTime = t.latestFinish - t.earliestStart;
      t.taskFloat = t.maximumTime - t.meanDuration;
      if (t.taskFloat == 0) {
        if (!t.isCritical) { 
          newCriticalPath = true;
        }
        t.isCritical = true;
      } else {
        t.isCritical = false;
      }
    }

    if (newCriticalPath) {
      System.out.println("NB: We have obtained a new critical path!");
    }
    printCriticalPath();
  }

  public int[] forecastWorkerDemand(String trade) {
    int[] workerDemand = new int[estimatedDeadline+1];
    for (Task t : tasks) {
      if (t.isFinished()) continue;
      if (t.trade.equals(trade)) {
        double remainingQuantity = (1 - (t.progress / 100)) * t.quantity;
        if (Math.abs(remainingQuantity % 1) < 0.000001) remainingQuantity = Math.round(remainingQuantity);
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

  public int numberOfRemainingTasks() {
    int remainingTasks = 0;
    for (Task t : tasks) {
      if (!t.isFinished()) remainingTasks++;
    }

    return remainingTasks;
  }

  /**
   * Prints all critical tasks.
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
      System.out.println(t.location + t.id + ": " + t.activity);
      while (t != null) {
        boolean end = true;
        for (Task t2 : t.successorTasks) {
          if (t2.isCritical) {
            t = t2;
            end = false;
            System.out.println(t.location + t.id + ": " + t.activity);
          }
        }
  
        if (end) break;
      }
    }
  }
}

package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class TaskGraph {
  
  public List<Task> allTasks;
  public List<Task> backlogTasks;
  public List<Task> endTasks = new ArrayList<>();
  public Task criticalPathTasks;
  public List<Task> criticalTasks;
  public int estimatedDeadline;
  public double criticalLastFinish;

  public TaskGraph() {
    this.allTasks = new ArrayList<>();
    this.backlogTasks = new ArrayList<>();
    this.criticalPathTasks = null;
  }

  /**
   * Adds a task to the allTask list and returns the task.
   * @param taskParameters is an array of strings with task metadata
   * @return the task as a Task object
   */
  public Task addTask(String[] taskParameters) {
    Task t = new Task(taskParameters);
    allTasks.add(t);
    return t;
  }

  public List<Task> getBacklogTasks() {
    return backlogTasks;
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
   * Iterates through all tasks, starting with task 0, and determines the 
   * earliest start and earliest finish variables. 
   */
  public void forwardPass() {
    Task t = allTasks.get(0);
    estimatedDeadline = forwardPass(t, 0);
  }

  public int forwardPass(Task t, int time) {
    t.earliestStart = time;
    t.earliestFinish = time + t.meanDuration;
    int estimate = 0;
    int latestFinish = t.earliestFinish;
    for (Task t2 : t.successorTasks) {
      estimate = forwardPass(t2, t.earliestFinish);
      if (estimate > latestFinish) latestFinish = estimate;
    }

    return latestFinish;
  }

  // public void backwardPass() {
  //   Task t = backlogTasks.get(backlogTasks.size()-1);
  //   backwardPass(t, estimatedDeadline);
  // }

  // public void backwardPass(Task t, int deadline) {
  //   t.latestStart = deadline - t.meanDuration;
  //   t.latestFinish = deadline;
  //   for (Task t2 : t.predecessorTasks) {
  //     backwardPass(t2, t.latestStart);
  //   }
  // }

  /**
   * Iterates through all tasks reversed, starting with the last task. 
   * Here, latest start and latest finish is calculated. 
   * @param l
   */
  public void backwardPass(Location l) {
    int end = l.tasks.get(l.tasks.size()-1).earliestFinish;
    Task temp = l.tasks.get(l.tasks.size()-1);
    while (!temp.predecessorTasks.isEmpty()) {
      temp.latestFinish = end;
      temp.latestStart = end - temp.meanDuration +1;
      end = end - temp.meanDuration;
      temp = temp.predecessorTasks.get(0);
    }
    temp.latestFinish = end;
    temp.latestStart = end - temp.meanDuration+1;
  }

  /**
   * Calculates the maximum time available and the float of a task.
   */
  public void calculateFloat() {
    for (Task t : allTasks) {
      t.maximumTime = t.latestFinish - t.earliestStart;
      t.taskFloat = t.maximumTime - t.meanDuration;
    }
  }

// public void backwardPass(){
  
// for (Task task : endTasks) {
//     double lastStartCheck = criticalLastFinish;
//     Task tempTask = task;
//     while (!tempTask.predecessorTasks.isEmpty()) {
//       tempTask.latestFinish = (int) lastStartCheck;
//       tempTask.latestStart = tempTask.latestFinish - tempTask.meanDuration +1;
//       lastStartCheck = tempTask.latestFinish - tempTask.meanDuration;
//       tempTask = tempTask.predecessorTasks.get(0);
//     }
//     tempTask.latestFinish = (int) lastStartCheck;
//     tempTask.latestStart = tempTask.latestFinish - tempTask.meanDuration +1;
//   }
// }

  // public void calculateCriticalPath() {
  //   for (int i = backlogTasks.size()-1; i >= 0; i--) {
  //     if (backlogTasks.get(i).predecessorTasks.size() > 0) backlogTasks.remove(i);
  //   }

  //   List<Task> path = null;
  //   List<Task> criticalPath = null;
  //   double longestDuration = 0;
  //   for (Task t : backlogTasks) {
  //     path = t.earliestPathDuration();
  //     if (t.longestPathDuration > longestDuration) {
  //       longestDuration = t.longestPathDuration;
  //       criticalPath = path;
  
  /**
   * Determines the critical path through all the tasks.
   */
  public void findCriticalPaths() {
    // for (int i = backlogTasks.size()-1; i >= 0; i--) {
    //   if (backlogTasks.get(i).predecessorTasks.size() > 0) backlogTasks.remove(i);
    // }

    // List<Task> path = null;
    // List<Task> criticalPath = null;
    // double longestDuration = 0;
    // for (Task t : allTasks) {
    //   path = t.checkLengthOfPath();
    //   if (t.longestPathDuration > longestDuration) {
    //     longestDuration = t.longestPathDuration;
    //     criticalPath = path;
    //   }
    // }

    // criticalPathTasks = criticalPath.get(criticalPath.size()-1);
    // criticalLastFinish = criticalPathTasks.longestPathDuration;
    // allTasks.remove(criticalPathTasks);
    // for (Task t : criticalPath) {
    //   System.out.println(t.location + t.id);
    //   t.isCritical = true;
    //   allTasks.remove(t);
    // }

    for (Task t : allTasks) {
      allTasks.remove(t);
      if (t.taskFloat > 0) {
        backlogTasks.add(t);
      } else {
        criticalTasks.add(t);
      }
    }

    for (Task t : allTasks) {
      System.out.println("We still have task " + t.location + t.id + " in allTasks");
    }
  }

  /**
   * Prints all critical tasks.
   */
  public void printCriticalPath() {
    Task t = criticalPathTasks;
    while (t != null) {
      for (Task t2 : t.successorTasks) {
        if (t2.isCritical) {
          t = t2;
          System.out.println(t.activity);
        }

        if (t.successorTasks.size() == 0) {
          t = null;
          break;
        }
      }
      break;      // TODO: printer ikke alle
    }
  }

  /**
   * 
   * @param l
   */
  public void locateEndPathTasks(Location l){
    endTasks.add(l.tasks.get(l.tasks.size()-1));
  }
}

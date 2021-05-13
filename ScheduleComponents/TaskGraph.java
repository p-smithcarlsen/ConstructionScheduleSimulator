package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class TaskGraph {
  
  public List<Task> backlogTasks;
  // private int sz;
  public Task criticalPathTasks;

  // public TaskGraph(int sz) {
  public TaskGraph() {
    this.backlogTasks = new ArrayList<>();
    // this.backlogTasks = new Task[sz];
    this.criticalPathTasks = null;
  }

  public Task addTask(String[] taskParameters) {
    Task t = new Task(taskParameters);
    backlogTasks.add(t);
    // backlogTasks[sz] = t;
    // sz++;

    return t;
  }

  public List<Task> getBacklogTasks() {
    return backlogTasks;
  }

  public void addTaskDependency(Task predecessor, Task successor) {
    predecessor.addSuccessor(successor);
    successor.addPredecessor(predecessor);
  }

  public void forwardPass(){
    int crossover = 0;
    for (Task t : backlogTasks) {
      if(t.predecessorTasks.isEmpty()) {
        t.earliestFinish = t.meanDuration -1;
        crossover = t.meanDuration;
      } else {
        t.earliestStart = crossover;
          t.earliestFinish = crossover + t.meanDuration -1;
          crossover = t.earliestFinish +1;
      }
    }
  }

  public void calculateCriticalPath() {
    for (int i = backlogTasks.size()-1; i >= 0; i--) {
      if (backlogTasks.get(i).predecessorTasks.size() > 0) backlogTasks.remove(i);
    }

    List<Task> path = null;
    List<Task> criticalPath = null;
    double longestDuration = 0;
    for (Task t : backlogTasks) {
      path = t.earliestPathDuration();
      if (t.longestPathDuration > longestDuration) {
        longestDuration = t.longestPathDuration;
        criticalPath = path;
      }
    }

    criticalPathTasks = criticalPath.get(criticalPath.size()-1);
    backlogTasks.remove(criticalPathTasks);
    for (Task t : criticalPath) {
      t.isCritical = true;
      backlogTasks.remove(t);
    }
  }

  public void printCriticalPath() {
    Task t = criticalPathTasks;
    System.out.println(t.activity);
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
      break;


      // for (Task t2 : t.successorTasks) {
      //   if (t2.isCritical) {
      //     t = t2;
      //     System.out.println(t.activity);
      //     if (t.successorTasks.size() == 0) break;
      //   } else {
      //     break;
      //   }
      // }
    }
  }
}

package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class TaskGraph {
  
  public List<Task> backlogTasks;
  public List<Task> endTasks = new ArrayList<>();
  // private int sz;
  public Task criticalPathTasks;
  public int estimatedDeadline;
  public double criticalLastFinish;

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

  // public void forwardPass(){
  //   int crossover = 0;
  //   for (Task t : backlogTasks) {
  //     if(t.predecessorTasks.isEmpty()) {
  //       t.earliestFinish = t.meanDuration -1;
  //       crossover = t.meanDuration;
  //     } else {
  //       t.earliestStart = crossover;
  //       t.earliestFinish = crossover + t.meanDuration -1;
  //       crossover = t.earliestFinish +1;
  //     }
  //   }
  // }

  public void forwardPass() {
    Task t = backlogTasks.get(0);
    estimatedDeadline = forwardPass(t, 0);
  }

  public int forwardPass(Task t, int time) {
    t.earliestStart = time;
    t.earliestFinish = time + t.meanDuration;
    int estimate = 0;
    int latestFinish = 0;
    for (Task t2 : t.successorTasks) {
      estimate = forwardPass(t2, t.earliestFinish);
      if (estimate > latestFinish) latestFinish = estimate;
    }

    return latestFinish;
  }

  public void backwardPass() {
    Task t = backlogTasks.get(backlogTasks.size()-1);
    backwardPass(t, estimatedDeadline);
  }

  public void backwardPass(Task t, int deadline) {
    t.latestStart = deadline - t.meanDuration;
    t.latestFinish = deadline;
    for (Task t2 : t.predecessorTasks) {
      backwardPass(t2, t.latestStart);
    }
  }

  public void calculateFloat() {
    for (Task t : backlogTasks) {
      t.maximumTime = t.latestFinish - t.earliestStart;
      t.taskFloat = t.maximumTime - t.meanDuration;
    }
  }

  public void backwardPass(){
    
  for (Task task : endTasks) {
      double lastStartCheck = criticalLastFinish;
      Task tempTask = task;
      while (!tempTask.predecessorTasks.isEmpty()) {
        tempTask.latestFinish = (int) lastStartCheck;
        tempTask.latestStart = tempTask.latestFinish - tempTask.meanDuration +1;
        lastStartCheck = tempTask.latestFinish - tempTask.meanDuration;
        tempTask = tempTask.predecessorTasks.get(0);
      }
      tempTask.latestFinish = (int) lastStartCheck;
      tempTask.latestStart = tempTask.latestFinish - tempTask.meanDuration +1;
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
    criticalLastFinish = criticalPathTasks.longestPathDuration;
    backlogTasks.remove(criticalPathTasks);
    for (Task t : criticalPath) {
      t.isCritical = true;
      backlogTasks.remove(t);
    }
  }

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

  public void locateEndPathTasks(){
    for (Task task : backlogTasks) {
      endTasks.add(endOfPath(task));
    }
    endTasks.add(endOfPath(criticalPathTasks));
  }

  public Task endOfPath(Task t){
    Task end = t;
    while(!end.successorTasks.isEmpty()) {
      end = end.successorTasks.get(0);
    }
    return end;
  }
}

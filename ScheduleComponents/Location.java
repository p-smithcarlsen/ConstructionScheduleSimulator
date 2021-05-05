package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class Location {
  public String id;
  public String name;
  public Task currentTask;
  public List<Task> tasks = new ArrayList<Task>();   // Turn into node network
  public int duration;

  public Location(String[] locationParameters) {
    this.id = locationParameters[0];
    this.name = locationParameters[1];
    this.duration = 0;
  }

  public void addTask(String[] taskParameters) {
    Task t = new Task(taskParameters);
    tasks.add(t);
  }

  public void forwardPass() {
    int currentTiming = 0;
    for (Task t : tasks) {
      t.calculateEarliestTimings(currentTiming);
      currentTiming = t.meanDuration;
    }
  }

  public void backwardPass(int projectDeadline) {
    if (projectDeadline < duration) {
      // End because not possible
      // Or is it?
    }

    for (Task t : tasks) {
      t.calculateLatestTimings(projectDeadline);
      projectDeadline -= t.meanDuration;
    }
  }

  // public Task getTask() {
  //   if (currentTask == null || currentTask.isFinished()) currentTask = tasks.get(0);
  //   return currentTask;
  // }

  public Task getTask(String id) {
    for (Task t : tasks) {
      if (t.id.equals(id)) return t;
    }

    return null;
  }

  public void calculateDuration() {
    this.duration = 0;
    for (Task t : tasks) {
      // System.out.println(t.id + ": " + t.quantity + " / " + t.productionRate);
      duration += t.meanDuration;
    }
    // Can simply sum durations since tasks are linear and straightforward
  }

  public void calculateDurationWithCertainty(double certainty) {
    // Calculation for finding the estimated duration of a task with a certainty (based on standard deviation)
  }

  public boolean isFinished() {
    return tasks.size() == 0;
  }

  // public void workOn(Task t, int workers) {
  //   t.work(workers);
  // } // Move to workforce

  public void print() {
    System.err.println(String.format("%nLocation %s (%s):", this.id, this.name));
    tasks.stream().forEach(t -> t.print());
  }
}

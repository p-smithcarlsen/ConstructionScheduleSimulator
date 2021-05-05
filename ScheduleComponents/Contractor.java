package ScheduleComponents;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Contractor {
  public String id;
  public String name;
  public String trade;
  public int[] workerDemand;
  public int scheduleLength;
  public List<Task> scheduledTasks;       // More flexibility than stack/queue since we can choose all tasks
  public int workers;
  public int availableWorkers;

  public Contractor(String id, String trade) {
    this.id = id;
    this.trade = trade;
  }

  public void calculateWorkerDemand(List<Task> tasks) {
    Collections.sort(tasks, new SortByEarliestStart());
    for (Task t : tasks)
      if (t.earliestFinish > scheduleLength) scheduleLength = t.earliestFinish;

    this.scheduledTasks = tasks;
    this.workerDemand = new int[scheduleLength];
    for (Task t : tasks) {
      for (int i = t.earliestStart; i < t.earliestFinish; i++)
        workerDemand[i] += t.optimalWorkerCount;
    }
  }

  public void assignWorkers(int today) {
    if (today >= workerDemand.length) return;
    availableWorkers = workerDemand[today];
    int w = 0;

    for (Task t : scheduledTasks) {
      if (t.earliestFinish > today && !t.isFinished() && t.canBeStarted()) {
        w = Math.min(t.optimalWorkerCount, availableWorkers);
        t.assignWorkers(w);
        System.out.println(id + " providing " + w + " workers to task " + t.location + t.id);
        // System.out.println(String.format("Contractor %s providing %s workers of %s available", id, w, availableWorkers));
        availableWorkers -= w;
      }
    }
  }

  public void print() {
    System.err.println(String.format("%n%s:", this.trade));
    System.out.println(String.format(" --Number of workers: %s", this.workers));
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
}

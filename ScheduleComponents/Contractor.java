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
    this.workerDemand = new int[scheduleLength+1];
    for (Task t : tasks) {
      for (int i = t.earliestStart; i < t.earliestFinish+1; i++)
        workerDemand[i] += t.optimalWorkerCount;
    }
  }

  /**
   * 
   * @param today
   */
  public void assignWorkers(int today) {
    if (today >= workerDemand.length) return;
    availableWorkers = workerDemand[today];
    int w = 0;

    for (Task t : scheduledTasks) {
      if (!t.isFinished() && t.canBeStarted()) {
        // t.earliestFinish > today, kan en task ikke godt slutte samme dag? i dettes setup vil den jo faile
        // >= ?
        // !t.isfinished, så hvis ikke færdig -> falsk, derfor negation til true fordi der mangler arbejde?
        // t.canbestarted kun true hvis all predecessor tasks er done?
        w = Math.min(t.optimalWorkerCount, availableWorkers);
        t.assignWorkers(w); // assigner enten det optimale antal workers, eller det mulige antal.
        System.out.println(id + " providing " + w + " workers to task " + t.location + t.id);
        // System.out.println(String.format("Contractor %s providing %s workers of %s available", id, w, availableWorkers));
        availableWorkers -= w; // sørger for, de samme workers ikke kan assignes twice, samme dag. 
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

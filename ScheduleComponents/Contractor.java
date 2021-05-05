package ScheduleComponents;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Contractor {
  public String id;
  public String name;
  public String trade;
  public int[] workerDemand;
  public List<Task> scheduledTasks;
  public int workers;
  public int availableWorkers;

  public Contractor(String id, String trade) {
    this.id = id;
    this.trade = trade;
    // this.workers = Integer.parseInt(tradeParameters[1]);
    // this.availableWorkers = this.workers;
  }

  public void calculateWorkerDemand(List<Task> tasks) {
    Collections.sort(tasks, new SortByEarliestFinish());
    this.scheduledTasks = tasks;
    this.workerDemand = new int[tasks.get(tasks.size()-1).earliestFinish];
    for (Task t : tasks) {
      for (int i = t.earliestStart; i < t.earliestFinish; i++)
        workerDemand[i] += t.optimalCrew;
    }
  }

  public int assignWorkers(double quantity) {
    int w = (int)Math.ceil(Math.min(availableWorkers, quantity));
    System.out.println(String.format("Contractor %s providing %s workers of %s available", id, w, availableWorkers));
    availableWorkers = availableWorkers - w;
    return w;
  }

  public void print() {
    System.err.println(String.format("%n%s:", this.trade));
    System.out.println(String.format(" --Number of workers: %s", this.workers));
  }

  /**
   * Used to sort tasks ascendingly by earliest finish time
   */
  private class SortByEarliestFinish implements Comparator<Task> {
    @Override
    public int compare(Task t1, Task t2) {
      return t1.earliestFinish - t2.earliestFinish;
    }
  }
}

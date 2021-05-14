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
  public List<Task> scheduledTasks;
  public int availableWorkers;

  public Contractor(String id, String trade) {
    this.id = id;
    this.trade = trade;
  }

  /**
   * Iterates over all the tasks assigned to this contractor and calculates
   * when workers are needed. This information will be inserted into an array
   * of ints, where each entry denotes the amount of workers needed on that day
   * @param tasks is the list of tasks assigned to this contractor
   */
  public void calculateWorkerDemand(List<Task> tasks) {
    Collections.sort(tasks, new SortByEarliestStart());
    for (Task t : tasks)
      if (t.earliestFinish > scheduleLength) scheduleLength = t.earliestFinish+1;

    this.scheduledTasks = tasks;
    this.workerDemand = new int[scheduleLength+1];
    for (Task t : tasks) {
      // t.print();
      for (int i = t.earliestStart; i < t.earliestFinish; i++) {
        workerDemand[i] += t.optimalWorkerCount;
      }
    }
  }

  /**
   * Iterates over the contractor's tasks and assigns workers to the
   * active ones. A task is active if it is not finished and if all
   * predecessor tasks are finished.
   * @param today is the day of work
   */
  public void assignWorkers(int today) {
    if (today >= workerDemand.length) return;
    availableWorkers = workerDemand[today];
    int w = 0;

    for (Task t : scheduledTasks) {
      // makes sure a) the task is not finished; b) all predecessor tasks are finished
      if (!t.isFinished() && t.canBeStarted()) {  
        w = Math.min(t.optimalWorkerCount, availableWorkers);
        // assigner enten det optimale antal workers, eller det mulige antal.
        t.assignWorkers(w); 
        System.out.println(trade + " providing " + w + " of " + workerDemand[today] + " workers to task " + t.location + t.id);
        // System.out.println(String.format("Contractor %s providing %s workers of %s available", id, w, availableWorkers));
        availableWorkers -= w; // sørger for, de samme workers ikke kan assignes twice, samme dag. 
      }
    }
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

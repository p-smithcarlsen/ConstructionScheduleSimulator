package ScheduleComponents;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Location {
  public String id;
  public String name;
  private Task currentTask;
  private Queue<Task> tasks = new LinkedBlockingQueue<Task>();

  public Location(String[] locationParameters) {
    this.id = locationParameters[0];
    this.name = locationParameters[1];
  }

  public void addTask(String[] taskParameters) {
    tasks.add(new Task(taskParameters));
  }

  public Task getTask() {
    if (currentTask == null || currentTask.isFinished()) currentTask = tasks.poll();
    return currentTask;
  }

  public boolean isFinished() {
    return tasks.size() == 0;
  }

  public void workOn(Task t, int workers) {
    t.work(workers);
  }

  public void print() {
    System.err.println(String.format("%nLocation %s (%s):", this.id, this.name));
    tasks.stream().forEach(t -> t.print());
  }
}

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

  /**
   * Adds a task to the list of tasks assigned to this location
   * @param t
   */
  public void addTask(Task t) {
    tasks.add(t);
  }

  public Task getTask(String id) {
    for (Task t : tasks) {
      if (t.id.equals(id)) return t;
    }

    return null;
  }

  public void print() {
    System.err.println(String.format("%nLocation %s (%s):", this.id, this.name));
    tasks.stream().forEach(t -> t.print(1));
  }
}

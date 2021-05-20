package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class Location {
  public int id;
  public String name;
  public Task currentTask;
  public List<Task> tasks = new ArrayList<Task>();   // Turn into node network
  public int duration;

  public Location(String[] locationParameters) {
    this.id = Integer.parseInt(""+locationParameters[0].charAt(1));
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

  public Task getTask(int id) {
    for (Task t : tasks) {
      if (t.id == id) return t;
    }

    return null;
  }

  public void print() {
    System.err.println(String.format("%nLocation %s (%s):", this.id, this.name));
    tasks.stream().forEach(t -> t.print(1));
  }
}

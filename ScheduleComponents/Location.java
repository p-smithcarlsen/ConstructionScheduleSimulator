package ScheduleComponents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Location {
  String id;
  String name;
  List<Task> tasks = new ArrayList<>();

  public Location(String[] locationParameters) {
    this.id = locationParameters[0];
    this.name = locationParameters[1];
  }

  public void addTask(String[] taskParameters) {
    tasks.add(new Task(taskParameters));
  }

  public void print() {
    System.err.println(String.format("%nLocation %s (%s):", this.id, this.name));
    tasks.stream().forEach(t -> t.print());
  }

  private class Task {
    String id;
    String activity;
    String trade;
    double quantity;
    String[] dependencies;

    public Task(String[] taskParameters) {
      this.id = taskParameters[0];
      this.activity = taskParameters[2];
      this.trade = taskParameters[3];
      taskParameters[4] = taskParameters[4].replaceAll(",", ".");
      this.quantity = Double.parseDouble(taskParameters[4]);
      this.dependencies = taskParameters[5].split(",");
    }

    public void print() {
      System.out.println(String.format(" --Task %s: %s, trade: %s, quantity: %s, dependency: %s", id, activity, trade, quantity, Arrays.deepToString(dependencies)));
    }
  }
}

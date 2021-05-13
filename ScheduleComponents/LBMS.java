package ScheduleComponents;

public class LBMS {

  public TaskGraph tasks;
  public Location[] locations;

  public LBMS(TaskGraph tasks, Location[] locations) {
    this.tasks = tasks;
    this.locations = locations;
  }

  public void prepareLocations() {
    createDependencies();   // between tasks
    tasks.calculateCriticalPath();

    for (Location l : locations) {
      // Can we do the two next ones without having established all dependencies?
      // I.e. do we need to do another loop after this loop?
      l.calculateDuration();    // of location
      // l.forwardPass();          // i.e. durations of tasks
    }
  }

  private void createDependencies() {
    for (Task t : tasks.getBacklogTasks()) {
      String[] dependencies = t.getDependencies().split(",");
      if (dependencies[0].equals("*")) continue;

      for (String d : dependencies) {
        String[] dependency = d.split("=");
        Task predecessor = getLocation(dependency[0]).getTask(dependency[1]);
        tasks.addTaskDependency(predecessor, t);
      }
    }
  }

  public Location getLocation(String id) {
    for (Location l : locations) {
      if (l.id.equals(id)) return l;
    }

    return null;
  }

  public void work() {
    for (Location l : locations) {
      for (Task t : l.tasks) {
        t.work();
      }
    }
  }
}

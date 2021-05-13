package ScheduleComponents;

public class LBMS {

  public TaskGraph tasks;
  public Location[] locations;

  public LBMS(TaskGraph tasks, Location[] locations) {
    this.tasks = tasks;
    this.locations = locations;
  }

  public void prepareLocations() {
    createDependencies();
    tasks.forwardPass();
    for (Location l : locations) {
      // between tasks
      // Can we do the two next ones without having established all dependencies?
      // I.e. do we need to do another loop after this loop?
      l.calculateDuration();    // of location
      // l.forwardPass();          // i.e. durations of tasks
    }
    tasks.calculateCriticalPath();
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

  public void totalProgress(){
    int numTotalTasks = 0;
    int numTotalCompletedTasks = 0;
    for (Location l : locations) {
      int numTasksLocation = 0;
      int numCompletedTasksAtLocation = 0;
      for (Task t : l.tasks) {
        numTasksLocation++;
        if (t.isFinished()) {
          numCompletedTasksAtLocation++;
        }
        
      }
      System.out.println("Location: " + "" + l.id + " Completed Tasks: " + numTasksLocation + "/" + numCompletedTasksAtLocation);
      numTotalTasks += numTasksLocation;
      numTotalCompletedTasks += numCompletedTasksAtLocation;
    }
    System.out.println("Total Completed Tasks: " + numTotalTasks + "/" + numTotalCompletedTasks);
  }

  public void printTasks(){
    for (Location l : locations) {
      System.out.println(l.id);
      for (Task t : l.tasks) {
        System.out.println(t.id + " " + t.earliestStart + " " + t.earliestFinish);
        System.out.println("predecessors " + t.predecessorTasks.size());
        for (Task tt : t.predecessorTasks) {
          System.out.println(tt.id);
        }
      }
    }
  }
}

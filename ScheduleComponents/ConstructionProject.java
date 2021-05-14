package ScheduleComponents;

public class ConstructionProject {

  public TaskGraph tasks;
  public Location[] locations;
  public DelayManager delays;

  public ConstructionProject(TaskGraph tasks, Location[] locations) {
    this.tasks = tasks;
    this.locations = locations;
    this.delays = new DelayManager();
  }

  /**
   * Prepares locations for the subsequent work. Dependencies are created between tasks,
   * start and finish times are determined for all tasks, floats are calculated and 
   * the critical paths are found. 
   */
  public void prepareLocations() {
    createDependencies();
    tasks.forwardPass();
    tasks.backwardPass();
    tasks.calculateFloat();
    tasks.findCriticalPaths();
  }

  /**
   * Iterates through all tasks and inserts tasks into predecessor
   * and successor lists.
   */
  private void createDependencies() {
    for (Task t : tasks.allTasks) {
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

  /**
   * Iterates through all tasks and performs the work that the
   * workers have been assigned for. Thus, the progress of the 
   * tasks will be incremented in this method. 
   */
  public void work() {
    for (Task t : tasks.criticalTasks) {
      t.work();
    }

    for (Task t : tasks.backlogTasks) {
      t.work();
    }
  }

  /**
   * Iterates over all locations and prints the status on the
   * tasks assigned to those locations. 
   */
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

  /**
   * Iterates over all tasks and prints the location and id of each task
   */
  public void printTasks(){
    Task tempTask;
    for (Task task : tasks.backlogTasks) {
      System.out.println();
      tempTask = task;
      while(!tempTask.successorTasks.isEmpty()) {
      System.out.print(tempTask.location + " " + tempTask.id + " ");
      tempTask = tempTask.successorTasks.get(0);
      }
      System.out.print(tempTask.location + " " + tempTask.id + " ");
    }
    Task tempCrit = tasks.criticalPathTasks;
    System.out.println();
    while (!tempCrit.successorTasks.isEmpty()) {
      System.out.print(tempCrit.location + " " + tempCrit.id + " ");
      tempCrit = tempCrit.successorTasks.get(0);
    }
    System.out.print(tempCrit.location + " " + tempCrit.id + " ");
  }
}

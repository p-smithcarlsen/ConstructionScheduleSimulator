package ScheduleComponents;

import java.util.List;

public class ConstructionProject {

  public TaskGraph tasks;
  public Location[] locations;
  public AlarmManager alarms;

  public ConstructionProject(TaskGraph tasks, Location[] locations) {
    this.tasks = tasks;
    this.locations = locations;
    this.alarms = new AlarmManager();
  }

  /**
   * Prepares locations for the subsequent work. Dependencies are created between tasks,
   * start and finish times are determined for all tasks, floats are calculated and 
   * the critical paths are found. 
   */
  public void prepareLocations() {
    createDependencies();
    tasks.forwardPass(0);
    tasks.backwardPass();
    tasks.calculateFloat();
  }

  /**
   * Iterates through all tasks and inserts tasks into predecessor
   * and successor lists.
   */
  private void createDependencies() {
    for (Task t : tasks.tasks) {
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
    for (Task t : tasks.tasks) {
      t.work();
    }
  }

  public void analyseTaskDelays(List<Alarm> alarms, Workforce w, int tomorrow) {
    if (alarms.size() == 0) return;
    for (Alarm a : alarms) {
      if (!a.task.isCritical) continue;

      int[] contractorSchedule = w.getContractorSchedule(a.trade);
      a.task.recalculateDuration(contractorSchedule, tomorrow);
    }

    for (Alarm a : alarms) {
      if (a.task.isCritical) continue;
      
      int[] contractorSchedule = w.getContractorSchedule(a.trade);
      a.task.recalculateDuration(contractorSchedule, tomorrow);
    }

    tasks.forwardPass(tomorrow);
    tasks.backwardPass();
    tasks.calculateFloat();
    w.updateContractorSchedules();
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
    for (Task task : tasks.tasks) {
      System.out.println();
      tempTask = task;
      while(!tempTask.successorTasks.isEmpty()) {
      System.out.print(tempTask.location + " " + tempTask.id + " ");
      tempTask = tempTask.successorTasks.get(0);
      }
      System.out.print(tempTask.location + " " + tempTask.id + " ");
    }
  }
}
package ScheduleComponents;

import java.util.List;
import java.util.Map;

import ScheduleComponents.Contractor.Trade;

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
    tasks.calculateFloat(0);
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
        int location = Integer.parseInt(""+dependency[0].charAt(1));
        int task = Integer.parseInt(""+dependency[1].charAt(1));
        Task predecessor = getLocation(location).getTask(task);
        tasks.addTaskDependency(predecessor, t);
      }
    }
  }

  public Location getLocation(int id) {
    for (Location l : locations) {
      if (l.id == id) return l;
    }

    return null;
  }

  /**
   * Iterates through all tasks and performs the work that the
   * workers have been assigned for. Thus, the progress of the 
   * tasks will be incremented in this method. 
   */
  public void work(int day) {
    for (Task t : tasks.tasks) {
      t.work(day);
    }
  }

  public void analyseAlarms(List<Alarm> alarms, Workforce w, int tomorrow) {
    // We know that there has been an undersupply of workers to a task, which
    // means that a task has progressed more slowly than expected. This means
    // that theere will be a higher need of workers at some point, maybe also a delay.
    // Since we try to finish critical tasks first, it often do not lead to a pushback
    // of the project deadline - instead just an increased early finish of a backlog task.

    // First, go through all tasks that are started but not finished (0 < progress < 100)
    // We are 'resetting' the project from day *tomorrow*, so we need to recalculate 
    // durations. (i.e. if a task is half done, and its original duration is 4, then its
    // new duration will be shorter because some work has already be performed).
    // New durations will probably mean new timinigs of some tasks (probably the ones that
    // are currently being worked and tasks that depend on these). So we need to reset timings
    // Related to real life: A contractor tells the project manager that there is a delay,
    // now the project manager needs to figure out how this affects the schedule...
    
    // The project manager has now assessed the situation, and has probably assessed
    // that the schedule (although some tasks having a longer earliest finish) is OK.
    // However, we still need to address whether we have enough workers to complete
    // all the tasks. If contractors have initially only hired the amount of workers
    // that is able to finish the assigned tasks, then we will need an additional
    // number of workers (equal to the amount calling in sick). They need to come in
    // at the latest when the last similar task (same trade type) has its latest 
    // finish date. Otherwise, the task will go over its latest finish, push back
    // depending tasks and extend project deadline.
    for (Alarm a : alarms) {
      // We will compare the worker demand with the worker supply and determine 
      // when (if so) a task's latest finish will be pushed back
      Contractor delayedContractor = w.getContractor(a.trade);
      delayedContractor.checkWorkerSupply(tomorrow);
      // tasks.printTaskSchedules(tomorrow-1);
      Map<Trade, int[]> updatedSchedules = tasks.determineScheduledTimings(w.getContractorSchedule(), tomorrow, w);
      for (Trade t : updatedSchedules.keySet()) {
        w.getContractor(t).workerDemand = updatedSchedules.get(t);
      }
      a.resolve();
    }

    tasks.calculateTimingsAndFloats(tomorrow);
  }

  public void alignTaskScheduledFinishes(int day) {
    for (Task t : tasks.tasks) {
      int latestWorkerSupply = 0;
      for (int i = 0; i < t.scheduledWorkers.length; i++) {
        if (t.scheduledWorkers[i] > 0 && i > latestWorkerSupply) {
          t.scheduledFinish = i+1;
        }
      }
    }
  }

  public void endOfDay() {
    for (Task t : tasks.tasks) {
      t.workersAssigned = 0;
    }
  }

  /**
   * Prints the estimated project deadline and the number of remaining tasks
   */
  public void printStatus() {
    if (tasks.numberOfRemainingTasks() > 0) {
      System.out.println("Project deadline according to schedules: day " + tasks.scheduledDeadline + " (remaining tasks: " + tasks.numberOfRemainingTasks() + ")");
    } else {
      System.out.println("All tasks finished!");
    }
  }

  public void printTaskAssignment() {
    for (Task t : tasks.tasks) {
      if (!t.isFinished() && t.canBeStarted()) {
        double w = t.transformWorkersToProgress(t.workersAssigned);
        System.out.printf("L%dT%d: Scheduled % 2d - % 2d  (progress=%5.1f, %5.1f)%n", t.location, t.id, t.scheduledStart, t.scheduledFinish, t.progress, t.progress+w);
      }
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

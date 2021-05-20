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
  public void work() {
    for (Task t : tasks.tasks) {
      t.work();
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
    tasks.calculateTimingsAndFloats(tomorrow);    // Take account for the small delay the sick worker may have caused
    // tasks.printTasksWithDependencies(locations.length, locations[0].tasks.size());
    
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
      // We know which contractor is lacking behind, so we will forecast the worker
      // demand for this contractor:
      int[] workerDemand = tasks.forecastWorkerDemand(a.trade);
      // We will compare the worker demand with the worker supply and determine 
      // when (if so) a task's latest finish will be pushed back
      Contractor delayedContractor = w.getContractor(a.trade);
      delayedContractor.checkWorkerSupply(a.task, workerDemand, tomorrow);
      tasks.determineScheduledTimings(w.contractorSchedules, tomorrow);
      // delayedContractor.scheduleTasks(tomorrow);
      // tasks.determineScheduledTimings(a.task, w.contractorSchedules);
      a.resolve();

      // if (otherContractorsNeedToReschedule) {
      //   boolean scheduleChanged = true;
      //   while (scheduleChanged) {
      //     for (Contractor c : w.contractors) {
      //       if (!c.trade.equals(a.trade)) {
      //         scheduleChanged = c.alignWorkerSchedule(tomorrow);
      //       }
      //     }
      //   }
  
      //   if (scheduleChanged) {
      //     for (Contractor c : w.contractors) {
      //       c.reschedule();
      //     }
      //   }
      //   tasks.determineScheduledTimings(w.contractorSchedules, tomorrow);
      // }
    }

    // If there has been a change in a contractor's schedule, we need to go over the schedules, 
    // finding out whether contractors are actually supplying workers on the correct days...
    // if (otherContractorsNeedToReschedule) {
    //   boolean scheduleChanged = true;
    //   while (scheduleChanged) {
    //     for (Contractor c : w.contractors) {
    //       scheduleChanged = c.alignWorkerSchedule(tomorrow);
    //     }
    //   }

    //   if (scheduleChanged) {
    //     for (Contractor c : w.contractors) {
    //       c.reschedule();
    //     }
    //   }
    //   tasks.determineScheduledTimings(w.contractorSchedules);
      // tasks.forwardPassWithScheduledTimings(tomorrow);
      // for (Contractor c : w.contractors) {
      //   suggestScheduleChange(tasks.forecastWorkerDemand(c.trade), c.workerDemand);
      // }
      // tasks.determineScheduledTimings(w.contractorSchedules);
    // }
  }

  public boolean suggestScheduleChange(int[] neededWorkers, int[] scheduledWorkers) {
    int i = 0; 
    System.out.println(neededWorkers.length + " : " + scheduledWorkers.length);
    while (i < neededWorkers.length || i < scheduledWorkers.length) {
      int needed = 0;
      int scheduled = 0;
      if (i < neededWorkers.length) {
        needed = neededWorkers[i];
      } else { needed = -1; }
      if (i < scheduledWorkers.length) {
        scheduled = scheduledWorkers[i];
      } else { scheduled = -1; }
      System.out.printf("%03d : %03%n", needed, scheduled);
      i++;
    }
    return false;
  }

  public void printStatus() {
    if (tasks.numberOfRemainingTasks() > 0) {
      System.out.println("Estimated deadline of project: day " + tasks.estimatedDeadline + " (remaining tasks: " + tasks.numberOfRemainingTasks() + ")");
    } else {
      System.out.println("All tasks finished!");
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

package Simulation;

import ScheduleComponents.LBMS;
import ScheduleComponents.Workforce;

public class Simulator {

  public Workforce workforce;

  public int day = 0; // bør dette ikke være 0? flere earliest starts er på 0

  public void runSimulation(LBMS lbms) {
    // Calculate durations for locations and tasks
    // Find critical path
    lbms.prepareLocations();
    //lbms.tasks.printCriticalPath();

    // Hire contractors and delegate tasks to individual contractors
    workforce = new Workforce(lbms.locations);

    lbms.printTasks();

    // Go through project day by day until all tasks are finished
    while (true) {
      System.err.println("day: " + day);
      // Assign workers
      workforce.assignWorkers(day);

      // Work tasks
      lbms.work();

      // End of day (workers are reset)
      workforce.endOfTheDay();

      // Check if there are any alarms

      lbms.totalProgress();

      // Take actions


      day++;
      if (day > 100) break;
    }
  }
}

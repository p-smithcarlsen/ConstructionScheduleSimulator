package Simulation;

import ScheduleComponents.LBMS;
import ScheduleComponents.Workforce;

public class Simulator {

  public int day = 1;

  public void runSimulation(LBMS lbms) {
    // Calculate durations for locations and tasks
    lbms.prepareLocations();

    // Hire contractors and delegate tasks to individual contractors
    Workforce workforce = new Workforce(lbms.locations);

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

      // Take actions


      day++;
      if (day > 1000) break;
    }
  }
}
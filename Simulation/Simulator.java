package Simulation;

import ScheduleComponents.LBMS;
import ScheduleComponents.Workforce;

public class Simulator {

  public Workforce workforce;

  public int day = 0; // bør dette ikke være 0? flere earliest starts er på 0

  /**
   * Runs the simulation, based on an LBMS object, which contains all 
   * the necessary information of the tasks in the construction project.
   * The project is simulated day for day, where each contractor assigns
   * workers to tasks and subsequently performs the work. If any alarms
   * materialize, the project manager will get feedback and possibly an 
   * option to perform an action.
   * @param lbms
   */
  public void runSimulation(LBMS lbms) {
    // Finds the critical path(s) in the tasks
    lbms.prepareLocations();

    // Hire contractors and delegate tasks to individual contractors
    workforce = new Workforce(lbms.locations);

    // Go through project day by day until all tasks are finished
    while (true) {
      System.err.println("day: " + day);
      
      // Assign workers
      workforce.assignWorkers(day);

      // Work tasks
      lbms.work();

      // Check if there are any alarms

      // Take actions

      // Got to next day
      day++;
      if (day > 100) break;
    }
  }
}

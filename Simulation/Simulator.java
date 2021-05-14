package Simulation;

import ScheduleComponents.DelayManager;
import ScheduleComponents.ConstructionProject;
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
  public void runSimulation(ConstructionProject constructionProject) {
    // Finds the critical path(s) in the tasks
    constructionProject.prepareLocations();

    // Hire contractors and delegate tasks to individual contractors
    workforce = new Workforce(constructionProject.locations, constructionProject.delays);

    // Go through project day by day until all tasks are finished
    while (true) {
      System.err.println("day: " + day);
      
      // Assign workers
      workforce.assignWorkers(day);

      // Work tasks
      constructionProject.work();

      // Check for delays (where can delays come from?)
      // - workers being sick                   comes from contractor object
      // - weather being disadvantageous        comes from lbms object
      // - tasks taking longer than expected    comes from task object
      // - material delivery delayed            comes from lbms object
      

      // Check worker supply vs worker demand
      if (constructionProject.delays.unresolvedDelay()) workforce.forecastContractorSchedules(day);

      // Take actions

      // Got to next day
      System.out.println("Estimated deadline of project: day " + constructionProject.tasks.estimatedDeadline);
      day++;
      if (day > 100) break;
    }
  }
}

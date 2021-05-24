package Simulation;

import ScheduleComponents.ConstructionProject;
import ScheduleComponents.Workforce;

public class Simulator {

  public Workforce workforce;

  public int day = 0; // bør dette ikke være 0? flere earliest starts er på 0

  /**
   * Runs the simulation, based on a ConstructionProject object, which contains all 
   * the necessary information of the tasks in the construction project.
   * The project is simulated day for day, where each contractor assigns
   * workers to tasks and subsequently performs the work. If any alarms
   * materialize, the project manager will get feedback and possibly an 
   * option to perform an action.
   * @param constructionProject
   */
  public void runSimulation(ConstructionProject constructionProject) {
    // Find the critical path(s) in the tasks
    constructionProject.prepareLocations();
    // constructionProject.tasks.printTasksWithDependencies(constructionProject.locations.length, constructionProject.locations[0].tasks.size());
    // Hire contractors and delegate tasks to individual contractors
    workforce = new Workforce(constructionProject.locations, constructionProject.alarms);
    // Go through all worker schedules and set their scheduled timings
    constructionProject.tasks.determineScheduledTimings(workforce.contractorSchedules, 0, workforce);
    // Go through project day by day until all tasks are finished
    while (true) {
      System.err.printf("\n\n==========| day: % 3d |==========\n", day);
      constructionProject.printStatus();
      if (day >= constructionProject.tasks.scheduledDeadline && constructionProject.tasks.numberOfRemainingTasks() > 0) {
        System.out.println("");
      }
      if (constructionProject.tasks.numberOfRemainingTasks() <= 0) {
        break;
      }
      
      // Assign workers
      workforce.assignWorkers(day);
      
      // constructionProject.printTaskAssignment();

      // Work tasks
      constructionProject.work();
      constructionProject.tasks.scheduledDeadline();

      // Check for delays (where can delays come from?)
      // - workers being sick                   comes from contractor object
      // - weather being disadvantageous        comes from lbms object
      // - tasks taking longer than expected    comes from task object
      // - material delivery delayed            comes from lbms object
      
      // If there are any alarms, resolve them
      if (constructionProject.alarms.getUnresolvedAlarms().size() > 0) {
        constructionProject.analyseAlarms(constructionProject.alarms.getUnresolvedAlarms(), workforce, day+1);
      }

      // workforce.printContractorSchedules();

      // Go to next day
      endOfDay(workforce, constructionProject);
      day++;
      if (day > 100) break;
    }
    // constructionProject.tasks.printTasksWithDependencies(constructionProject.locations.length, constructionProject.locations[0].tasks.size());
  }

  public void endOfDay(Workforce w, ConstructionProject cp) {
    w.endOfDay();
    cp.endOfDay();
  }
}

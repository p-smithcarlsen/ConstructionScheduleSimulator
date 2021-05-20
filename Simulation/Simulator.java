package Simulation;

import ScheduleComponents.ConstructionProject;
import ScheduleComponents.Alarm;
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
    // Print overview of tasks (and dependencies)
    constructionProject.tasks.printTasksWithDependencies(constructionProject.locations.length, constructionProject.locations[0].tasks.size());

    // Hire contractors and delegate tasks to individual contractors
    workforce = new Workforce(constructionProject.locations, constructionProject.alarms);

    constructionProject.tasks.determineScheduledTimings(workforce.contractorSchedules, 0);

    // Go through project day by day until all tasks are finished
    while (true) {
      System.err.printf("\n\n==========| day: % 3d |==========\n", day);
      constructionProject.printStatus();
      
      // Assign workers
      workforce.assignWorkers(day);

      // Work tasks
      constructionProject.work();

      // Check for delays (where can delays come from?)
      // - workers being sick                   comes from contractor object
      // - weather being disadvantageous        comes from lbms object
      // - tasks taking longer than expected    comes from task object
      // - material delivery delayed            comes from lbms object
      
      // If any alarms, take actions
      if (constructionProject.alarms.getUnresolvedAlarms().size() > 0) {
        constructionProject.analyseAlarms(constructionProject.alarms.getUnresolvedAlarms(), workforce, day+1);
      }

      // Go to next day
      System.out.println();
      day++;
      if (day > 100) break;
    }

    for (Alarm d : constructionProject.alarms.alarms) {
      System.out.println(d);
    }
  }
}

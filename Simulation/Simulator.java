package Simulation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ScheduleComponents.Alarm;
import ScheduleComponents.ConstructionProject;
import ScheduleComponents.Workforce;
import ScheduleComponents.Contractor.Trade;

public class Simulator {

  public Workforce workforce;

  public int day = 0;

  /**
   * Runs the simulation, based on a ConstructionProject object, which contains all 
   * the necessary information of the tasks in the construction project.
   * The project is simulated day for day, where each contractor assigns
   * workers to tasks and subsequently performs the work. If any alarms
   * materialize, the project manager will get feedback and possibly an 
   * option to perform an action.
   * @param constructionProject
   * @throws IOException
   */
  public void runSimulation(ConstructionProject constructionProject, Logger l, Analyzer a, boolean addWorkers) throws IOException {
    System.out.println("\n\n                            _______________                           ");
    System.out.println("===========================/ PROJECT START \\===========================");
    // Find the critical path(s) in the tasks
    constructionProject.prepareLocations();
    // Hire contractors and delegate tasks to individual contractors
    workforce = new Workforce(constructionProject.locations, constructionProject.alarms);
    l.log(workforce);
    // Go through all worker schedules and set their scheduled timings
    constructionProject.tasks.determineScheduledTimings(workforce.contractorSchedules, 0, workforce);
    l.logScheduledDeadline(constructionProject.tasks.scheduledDeadline);

    // Check analyzer to see schedule warnings
    a.setScheduledDeadline(constructionProject.tasks.scheduledDeadline);
    Map<Trade, Integer> extraWorkerSupply = a.seeWarnings(workforce.contractors);
    if (extraWorkerSupply != null && addWorkers) {
      workforce.addExtraWorkers(extraWorkerSupply);
      l.log(extraWorkerSupply);
    }

    while (true) {
      // Go through project day by day until all tasks are finished
      // System.err.printf("\n\n==========| day: % 3d |==========\n", day);
      if (constructionProject.tasks.numberOfRemainingTasks() <= 0) {
        break;
      }
      
      // Assign workers
      workforce.assignWorkers(day);

      // Work tasks
      constructionProject.work(day);
      constructionProject.alignTaskScheduledFinishes(day);
      constructionProject.tasks.scheduledDeadline();
      
      // If there are any alarms, resolve them
      if (constructionProject.alarms.getUnresolvedAlarms().size() > 0) {
        List<Alarm> unresolved = constructionProject.alarms.getUnresolvedAlarms();
        l.log(unresolved);
        constructionProject.analyseAlarms(unresolved, workforce, day+1);
      }

      // Go to next day
      workforce.alignSchedules(day);
      endOfDay(workforce, constructionProject);
      day++;
      if (day > 100) break;
    }
    l.logProjectEnd(day);
    l.end();
    System.out.println("\n\n===========================\\  PROJECT END  /===========================");
    System.out.println("                            \\-------------/                           \n\n");
  }

  public void endOfDay(Workforce w, ConstructionProject cp) {
    w.endOfDay();
    cp.endOfDay();
  }
}

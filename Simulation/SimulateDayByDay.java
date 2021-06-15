package Simulation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ScheduleComponents.Alarm;
import ScheduleComponents.ConstructionProject;
import ScheduleComponents.Workforce;
import ScheduleComponents.Contractor.Trade; 

public class SimulateDayByDay {

  public ConstructionProject c;
  public Workforce workforce;
  public int day = 0;

  public SimulateDayByDay(ConstructionProject constructionProject) {
    this.c = constructionProject;
  }

  public void createInitialSchedule(Logger l, Analyzer a, boolean addWorkers, boolean printToConsole, boolean manualInput) throws IOException {
    if (printToConsole) {
      System.out.println("\n\n                            _______________                           ");
      System.out.println("===========================/ PROJECT START \\===========================");
      a.printDelayDistribution();
    }
    // Find the critical path(s) in the tasks
    // constructionProject.prepareLocations();
    // Hire contractors and delegate tasks to individual contractors
    workforce = new Workforce(c.locations, c.alarms);
    l.log(workforce);
    // Go through all worker schedules and set their scheduled timings
    c.tasks.determineScheduledTimings(workforce.contractorSchedules, 0, workforce);
    l.logScheduledDeadline(c.tasks.scheduledDeadline);

    // Check analyzer to see schedule warnings
    a.setScheduledDeadline(c.tasks.scheduledDeadline);
    Map<Trade, Integer> extraWorkerSupply = a.seeWarnings(workforce.contractors, printToConsole);
    if (extraWorkerSupply != null && addWorkers) {
      // Ask to add extra workers or not
      if (manualInput) {
        boolean addExtraWorkers = askUserToAddMoreWorkers();
        if (addExtraWorkers) {
          workforce.addExtraWorkers(extraWorkerSupply);
          l.log(extraWorkerSupply);
        }
      } else {
        workforce.addExtraWorkers(extraWorkerSupply);
        l.log(extraWorkerSupply);
      }
    }
  }

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
  public void simulateDay(Logger l, Analyzer a, boolean addWorkers, boolean printToConsole, boolean manualInput) throws IOException {
    // Go through project day by day until all tasks are finished
    if (printToConsole) {
      System.err.printf("\n\n==========| day: % 3d |==========\n", day);
      c.printStatus();
    }
    if (c.tasks.numberOfRemainingTasks() <= 0) {
      return;
    }
    
    // Assign workers
    workforce.assignWorkers(day, manualInput);

    // Work tasks
    c.work(day, printToConsole);
    c.alignTaskScheduledFinishes(day);
    c.tasks.scheduledDeadline();
    
    // If there are any alarms, resolve them
    if (c.alarms.getUnresolvedAlarms().size() > 0) {
      List<Alarm> unresolved = c.alarms.getUnresolvedAlarms();
      l.log(unresolved);
      c.analyseAlarms(unresolved, workforce, day+1, printToConsole, manualInput);
    }

    // Go to next day
    workforce.alignSchedules(day);
    endOfDay(workforce, c);
  }

  public void endProject(Logger l, boolean printToConsole) throws IOException {
    l.logProjectEnd(day);
    l.end();
    
    if (printToConsole) {
      System.out.println("\n\n===========================\\  PROJECT END  /===========================");
      System.out.println("                            \\-------------/                           \n\n");
    }
  }

  public boolean isFinished() {
    return c.tasks.numberOfRemainingTasks() <= 0;
  }

  public boolean askUserToAddMoreWorkers() {
    Scanner sc = new Scanner(System.in);
    System.out.println("\n\nDo you want to add extra workers to the schedule? (Y/N)");

    String resp = "";
    while (!resp.equals("y") && !resp.equals("n")) {
      try {
        resp = sc.nextLine().toLowerCase();
        if (!resp.equals("y") && !resp.equals("n")) {
          System.out.println("\nI didn't understand that... please try again!");
        }
        
      } catch (Exception e) {
        System.out.println(e.getMessage());
      } 
    }
    // boolean addWorkers = false;
    if (resp.equals("y")) return true;
    return false;
  }

  public void endOfDay(Workforce w, ConstructionProject cp) {
    w.endOfDay();
    cp.endOfDay();
  }

  public void prepareTimings(ConstructionProject constructionProject) {
    constructionProject.prepareLocations();
  }
}

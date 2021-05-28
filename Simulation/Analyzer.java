package Simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ScheduleComponents.Contractor;
import ScheduleComponents.Contractor.Trade;

public class Analyzer {

  List<SimulationLog> data = new ArrayList<>();
  Map<Integer, List<SimulationLog>> projectsByExtraWorkers = new HashMap<>();
  Map<Integer, Integer> delaysByExtraWorkers = new HashMap<>();
  int estimatedDeadline;
  int projects;
  int[] projectDelays = new int[10];
  
  public Analyzer() throws IOException {
    File path = new File("Data/Database");

    File[] files = path.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        data.add(new SimulationLog(files[i]));
      }
    }
  }

  public void analyzeData() {
    for (SimulationLog l : data) {
      int projectDelay = Math.max(l.projectEnd - l.scheduledDeadline, 0);
      while (projectDelay >= projectDelays.length) projectDelays = resize(projectDelays, projectDelays.length+2);
      projectDelays[projectDelay]++;
      projects++;

      if (projectsByExtraWorkers.containsKey(l.addedWorkers)) {
        projectsByExtraWorkers.get(l.addedWorkers).add(l);
      } else {
        projectsByExtraWorkers.put(l.addedWorkers, new ArrayList<>());
        projectsByExtraWorkers.get(l.addedWorkers).add(l);
      }

      if (projectDelay == 0) continue;
      if (delaysByExtraWorkers.containsKey(l.addedWorkers)) {
        delaysByExtraWorkers.put(l.addedWorkers, delaysByExtraWorkers.get(l.addedWorkers)+1);
      } else {
        delaysByExtraWorkers.put(l.addedWorkers, 1);
      }
    }
  }

  public void printDelayDistribution() {
    if (projects < 50) {
      System.out.printf("%n%nWe do not have enough data to accurately create a delay distribution...%n%n");
      return;
    }
    System.out.printf("%n%n%n ## This is a graph based on all previous projects.%nIt displays the " +
      "delays the projects encountered and the accumulated percent of having maximum this amount of " + 
      "delay for a new construction project%n%n");
    double percent = 0;
    for (int i = 0; i < projectDelays.length; i++) {
      double percentForDelay = (double)projectDelays[i]/(double)projects*100;
      percent += percentForDelay;
      if (percentForDelay > 0.0) {
        System.out.printf("%10s: ", i);
        System.out.println(String.format("*".repeat((int)Math.min(Math.ceil(percent), 100.0))));
      }
    }
    System.out.printf("%2s", "");
    for (int i = 0; i <= 100; i += 10) {
      System.out.printf("%9s|", i);
    }
  }

  public void setScheduledDeadline(int deadline) {
    this.estimatedDeadline = deadline;
  }

  public Map<Trade, Integer> seeWarnings(Contractor[] contractors, boolean printToConsole) {
    if (printToConsole) System.out.printf("%n%nYou are beginning a construction project with deadline in %d days.%n", estimatedDeadline);
    Map<Trade, Double> workersNeeded = new HashMap<>();
    Map<Trade, Integer> projectsPerContractor = new HashMap<>();
    for (Contractor c : contractors) {
      workersNeeded.put(c.trade, 0.0);
      projectsPerContractor.put(c.trade, 0);
    }

    List<SimulationLog> similarProjects = new ArrayList<>();
    for (SimulationLog l : data) {
      boolean[] sameContractor = new boolean[contractors.length];
      for (Trade t : l.workerCounts.keySet()) {
        for (int i = 0; i < contractors.length; i++) {
          if (contractors[i].trade == t) {
            sameContractor[i] = true;
          }
        }
      }
      int similarProject = contractors.length;
      for (boolean b : sameContractor) {
        if (b) similarProject--;
      }
      if (similarProject < 2) similarProjects.add(l);
    }

    int projectsWithNoAddedWorkers= 0;
    int projectsWithNoDelays = 0;
    for (SimulationLog l : similarProjects) {
      if (l.addedWorkers == 0) {
        for (Trade t : l.workerCounts.keySet()) {
          for (Trade t2 : projectsPerContractor.keySet()) {
            if (t == t2) {
              projectsPerContractor.put(t2, projectsPerContractor.get(t2)+1);
            }
          }
        }

        projectsWithNoAddedWorkers++;
        if (l.projectEnd == l.scheduledDeadline) {
          projectsWithNoDelays++;
        }
      }
    }

    if (printToConsole) {
      // Not enough similar projects: return
      double chanceOfSuccess = (double)projectsWithNoDelays / (double)projectsWithNoAddedWorkers * 100.0;
      if (Double.isNaN(chanceOfSuccess)) {
        System.out.printf("%nWe do not have enough similar projects to predict worker shortages...%n");
        return null;
      }
      
      // Sufficient similar projects: calculate chance of finishing on time and
      // how much adding extra workers have helped in the past
      System.out.printf("%nBased on %d similar previous projects (similar combination" + 
        " of contractors), there is a %4.1f%% chance to finish on time.%n", similarProjects.size(), chanceOfSuccess);
    }

    boolean workersSick = false;
    for (SimulationLog l : similarProjects) {
      if (l.addedWorkers > 0) continue;
      for (Trade t : l.delays.keySet()) {
        if (!workersNeeded.containsKey(t)) continue;
        if (workersNeeded.containsKey(t)) {
          workersNeeded.put(t, workersNeeded.get(t) + ((l.delays.get(t).size() / (double)projectsPerContractor.get(t))));
          if (workersNeeded.get(t) > 0.1) workersSick = true;
        } else {
          workersNeeded.put(t, (double)l.delays.get(t).size() / (double)projectsPerContractor.get(t));
        }
      }
    }
    if (workersSick && printToConsole) {
      System.out.printf("According to the previous projects, the some contractors will encounter sick workers:%n");
      for (Trade t : workersNeeded.keySet()) {
        if (workersNeeded.get(t) > 0.1) {
          System.out.printf("%30s: %3d%n", t, (int)Math.ceil(workersNeeded.get(t)));
        }
      }
    }

    Map<Trade, Integer> m = new HashMap<>();
    for (Trade t : workersNeeded.keySet()) {
      m.put(t, workersNeeded.get(t) > 0.1 ? (int) Math.ceil(workersNeeded.get(t)) : 0);
    }

    // Find chance of finishing by deadline when adding workers
    double projectsWithAddedWorkers = 0.0;
    double projectsOnTimeWithAddedWorkers = 0.0;
    for (SimulationLog l : similarProjects) {
      if (l.addedWorkers > 0) {
        projectsWithAddedWorkers++;
        if (l.projectEnd == l.scheduledDeadline) {
          projectsOnTimeWithAddedWorkers++;
        }
      }
    }

    if (printToConsole) {
      double chanceOfFinishingOnTime = projectsOnTimeWithAddedWorkers / projectsWithAddedWorkers * 100;
      if (Double.isNaN(chanceOfFinishingOnTime)) {
        System.out.printf("%nWe do not have enough data on adding workers to similar projects to know whether it will make a difference");
      } else {
        System.out.printf("%n%3d similar projects with added workers have finished on time in %4.1f%% of the cases!", (int) projectsWithAddedWorkers, chanceOfFinishingOnTime);
      }
    }
    
    return m;
  }

  public void successRateNoAddedWorkers() {
    List<SimulationLog> logs = projectsByExtraWorkers.get(0);
    if (logs == null || logs.size() == 0) return;

    double all = 0;
    double success = 0;
    double totalDelay = 0;
    for (SimulationLog l : logs) {
      all++;
      totalDelay += (l.projectEnd - l.scheduledDeadline);
      if (l.projectEnd == l.scheduledDeadline) {
        success++;
      }
    }

    System.out.printf("    # Projects with no added workers: %5.0f project(s), %5.0f (%5.1f%%) within deadline (average delay: %5.1f days)...%n", all, success, success / all * 100, totalDelay / all);
  }

  public void successRateWithAddedWorkers() {
    List<SimulationLog> logs = new ArrayList<>();
    for (int i : projectsByExtraWorkers.keySet()) {
      if (i > 0) logs.addAll(projectsByExtraWorkers.get(i));
    }
    if (logs == null || logs.size() == 0) return;

    double all = 0;
    double success = 0;
    double totalDelay = 0;
    for (SimulationLog l : logs) {
      all++;
      totalDelay += (l.projectEnd - l.scheduledDeadline);
      if (l.projectEnd == l.scheduledDeadline) {
        success++;
      }
    }

    System.out.printf("    # Projects with added workers:    %5.0f project(s), %5.0f (%5.1f%%) within deadline (average delay: %5.1f days)...%n", all, success, success / all * 100, totalDelay / all);
  }

  private int[] resize(int[] arr, int sz) {
    if (sz < arr.length) return arr;

    int[] newArr = new int[sz];
    for (int i = 0; i < arr.length; i++) newArr[i] = arr[i];
    arr = newArr;
    return arr;
  }

  private class SimulationLog {

    Map<Trade, Integer> workerCounts = new HashMap<>();
    Map<Trade, Integer> extraWorkers = new HashMap<>();
    Map<Trade, List<Integer>> delays = new HashMap<>();
    int scheduledDeadline;
    int projectEnd;
    int addedWorkers;

    public SimulationLog(File f) throws IOException {
      BufferedReader br = new BufferedReader(new FileReader(f));
      String[] parameters;
      Trade trade;
      int workerCount;
      String line = br.readLine();
      while (line != null) {
        String[] category = line.split(":");
        switch (category[0]) {
          case "WorkerCount":
            parameters = category[1].split("=");
            trade = Trade.valueOf(parameters[0]);
            workerCount = Integer.parseInt(parameters[1]);
            workerCounts.put(trade, workerCount);
            break;

          case "ExtraWorkers":
            parameters = category[1].split("=");
            trade = Trade.valueOf(parameters[0]);
            workerCount = Integer.parseInt(parameters[1]);
            extraWorkers.put(trade, workerCount);
            addedWorkers += workerCount;
            break;

          case "Delay":
            String[] delay = category[1].split(",");
            String[] t = delay[0].split("=");
            trade = Trade.valueOf(t[1]);
            String[] d = delay[1].split("=");
            int day = Integer.parseInt(d[1]);
            if (delays.get(trade) == null) delays.put(trade, new ArrayList<>());
            delays.get(trade).add(day);
            break;

          case "ScheduledDeadline":
            parameters = category[1].split("=");
            scheduledDeadline = Integer.parseInt(parameters[1]);
            break;

          case "ProjectEnd":
            parameters = category[1].split("=");
            projectEnd = Integer.parseInt(parameters[1]);
            break;

        }
        line = br.readLine();
      }

      br.close();
    }
  }
}

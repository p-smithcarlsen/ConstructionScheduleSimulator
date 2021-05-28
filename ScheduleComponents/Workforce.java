package ScheduleComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ScheduleComponents.Contractor.Trade;

public class Workforce {

  public Contractor[] contractors;
  public Map<Contractor.Trade, int[]> contractorSchedules;
  public Map<Contractor.Trade, List<Task>> tradeTypesAndTasks;
  public int sz;
  public AlarmManager delays;

  public Workforce(Location[] locations, AlarmManager delays) {
    this.delays = delays;
    tradeTypesAndTasks = new HashMap<>();
    groupTasks(locations);
    updateContractorSchedules();
  }

  /**
   * Summarizes the trades (keys) and tasks (values) in a map structure. Subsequently
   * instantiates a contractor in the contractors array for each key.
   * @param locations is the array of locations, containing the task information
   */
  public void groupTasks(Location[] locations) {
    for (Location l : locations) {
      for (Task t : l.tasks) {
        if (tradeTypesAndTasks.containsKey(t.trade)) {
          tradeTypesAndTasks.get(t.trade).add(t);
        } else {
          tradeTypesAndTasks.put(t.trade, new ArrayList<>());
          tradeTypesAndTasks.get(t.trade).add(t);
        }
      }
    }
    this.contractors = new Contractor[tradeTypesAndTasks.size()];
    for (Contractor.Trade trade : tradeTypesAndTasks.keySet()) {
      contractors[sz] = new Contractor("C" + sz, trade);
      sz++;
    }
  }

  /**
   * Once the map of trades and related tasks is created, this function
   * iterates over all contractors, providing them with their tasks, so
   * they can calculate the worker demand throughout the construction
   * project.
   */
  public void updateContractorSchedules() {
    this.contractorSchedules = new HashMap<>();
    for (int i = 0; i < contractors.length; i++) {
      Contractor c = contractors[i];
      int[] contractorSchedule = c.calculateWorkerDemand(tradeTypesAndTasks.get(c.trade));
      contractorSchedules.put(c.trade, contractorSchedule);
    }
  }

  public Contractor getContractor(Trade trade) {
    for (Contractor c : contractors) {
      if (c.trade.equals(trade)) return c;
    }
    return null;
  }

  public Map<Trade, int[]> getContractorSchedule() {
    for (Contractor c : contractors) {
      contractorSchedules.put(c.trade, c.workerDemand);
    }
    return contractorSchedules;
  }

  public int[] getContractorSchedule(Trade trade) {
    Contractor c = getContractor(trade);
    return c.workerDemand;
  }

  public void addExtraWorkers(Map<Trade, Integer> extraWorkerSupply) {
    for (Trade t : extraWorkerSupply.keySet()) {
      getContractor(t).addExtraWorkers(extraWorkerSupply.get(t));
    }
  }

  public void alignSchedules(int today) {
    for (Contractor c : contractors) {
      c.alignSchedule(today);
    }
  }

  public void printContractorSchedules() {
    int longestSchedule = 0;
    for (Contractor c : contractors) {
      if (c.workerDemand.length > longestSchedule) longestSchedule = c.workerDemand.length+1;
    }
    String indices = "";
    for (int i = 0; i < longestSchedule; i++) {
      indices += String.format(" %3d", i);
    }
    System.out.printf("%-40s   %s%n", " ", indices);

    for (Contractor c : contractors) {
      int needed = 0;
      for (Task t : c.scheduledTasks) {
        double q = t.getRemainingQuantity();
        double p = (double) t.productionRate / (double) t.optimalWorkerCount;
        needed += Math.ceil(q / p);
      }
      System.out.printf("%30s: (%3d)", c.trade, needed);
      int hired = 0;
      for (int i = 0; i < longestSchedule; i++) {
        if (i < c.workerDemand.length) {
          hired += c.workerDemand[i];
        } else {
          hired += 0;
        }
      }
      System.out.printf(" (%3d)", hired);
      for (int i = 0; i < longestSchedule; i++) {
        if (i < c.workerDemand.length) {
          System.out.printf(" %3d", c.workerDemand[i]);
        } else {
          System.out.printf(" %3d", 0);
        }
      }
      System.out.println();
    }
  }

  public void printContractorsAndTasks(int day) {
    for (Contractor c : contractors) {
      c.printScheduleAndTasks(day);
    }
  }

  /**
   * Iterates through all contractors, letting them assign workers
   * to the task they should work on. Contractors will prioritise
   * assigning a number of workers equal to the optimal worker crew
   * in the given task. 
   * @param today is the given day, workers are being assigned
   */
  public void assignWorkers(int today, boolean manualInput) {
    boolean sickWorkers = false;
    for (Contractor c : contractors)
      sickWorkers = c.assignWorkers(today, delays, sickWorkers, manualInput);
  }

  public void endOfDay() {
    for (Contractor c : contractors) {
      c.availableWorkers = 0;
      c.sickWorkers = 0;
    }
  }
}

package ScheduleComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Workforce {

  public Contractor[] contractors;
  public Map<String, int[]> contractorSchedules;
  public Map<String, List<Task>> tradeTypesAndTasks;
  public int sz;
  public AlarmManager delays;
  // public boolean idleWorkers;

  public Workforce(Location[] locations, AlarmManager delays) {
    this.delays = delays;
    tradeTypesAndTasks = new HashMap<>();
    groupTasks(locations);
    updateContractorSchedules();
  }

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
    for (String trade : tradeTypesAndTasks.keySet()) {
      contractors[sz] = new Contractor("C" + sz, trade);
      sz++;
    }
  }

  /**
   * Iterates through all locations and their tasks, creating a map
   * with the trade types and a list of their assigned tasks as 
   * values. Subsequently, all contractors will be given their respective
   * tasks and calculate the resulting worker demand.
   * @param locations
   */
  public void updateContractorSchedules() {
    // Align worker supply with tasks
    this.contractorSchedules = new HashMap<>();
    for (int i = 0; i < contractors.length; i++) {
      Contractor c = contractors[i];
      int[] contractorSchedule = c.calculateWorkerDemand(tradeTypesAndTasks.get(c.trade));
      contractorSchedules.put(c.trade, contractorSchedule);
    }
  }

  public Contractor getContractor(String trade) {
    for (Contractor c : contractors) {
      if (c.trade.equals(trade)) return c;
    }
    return null;
  }

  public int[] getContractorSchedule(String trade) {
    Contractor c = getContractor(trade);
    return c.workerDemand;
  }

  /**
   * Iterates through all contractors, letting them assign workers
   * to the task they should work on. Contractors will prioritise
   * assigning a number of workers equal to the optimal worker crew
   * in the given task. 
   * @param today
   */
  public void assignWorkers(int today) {
    for (Contractor c : contractors)
      c.assignWorkers(today, delays);
  }

  // public void resolveDelay(Alarm d) {
  //   getContractor(d.origin).resolveDelay(d);
  // }

  /**
   * Will only be triggered once a delay has been encountered. This 
   * method walks through
   * @param day is the current day
   * @param forecast is how many days there should be forecasted
   */
  // public void checkWorkerSupply(int day, Alarm d) {
  //   getContractor(d.origin).checkWorkerSupply(day, delays, d);
  // }
}

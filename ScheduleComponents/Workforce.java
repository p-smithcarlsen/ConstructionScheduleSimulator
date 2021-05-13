package ScheduleComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Workforce {
  public Contractor[] contractors;
  public int sz;
  public boolean idleWorkers;

  public Workforce(int typesOfContractors) {
    this.contractors = new Contractor[typesOfContractors];
    this.sz = 0;
    this.idleWorkers = false;
  }

  public Workforce(Location[] locations) {
    groupTasks(locations);
  }

  public void groupTasks(Location[] locations) {
    // Summarize necessary trades and related tasks
    // TODO: Get overview from LBMS object
    Map<String, List<Task>> tradeTypesAndTasks = new HashMap<>();
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

    // Hire contractors and tell them about their scheduled tasks
    this.contractors = new Contractor[tradeTypesAndTasks.size()];
    for (String trade : tradeTypesAndTasks.keySet()) {
      contractors[sz] = new Contractor("C" + sz, trade);
      contractors[sz].calculateWorkerDemand(tradeTypesAndTasks.get(trade));
      sz++;
    }
  }

  public void assignWorkers(int today) {
    for (Contractor c : contractors)
      c.assignWorkers(today);
  }

  public void endOfTheDay() {
    for (Contractor c : contractors)
      c.availableWorkers = c.workers;
  }

  public void print() {
    for (Contractor c : contractors)
      c.print();
  }
}

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
    Map<String, List<Task>> tt = new HashMap<>();   // TODO: rename
    for (Location l : locations) {
      for (Task t : l.tasks) {
        if (tt.containsKey(t.trade)) {
          tt.get(t.trade).add(t);
        } else {
          tt.put(t.trade, new ArrayList<>());
          tt.get(t.trade).add(t);
        }
      }
    }

    // Hire contractors and tell them about their scheduled tasks
    this.contractors = new Contractor[tt.size()];
    for (String trade : tt.keySet()) {
      contractors[sz] = new Contractor("C" + sz, trade);
      contractors[sz].calculateWorkerDemand(tt.get(trade));
      sz++;
    }
  }

  public void assignWorkers(int today) {
    for (Contractor c : contractors)
      c.assignWorkers(today);
  }

  // public int typesOfContractors() {
  //   return contractors.length;
  // // }

  // public void addContractor(String[] tradeParameters) {
  //   contractors[sz] = new Contractor(String.format("C%d", sz), tradeParameters[0]);
  //   sz++;
  // }

  // public void workOn(Task t) {
  //   t.work(getWorkers(t.trade).assignWorkers(t.quantity * (100 - t.progress) / 100));
  // }

  // private Contractor getWorkers(String trade) {
  //   for (Contractor c : contractors) {
  //     if (c.trade.equals(trade)) return c;
  //   }
  //   return null;
  // }

  public void endOfTheDay() {
    for (Contractor c : contractors)
      c.availableWorkers = c.workers; // hvorfor ikke bare 0 her, når workers aldrig bliver kaldt noget sted?
  }

  public void print() {
    for (Contractor c : contractors)
      c.print();
  }
}

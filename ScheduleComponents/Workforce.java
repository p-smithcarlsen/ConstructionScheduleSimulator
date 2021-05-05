package ScheduleComponents;

public class Workforce {
  public 
  public Contractor[] contractors;
  public int sz;
  public boolean idleWorkers;

  public Workforce(int typesOfContractors) {
    this.contractors = new Contractor[typesOfContractors];
    this.sz = 0;
    this.idleWorkers = false;
  }

  public Workforce(Location[] locations) {
    for (Location l : locations) {
      for (Task t : l.tasks) {
        insertIntoSchedule(t);
      }
    }
    // Find out what contractors to use
    // Find out how many workers each contractor needs
  }

  public void insertIntoSchedule(Task t) {

  }

  public int typesOfContractors() {
    return contractors.length;
  }

  public void addContractor(String[] tradeParameters) {
    contractors[sz] = new Contractor(String.format("C%d", sz), tradeParameters);
    sz++;
  }

  public void calculateWorkerDemand() {

  }

  public void workOn(Task t) {
    t.work(getWorkers(t.trade).assignWorkers(t.quantity * (100 - t.progress) / 100));
  }

  private Contractor getWorkers(String trade) {
    for (Contractor c : contractors) {
      if (c.trade.equals(trade)) return c;
    }
    return null;
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

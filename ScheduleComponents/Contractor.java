package ScheduleComponents;

public class Contractor {
  public String id;
  public String name;
  public String trade;
  public int[] workerDemand;
  public int workers;
  public int availableWorkers;

  public Contractor(String id, String[] tradeParameters) {
    this.id = id;
    this.trade = tradeParameters[0];
    // this.workers = Integer.parseInt(tradeParameters[1]);
    // this.availableWorkers = this.workers;
  }

  public void calculateWorkerDemand() {

  }

  public int assignWorkers(double quantity) {
    int w = (int)Math.ceil(Math.min(availableWorkers, quantity));
    System.out.println(String.format("Contractor %s providing %s workers of %s available", id, w, availableWorkers));
    availableWorkers = availableWorkers - w;
    return w;
  }

  public void print() {
    System.err.println(String.format("%n%s:", this.trade));
    System.out.println(String.format(" --Number of workers: %s", this.workers));
  }
}

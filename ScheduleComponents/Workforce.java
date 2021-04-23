package ScheduleComponents;

public class Workforce {
  private Contractor[] contractors;
  private int sz;

  public Workforce(int typesOfContractors) {
    this.contractors = new Contractor[typesOfContractors];
  }

  public int typesOfContractors() {
    return contractors.length;
  }

  public void addContractor(String[] tradeParameters) {
    contractors[sz] = new Contractor(String.format("C%d", sz), tradeParameters);
    sz++;
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

  private class Contractor {
    private String id;
    private String trade;
    private int workers;
    private int availableWorkers;

    public Contractor(String id, String[] tradeParameters) {
      this.id = id;
      this.trade = tradeParameters[0];
      this.workers = Integer.parseInt(tradeParameters[1]);
      this.availableWorkers = this.workers;
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
}

package ScheduleComponents;

public class Trade {
  private String trade;
  private int workers;

  public Trade(String[] tradeParameters) {
    this.trade = tradeParameters[0];
    this.workers = Integer.parseInt(tradeParameters[1]);
  }

  public void print() {
    System.err.println(String.format("%n%s:", this.trade));
    System.out.println(String.format(" --Number of workers: %s", this.workers));
  }
}

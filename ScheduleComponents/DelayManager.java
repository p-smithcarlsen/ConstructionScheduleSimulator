package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class DelayManager {
  
  public List<Delay> delays;
  boolean unresolvedDelay;

  public DelayManager() {
    this.delays = new ArrayList<>();
  }

  public void addDelays(Delay d) {
    delays.add(d);
    unresolvedDelay = true;
  }

  public boolean unresolvedDelay() {
    for (Delay d : delays) {
      if (!d.resolved) return true;
    }
    return unresolvedDelay;
  }

  public void delayResolved(Delay d) {
    d.resolve();
  }
}

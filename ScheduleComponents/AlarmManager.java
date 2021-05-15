package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class AlarmManager {
  
  public List<Alarm> delays;
  boolean unresolvedDelay;

  public AlarmManager() {
    this.delays = new ArrayList<>();
  }

  public void addDelays(Alarm d) {
    delays.add(d);
    unresolvedDelay = true;
  }

  public boolean unresolvedDelay() {
    for (Alarm d : delays) {
      if (!d.resolved) return true;
    }
    return unresolvedDelay;
  }

  public List<Alarm> getUnresolvedDelays() {
    List<Alarm> unresolved = new ArrayList<>();
    for (Alarm d : delays) {
      if (!d.resolved) unresolved.add(d);
    }
    return unresolved;
  }

  public void delayResolved(Alarm d) {
    d.resolve();
  }
}

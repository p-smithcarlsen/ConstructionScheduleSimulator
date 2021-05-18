package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class AlarmManager {
  
  public List<Alarm> alarms;
  boolean unresolvedDelay;

  public AlarmManager() {
    this.alarms = new ArrayList<>();
  }

  public void addDelays(Alarm d) {
    alarms.add(d);
    unresolvedDelay = true;
  }

  public boolean unresolvedDelay() {
    for (Alarm d : alarms) {
      if (!d.resolved) return true;
    }
    return unresolvedDelay;
  }

  public List<Alarm> getUnresolvedAlarms() {
    List<Alarm> unresolved = new ArrayList<>();
    for (Alarm d : alarms) {
      if (!d.resolved) unresolved.add(d);
    }
    return unresolved;
  }

  public void delayResolved(Alarm d) {
    d.resolve();
  }
}

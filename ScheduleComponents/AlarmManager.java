package ScheduleComponents;

import java.util.ArrayList;
import java.util.List;

public class AlarmManager {
  
  public List<Alarm> alarms;
  boolean unresolvedAlarm;

  public AlarmManager() {
    this.alarms = new ArrayList<>();
  }

  public void addDelays(Alarm d) {
    alarms.add(d);
    unresolvedAlarm = true;
  }

  /**
   * 
   * @return
   */
  public boolean unresolvedDelay() {
    for (Alarm d : alarms) {
      if (!d.resolved) return true;
    }
    return unresolvedAlarm;
  }

  /**
   * 
   * @return
   */
  public List<Alarm> getUnresolvedAlarms() {
    List<Alarm> unresolved = new ArrayList<>();
    for (Alarm d : alarms) {
      if (!d.resolved) unresolved.add(d);
    }
    return unresolved;
  }

  /**
   * 
   * @param d
   */
  public void resolveAlarm(Alarm a) {
    a.resolve();
  }
}

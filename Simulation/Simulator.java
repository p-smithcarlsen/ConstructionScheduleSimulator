package Simulation;

import ScheduleComponents.Location;
import ScheduleComponents.Task;
import ScheduleComponents.Workforce;

public class Simulator {

  public static void runSimulation(Location[] locations, Workforce workforce) {
    int timeUnit = 1;
    while (true) {
      int remainingLocations = locations.length;
      System.err.println(String.format("Day: %s", timeUnit));
      for (Location l : locations) {
        if (l.isFinished()) {
          remainingLocations--;
          System.out.println(String.format("Location %s finished!", l.id));
        } else {
          Task nextTask  = l.getTask();
          workforce.workOn(nextTask);
        }
      }

      workforce.endOfTheDay();
      timeUnit++;
      if (remainingLocations == 0) break;
      if (timeUnit > 20) break;
    }
  }
}

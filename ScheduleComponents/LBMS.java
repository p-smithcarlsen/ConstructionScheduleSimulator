package ScheduleComponents;

public class LBMS {

  public Location[] locations;

  public LBMS(Location[] locations) {
    this.locations = locations;
  }

  public void prepareLocations() {
    for (Location l : locations) {
      createDependencies();   // between tasks
      // Can we do the two next ones without having established all dependencies?
      // I.e. do we need to do another loop after this loop?
      l.calculateDuration();    // of location
      l.forwardPass();          // i.e. durations of tasks
    }
  }

  public void createDependencies() {
    for (Location l : locations) {
      for (Task t : l.tasks) {
        String[] dependencies = t.getDependencies().split(",");
        if (dependencies[0].equals("*")) continue;
        for (String d : dependencies) {
          String[] dependency = d.split("=");
          Location l2 = getLocation(dependency[0]);
          Task predecessor = l2.getTask(dependency[1]);
          t.addPredecessor(predecessor);
        }
      }
    }
  }

  public Location getLocation(String id) {
    for (Location l : locations) {
      if (l.id.equals(id)) return l;
    }

    return null;
  }

  public void work() {
    for (Location l : locations) {
      for (Task t : l.tasks) {
        t.work();
      }
    }
  }
}

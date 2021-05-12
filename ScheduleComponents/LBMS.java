package ScheduleComponents;

public class LBMS {

  public Location[] locations;
  
  public LBMS(Location[] locations) {
    this.locations = locations;
  }

  public void prepareLocations() {
    for (Location l : locations) {
      createDependencies(l);   // between tasks
      // Can we do the two next ones without having established all dependencies?
      // I.e. do we need to do another loop after this loop?
      l.calculateDuration();    // of location
      l.forwardPass();          // i.e. durations of tasks
    }
  }

  public void createDependencies(Location l) {
    //for (Location l : locations) { // double location loop, parsed from earlier instead
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
    //}
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

  public void totalProgress(){
    int numTotalTasks = 0;
    int numTotalCompletedTasks = 0;
    for (Location l : locations) {
      int numTasksLocation = 0;
      int numCompletedTasksAtLocation = 0;
      for (Task t : l.tasks) {
        numTasksLocation++;
        if (t.isFinished()) {
          numCompletedTasksAtLocation++;
        }
        
      }
      System.out.println("Location: " + "" + l.id + " Completed Tasks: " + numTasksLocation + "/" + numCompletedTasksAtLocation);
      numTotalTasks += numTasksLocation;
      numTotalCompletedTasks += numCompletedTasksAtLocation;
    }
    System.out.println("Total Completed Tasks: " + numTotalTasks + "/" + numTotalCompletedTasks);
  }

  public void printTasks(){
    for (Location l : locations) {
        System.out.println(l.id);
        for (Task t : l.tasks) {
          System.out.println(t.id + " " + t.earliestStart + " " + t.earliestFinish);
          System.out.println("predecessors " + t.predecessorTasks.size());
          for (Task tt : t.predecessorTasks) {
            System.out.println(tt.id);
          }
        }
    }
  }
}

import java.io.IOException;
import java.util.Arrays;

import Data.DataGenerator;
import Data.DataParser;
import ScheduleComponents.Location;
import ScheduleComponents.Workforce;
import Simulation.Simulator;

public class Program {

  private static Location[] locations;
  // private static Workforce workforce;

  /*
  To-do:
  - For each location, calculate the necessary workforce throughout the project (for each time unit)
  - Tracker, that always have an estimated forecast for when project is finished
  - Make task durations probability distributions
  - Make tasks into node network
      - Create algorithm to check for cycles
  - Create a Task prototype (on random) instead of a random task with random properties from arrays
  - Make it possible to use "takt" in project
  - More variability in the projects possible to create (only repetitive/not repetitive)
      - Dependencies between repetitive tasks (task 3 in locations 2 depends on task 3 in location 1...)
      - How to implement layer 4 logic? Buffers in place fx
      - Insert "special" dependencies as well? I.e. layer 5 logic, task 2.3 must be before task 3.1
      - Have more tasks that do not depend on each other (i.e. can choose between two tasks to do first)
      - A workable backlog (i.e. tasks that do not have predecessors - a little same as above)
          - This also means that it makes sense to go on the critical path and only take free tasks whenever resources are available
  - Some workers providing more or less than 1 hour work per hour worked (factor 0.9 fx)
  - Putting difficulty on tasks, so work is less effective
  - Give each contractor a list of tasks they have to do (in order)

  For later:
  - Re-arranging tasks, e.g. changing critical path
  - Splitting tasks Seppänen & Kenley, 2010: pp. 156
  */

  public static void main(String[] args) throws IOException {
    runSmallSchedule();
    Simulator.runSimulation(locations);
  }

  /**
   * 
   * @throws NumberFormatException
   * @throws IOException
   */
  public static void runSmallSchedule() throws NumberFormatException, IOException {
    String filePath = createDataset("Data/ScheduleData", 5, 10);
    readDataIntoObjects(filePath);
  }
  
  /**
   * 
   * @throws NumberFormatException
   * @throws IOException
   */
  public static void runMediumSchedule() throws NumberFormatException, IOException {
    createDataset("Data/ScheduleData", 10, 25);
  }

  /**
   * 
   * @throws NumberFormatException
   * @throws IOException
   */
  public static void runLargeSchedule() throws NumberFormatException, IOException {
    createDataset("Data/ScheduleData", 25, 60);
  }

  /**
   * 
   * @param fileDir
   * @param NO_LOCATIONS
   * @param NO_TASKS_PER_LOCATION
   * @throws NumberFormatException
   * @throws IOException
   */
  public static String createDataset(String fileDir, int NO_LOCATIONS, int NO_TASKS_PER_LOCATION) throws NumberFormatException, IOException {
    DataGenerator g = new DataGenerator();
    return g.generateDataset(fileDir, NO_LOCATIONS, NO_TASKS_PER_LOCATION);
  }

  /**
   * 
   * @param filePath
   * @throws NumberFormatException
   * @throws IOException
   */
  public static void readDataIntoObjects(String filePath) throws NumberFormatException, IOException {
    DataParser p = new DataParser();
    p.parseData(filePath);
    locations = p.getLocations();
    // workforce = p.getWorkforce();
    
    // Print data
    // Arrays.stream(locations).forEach(l -> l.print());
    // Arrays.stream(trades).forEach(t -> t.print());
  }
}
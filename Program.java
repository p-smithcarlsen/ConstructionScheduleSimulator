import java.io.IOException;

import Data.DataGenerator;
import Data.DataParser;
import ScheduleComponents.ConstructionProject;
import Simulation.Simulator;

public class Program {

  private static ConstructionProject constructionProject;

  /*
  To-do:
  - Make task durations probability distributions
  - Make tasks into node network
      - Create algorithm to check for cycles
  - Make it possible to use "takt" in project
  - More variability in the projects possible to create (only repetitive/not repetitive)
      - How to implement layer 4 logic? Buffers in place fx
      - Insert "special" dependencies as well? I.e. layer 5 logic, task 2.3 must be before task 3.1
      - Have more tasks that do not depend on each other (i.e. can choose between two tasks to do first)
      - A workable backlog (i.e. tasks that do not have predecessors - a little same as above)
          - This also means that it makes sense to go on the critical path and only take free tasks whenever resources are available

  - allocate idle workers to other tasks, if possible
  - enum on contractor type?
  - run 1000 (or many) times and save data for a database
  - find out when workers become idle (forecast)
  - include buffers in dependencies

  For later:
  - Include logic relationship in dependencies (F-S, F-F, S-S, S-F)
  */

  public static void main(String[] args) throws IOException {
    // runSmallSchedule(true);
    // Simulator s = new Simulator();
    // s.runSimulation(constructionProject);
    loadScheduleData("dataset_54.csv");
    Simulator s = new Simulator();
    s.runSimulation(constructionProject);
  }

  public static void loadScheduleData(String filePath) throws NumberFormatException, IOException {
    readDataIntoObjects(filePath);
  }

  /**
   * Creates a small schedule (5 locations, 5 tasks per location), parses 
   * the resulting data into objects and simulates the project.
   * @throws NumberFormatException
   * @throws IOException
   */
  public static void runSmallSchedule(boolean repetitive) throws NumberFormatException, IOException {
    String filePath = "";
    if (repetitive) { filePath = createDataset("Data/ScheduleData", 5, 5, true); }
    else { filePath = createDataset("Data/ScheduleData", 5, 5, false); }
    readDataIntoObjects(filePath);
  }
  
  /**
   * Creates a small schedule (10 locations, 25 tasks per location), parses 
   * the resulting data into objects and simulates the project.
   * @throws NumberFormatException
   * @throws IOException
   */
  public static void runMediumSchedule(boolean repetitive) throws NumberFormatException, IOException {
    String filePath = "";
    if (repetitive) { filePath = createDataset("Data/ScheduleData", 10, 25, true); }
    else { filePath = createDataset("Data/ScheduleData", 10, 25, false); }
    readDataIntoObjects(filePath);
  }

  /**
   * Creates a small schedule (25 locations, 60 tasks per location), parses 
   * the resulting data into objects and simulates the project.
   * @throws NumberFormatException
   * @throws IOException
   */
  public static void runLargeSchedule(boolean repetitive) throws NumberFormatException, IOException {
    String filePath = "";
    if (repetitive) { filePath = createDataset("Data/ScheduleData", 25, 60, true); }
    else { filePath = createDataset("Data/ScheduleData", 25, 60, false); }
    readDataIntoObjects(filePath);
  }

  /**
   * Creates a dataset in the given dile directory. Number of locations and 
   * tasks are specified as parameters. It is possible to make tasks repetitive
   * (parameter repetitive = true) or random (repetitive = false).
   * @param fileDir
   * @param NO_LOCATIONS
   * @param NO_TASKS_PER_LOCATION
   * @throws NumberFormatException
   * @throws IOException
   * @return the name of the file created
   */
  public static String createDataset(String fileDir, int NO_LOCATIONS, int NO_TASKS_PER_LOCATION, boolean repetitive) throws NumberFormatException, IOException {
    DataGenerator g = new DataGenerator();
    return g.generateDataset(fileDir, NO_LOCATIONS, NO_TASKS_PER_LOCATION, repetitive);
  }

  /**
   * Reads the dataset specified by the filePath parameter and loads
   * the data into an LBMS object. 
   * @param filePath
   * @throws NumberFormatException
   * @throws IOException
   */
  public static void readDataIntoObjects(String filePath) throws NumberFormatException, IOException {
    DataParser p = new DataParser();
    p.parseData(filePath);
    constructionProject = new ConstructionProject(p.getTasks(), p.getLocations());
  }
}
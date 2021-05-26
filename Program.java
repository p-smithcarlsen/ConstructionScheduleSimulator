import java.io.IOException;

import Data.DataGenerator;
import Data.DataParser;
import ScheduleComponents.ConstructionProject;
import Simulation.Analyzer;
import Simulation.Logger;
import Simulation.Simulator;

public class Program {

  private static ConstructionProject constructionProject;
  public static String filePath;

  public static void main(String[] args) throws IOException {
    // for (int i = 0; i < 100; i++) {
      runSmallSchedule(true);
      Analyzer a = new Analyzer();
      a.analyzeData();
      Logger l = new Logger(findLogName());
      Simulator s = new Simulator();
      s.runSimulation(constructionProject, l, a);
      // loadScheduleData("dataset_133.csv");
      // Simulator s = new Simulator();
      // s.runSimulation(constructionProject);
    // }
  }

  public static int findLogName() {
    int underscoreIndex = filePath.indexOf("_");
    int fileNumber = 0;
    try {
      fileNumber = Integer.parseInt(filePath.substring(underscoreIndex+1, filePath.indexOf(".")));
    } catch (NumberFormatException e) {}

    return fileNumber;
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
    filePath = "";
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
    filePath = "";
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
    filePath = "";
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
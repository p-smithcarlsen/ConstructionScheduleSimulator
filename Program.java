import java.io.File;
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
    System.out.println("Deleting class files...");
    deleteClassFiles(".");
    System.out.println("Resetting database...");
    resetScheduleDataAndLogs();
    int n = 1000;
    for (int i = 0; i < n; i++) {
    // int i = 0;
    // while (true) {
      runSmallSchedule(true);
      Analyzer a = new Analyzer();
      a.analyzeData();
      Logger l = new Logger(findLogName());
      Simulator s = new Simulator();
      s.runSimulation(constructionProject, l, a, i > n/2 && i % 2 == 0);
      // s.runSimulation(constructionProject, l, a, i > 500 && i % 2 == 0);
      a.successRateNoAddedWorkers();
      a.successRateWithAddedWorkers();
      // i++;
    }
  }

  public static void resetScheduleDataAndLogs() {
    File dir = new File("Data/Database");
    for (File f : dir.listFiles()) {
      if (!f.isDirectory()) f.delete();
    }

    dir = new File("Data/ScheduleData");
    for (File f : dir.listFiles()) {
      if (!f.isDirectory()) f.delete();
    }
  }

  public static void deleteClassFiles(String dir) {
    File ff = new File(dir);
    for (File f : ff.listFiles()) {
      if (f.getName().startsWith(".")) continue;
      if (f.isDirectory()) {
        deleteClassFiles(f.getAbsolutePath());
      } else {
        String name = f.getName();
        if (name.endsWith(".class")) f.delete();
      }
    }
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
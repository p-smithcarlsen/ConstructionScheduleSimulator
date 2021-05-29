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
    if (args[0].equals("schedule")) {
      if (args[1].equals("true")) {
        simulateSchedule(true);
      } else {
        simulateSchedule(false);
      }
    } else if (args[0].equals("experiment")) {
      int n = 0;
      try {
        n = Integer.parseInt(args[1]);
      } catch (Exception e) {
        System.out.println("Invalid input...");
        return;
      }
      runExperiment(n);
    } else {
      System.out.println("Invalid input...");
      return;
    }
  }

  public static void simulateSchedule(boolean manualInput) throws IOException {
    runSmallSchedule(true);
    Analyzer a = new Analyzer();
    a.analyzeData();
    Logger l = new Logger(findLogName());
    Simulator s = new Simulator();
    s.runSimulation(constructionProject, l, a, true, true, manualInput);
  }

  public static void runExperiment(int n) throws IOException {
    System.out.println("\n    # Database has been reset...\n");
    resetScheduleDataAndLogs();
    Analyzer a = new Analyzer();
    System.out.println("                 0" + " ".repeat(50) + "100%");
    System.out.printf("    # Progress:  |");
    double progress = 0.0;
    int printed = 0;
    for (int i = 0; i < n; i++) {
      progress = (double)(i+1) / (double)n * 100.0; 
      if (i == n-1) {
        System.out.printf("|".repeat((100-printed)/2) + "||%n%n");
      } else if(progress > printed) {
        while (progress > printed) {
          System.out.printf("|");
          printed += 2;
        }
      }
      runSmallSchedule(true);
      a = new Analyzer();
      a.analyzeData();
      Logger l = new Logger(findLogName());
      Simulator s = new Simulator();
      boolean addWorkers = i > n/4 && i % 2 == 0;
      s.runSimulation(constructionProject, l, a, addWorkers, false, false);
    }
    a.successRateNoAddedWorkers();
    a.successRateWithAddedWorkers();
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
    int locations = 5;
    int tasksPerLocations = 5;
    if (repetitive) { filePath = createDataset("Data/ScheduleData", locations, tasksPerLocations, true); }
    else { filePath = createDataset("Data/ScheduleData", locations, tasksPerLocations, false); }
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
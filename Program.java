import java.io.IOException;
import java.util.Arrays;

import Data.DataGenerator;
import Data.DataParser;
import ScheduleComponents.Location;
import ScheduleComponents.Workforce;
import Simulation.Simulator;

public class Program {

  private static Location[] locations;
  private static Workforce workforce;

  public static void main(String[] args) throws IOException {
    runSmallSchedule();
    Simulator.runSimulation(locations, workforce);
  }

  /**
   * 
   * @throws NumberFormatException
   * @throws IOException
   */
  public static void runSmallSchedule() throws NumberFormatException, IOException {
    createDataset("Data/ScheduleData", 5, 10);
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
  public static void createDataset(String fileDir, int NO_LOCATIONS, int NO_TASKS_PER_LOCATION) throws NumberFormatException, IOException {
    // Create data
    DataGenerator g = new DataGenerator();
    String filePath = g.generateDataset(fileDir, NO_LOCATIONS, NO_TASKS_PER_LOCATION);

    // Read data
    DataParser p = new DataParser();
    p.parseData(filePath);
    locations = p.getLocations();
    workforce = p.getWorkforce();
    
    // Print data
    // Arrays.stream(locations).forEach(l -> l.print());
    // Arrays.stream(trades).forEach(t -> t.print());
  }
}
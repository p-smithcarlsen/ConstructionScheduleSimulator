package Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DataGenerator {

  // Notice how activities of index x can be completed by trades of index x
  private String[] locationNames = new String[]{ "Ground Level", "First Floor", "Second Floor", "Third Floor", "Fourth Floor" };
  private String[] activities = new String[]{ "Wood Framing", "Finishing", "Electricity", "Flooring", "Glass Installation", "Heating/AC", "Insulation", "Sewage", "Piping", "Interior Walls" };
  private String[] trades = new String[]{ "Carpenter", "Cement/Concrete Finisher", "Electrician", "Flooring Installer", "Glazier", "HVAC Tech", "Insulation Worker", "Plumber", "Roofing Mechanic", "Painter" };
  private int[] optimalCrews = new int[]{ 3, 3, 2, 3, 2, 2, 4, 4, 3, 4 };
  private double[] quantities = new double[]{ 12.0, 8.0, 6.0, 10.5, 7.0, 9.0, 6.5, 12.0, 7.0, 9.5 };
  private int[] productionRates = new int[]{ 3, 3, 4, 2, 2, 4, 3, 2, 4, 3 };
  private Random r = new Random();

  /**
   * Opens a dataset file in the given directory. If file is already in place,
   * creates a new file with identical name and counter at the end, i.e. abc_X.csv
   * @param directory the folder where the file should be created
   * @param locations
   * @param tasksPerLocation
   * @param repetitive determines whether tasks should be repetitive or random over locations
   * @return the name of the file created in the given directory
   */
  public String generateDataset(String directory, int locations, int tasksPerLocation, boolean repetitive) {
    File f = new File(String.format("%s/dataset.csv", directory));
    int i = 2;
    try {
      while (!f.createNewFile()) {
        f = new File(String.format("%s/dataset_%d.csv", directory, i));
        i++;
      }

      // Create filewriter and generate data
      FileWriter w = new FileWriter(f);
      if (repetitive) { w.write(generateRepetitiveTaskData(locations, tasksPerLocation)); }
      else { w.write(generateTaskData(locations, tasksPerLocation)); }
      w.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return f.getName();
  }

  /**
   * Generates a very randomized data set of size n where all tasks in a 
   * location are linearly dependent on the previous task
   * @param locations
   * @param tasksPerLocation
   * @return a string with metadata on locations and tasks for each location
   */
  public String generateTaskData(int locations, int tasksPerLocation) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s %s%n", locations, tasksPerLocation*locations));

    for (int i = 0; i < locations; i++) {
      sb.append(createLocation(i));
      
      for (int j = 0; j < tasksPerLocation; j++) {
        sb.append(createTask(j, i, -1));

      }
    }

    return sb.toString();
  }

  /**
   * Generates one set of randomized tasks for a location, and duplicates this
   * set for each of the remaining locations. All tasks are linearly dependent
   * on the previous task. Also, first tasks in other locations are dependent
   * on the first task in location 0. 
   * @param locations
   * @param tasksPerLocation
   * @return a string with metadata on locations and tasks for each location
   */
  public String generateRepetitiveTaskData(int locations, int tasksPerLocation) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s %s%n", locations, tasksPerLocation*locations));
    int[] tasks = new int[tasksPerLocation];
    for (int i = 0; i < tasksPerLocation; i++) { tasks[i] = r.nextInt(activities.length); }

    for (int i = 0; i < locations; i++) {
      sb.append(createLocation(i));
      for (int j = 0; j < tasks.length; j++) {
        sb.append(createTask(j, i, tasks[j]));
      }
      // System.out.println(sb.toString());
    }

    return sb.toString();
  }

  /**
   * Creates the metadata, i.e. id and name, of a location
   * @param locationId
   * @return a string with the created metadata
   */
  public String createLocation(int locationId) {
    String locationName = locationNames[r.nextInt(locationNames.length)];
    return String.format("L%d;%s%n", locationId, locationName);
  }

  /**
   * Creates the metadata for a single task. If the rand parameter is -1, 
   * the task will be randomized. If not, the rand int will determine the
   * index in the various metadata arrays, which determines the content
   * of the task
   * @param taskId the id of the task to be created
   * @param locationId the id of the location, to which the task will be assigned
   * @param @rand if -1, the task will be randomized. Otherwise, rand determines data
   * @return a string with the metadata of the task
   */
  public String createTask(int taskId, int locationId, int rand) {
    if (rand == -1) rand = r.nextInt(activities.length);
    String activity = activities[rand];
    String trade = trades[rand];
    int optimalCrew = optimalCrews[rand];
    double quantity = quantities[rand];
    int productionRate = productionRates[rand];
    String dependency = "";
    dependency = "L";
    if (taskId == 0) {
      if (locationId > 0 && locationId != 3) {
        dependency += locationId-1 + "=T0";
      } else {
        dependency = "*";
      }
    } else if (taskId > 0) {
      dependency += String.format("%d=T%d", locationId, taskId-1);
      if (locationId == 3 && taskId == 2) dependency += String.format(",L%d=T%d", locationId-1, taskId); 
    }
    return String.format("T%d;L%d;%s;%s;%d;%2.2f;%d;%s%n", taskId, locationId, activity, trade, optimalCrew, quantity, productionRate, dependency);
  }

  public static void main(String[] args) {
    DataGenerator g = new DataGenerator();
    g.generateDataset("Data/ScheduleData", 5, 5, true);
  }
}

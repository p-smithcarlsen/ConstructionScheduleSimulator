package MikkelPhilipThesis.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DataGenerator {

  public DataGenerator() {}
  
  private Random r = new Random();
  private String[] locationNames = new String[]{"Ground Level", "First Floor", "Second Floor", "Third Floor", "Fourth Floor"};
  // Notice how activities of index x can be completed by trades of index x
  private String[] activities = new String[]{
    "Wood Framing", "Finishing", "Electricity", "Flooring", "Glass Installation", 
    "Heating/AC", "Insulation", "Sewage", "Piping", "Interior Walls"};
  private String[] trades = new String[]{
    "Carpenter", "Cement/Concrete Finisher", "Electrician", "Flooring Installer", "Glazier", 
    "HVAC Tech", "Insulation Worker", "Plumber", "Roofing Mechanic", "Painter"};
  private double[] quantities = new double[]{
    3.0, 2.0, 1.5, 3.5, 2.0, 2.0, 1.5, 3.0, 2.0, 2.5};

  /**
   * Opens a dataset file in the given directory. If file is already in place,
   * creates a new file with identical name and counter at the end, i.e. abc_X.csv
   * @param path
   * @return
   */
  public File createNewFile(String directory) {
    File f = new File(String.format("%s/dataset.csv", directory));
    int i = 2;
    try {
      while (!f.createNewFile()) {
        f = new File(String.format("%s/dataset_%d.csv", directory, i));
        i++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return f;
  }

  /**
   * Generates a randomized data set of size n
   * @param n
   */
  public String generateData(int locations, int tasks) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s%n", locations));

    for (int i = 0; i < locations; i++) {
      sb.append(createLocation(i));
      
      for (int j = 0; j < tasks; j++) {
        sb.append(createTask(j, i));

      }
    }

    return sb.toString();
  }

  /**
   * 
   * @param locationId
   * @return
   */
  public String createLocation(int locationId) {
    String locationName = locationNames[r.nextInt(locationNames.length)];   // So far, location names are not important and may be duplicates
    return String.format("L%d;%s%n", locationId, locationName);
  }

  /**
   * 
   * @param taskId
   * @param locationId
   * @return
   */
  public String createTask(int taskId, int locationId) {
    int rand = r.nextInt(activities.length);
    String activity = activities[rand];
    String trade = trades[rand];
    double quantity = quantities[rand];
    String dependency = "*";
    if (taskId > 0) dependency = String.format("T%d", taskId-1);
    return String.format("T%d;L%d;%s;%s;%2.2f;%s%n", taskId, locationId, activity, trade, quantity, dependency);
  }

  public static void main(String[] args) {
    // Create file
    DataGenerator g = new DataGenerator();
    File f = g.createNewFile("Data/ScheduleData");
    
    int locations, tasks;
    if (args.length < 2) {
      locations = 5;                              // Adjust number of locations
      tasks = 5;                                  // Adjust number of tasks
    } else {
      locations = Integer.parseInt(args[1]);
      tasks = Integer.parseInt(args[2]);
    }

    // Write to file
    FileWriter w;
    try {
      w = new FileWriter(f);
      w.write(g.generateData(locations, tasks));
      w.close();
  
      // Write info to console
      BufferedReader br = new BufferedReader(new FileReader(f));
      br.lines().forEach(s -> System.out.println(s));
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

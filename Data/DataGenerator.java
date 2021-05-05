package Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DataGenerator {

  // private Map<String, Integer> tradesUsed = new HashMap<>();
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
    12.0, 8.0, 6.0, 10.5, 7.0, 9.0, 6.5, 12.0, 7.0, 9.5};

  public DataGenerator() {}

  /**
   * Opens a dataset file in the given directory. If file is already in place,
   * creates a new file with identical name and counter at the end, i.e. abc_X.csv
   * @param directory
   * @param locations
   * @param tasksPerLocation
   */
  public String generateDataset(String directory, int locations, int tasksPerLocation) {
    File f = new File(String.format("%s/dataset.csv", directory));
    int i = 2;
    try {
      while (!f.createNewFile()) {
        f = new File(String.format("%s/dataset_%d.csv", directory, i));
        i++;
      }

      // Create filewriter and generate data
      FileWriter w = new FileWriter(f);
      w.write(generateTaskData(locations, tasksPerLocation));
      // w.write(generateTradeData());
      w.close();
  
      // Write info to console
      // BufferedReader br = new BufferedReader(new FileReader(f));
      // br.lines().forEach(s -> System.out.println(s));
      // br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return f.getName();
  }

  /**
   * Generates a randomized data set of size n
   * @param n
   */
  public String generateTaskData(int locations, int tasksPerLocation) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s%n", locations));

    for (int i = 0; i < locations; i++) {
      sb.append(createLocation(i));
      
      for (int j = 0; j < tasksPerLocation; j++) {
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
    // tradesUsed.put(trade, tradesUsed.containsKey(trade) ? tradesUsed.get(trade) + 1 : 1);
    int optimalCrew = r.nextInt(5)+1;
    double quantity = quantities[rand];
    int productionRate = Math.min((int)quantity, r.nextInt(5)+1);
    String dependency = "*";
    if (taskId > 0) dependency = String.format("T%d", taskId-1);
    return String.format("T%d;L%d;%s;%s;%d;%2.2f;%d;%s%n", taskId, locationId, activity, trade, optimalCrew, quantity, productionRate, dependency);
  }

  // public String generateTradeData() {
  //   StringBuilder sb = new StringBuilder();
  //   sb.append(String.format("%d%n", tradesUsed.size()));

  //   for (String trade : tradesUsed.keySet()) {
  //     sb.append(String.format("%s;%d%n", trade, tradesUsed.get(trade)));
  //   }
    
  //   return sb.toString();
  // }

  public static void main(String[] args) {
    DataGenerator g = new DataGenerator();
    g.generateDataset("Data/ScheduleData", 5, 5);
  }
}

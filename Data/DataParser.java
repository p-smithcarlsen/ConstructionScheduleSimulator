package Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import ScheduleComponents.Location;
import ScheduleComponents.Task;
import ScheduleComponents.TaskGraph;

public class DataParser {

  private TaskGraph tasks;
  private Location[] locations;
  private int locationSize = 0;
  NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  public DataParser() {}

  public void parseData(String fileName) throws NumberFormatException, IOException {
    File f = new File(String.format("Data/ScheduleData/%s", fileName));
    BufferedReader br = new BufferedReader(new FileReader(f));
    
    String[] locationsAndTasks = br.readLine().split(" ");
    locations = new Location[Integer.parseInt(locationsAndTasks[0])];
    // tasks = new TaskGraph(Integer.parseInt(locationsAndTasks[1]));
    tasks = new TaskGraph();
    String line = br.readLine();
    while (line != null) {
      if (line.startsWith("L")) {
        readLocation(line);
      } else if (line.startsWith("T")) {
        readTask(line);
      } else {
        break;
      }
      line = br.readLine();
    }

    br.close();
  }

  public void addTask(String line) {

  }

  /**
   * 
   * @param line
   */
  public void readLocation(String line) {
    locations[locationSize] = new Location(line.split(";"));
    locationSize++;
  }

  /**
   * 
   * @param line
   */
  public void readTask(String line) {
    Task t = tasks.addTask(line.split(";"));
    locations[locationSize-1].addTask(t);
  }

  public TaskGraph getTasks() {
    return tasks;
  }

  /**
   * 
   * @return
   */
  public Location[] getLocations() {
    return locations;
  }

  /**
   * 
   * @return
   */
  // public Workforce getWorkforce() {
  //   return workforce;
  // }

  public static void main(String[] args) {
    try {
      DataParser p = new DataParser();
      p.parseData("dataset_2.csv");
      Location[] locations = p.getLocations();
      // Workforce workforce = p.getWorkforce();

      Arrays.stream(locations).forEach(l -> l.print());
      System.out.println();
      // workforce.print();
    } catch (NumberFormatException | IOException e) {
      e.printStackTrace();
    }
  }
}

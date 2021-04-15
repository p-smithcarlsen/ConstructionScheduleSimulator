package MikkelPhilipThesis.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DataParser {

  private Location[] locations;
  private int sz = 0;
  NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  public DataParser() {}

  public Location[] parseData(String fileName) throws NumberFormatException, IOException {
    File f = new File(String.format("Data/ScheduleData/%s", fileName));
    BufferedReader br = new BufferedReader(new FileReader(f));
    locations = new Location[Integer.parseInt(br.readLine())];
    br.lines().forEach(s -> processLine(s));
    br.close();

    return locations;
  }

  public void processLine(String line) {
    String[] parameters = line.split(";");
    if (parameters[0].startsWith("L")) {
      locations[sz] = new Location(parameters);
      sz++;
    } else if (parameters[0].startsWith("T")) {
      locations[sz-1].addTask(parameters);
    }
  }

  private class Location {
    String id;
    String name;
    List<Task> tasks = new ArrayList<>();

    public Location(String[] locationParameters) {
      this.id = locationParameters[0];
      this.name = locationParameters[1];
    }

    public void addTask(String[] taskParameters) {
      tasks.add(new Task(taskParameters));
    }

    public void print() {
      System.err.println(String.format("%nLocation %s (%s):", this.id, this.name));
      tasks.stream().forEach(t -> t.print());
    }

    private class Task {
      String id;
      String activity;
      String trade;
      double quantity;
      String[] dependencies;

      public Task(String[] taskParameters) {
        this.id = taskParameters[0];
        this.activity = taskParameters[2];
        this.trade = taskParameters[3];
        taskParameters[4] = taskParameters[4].replaceAll(",", ".");
        this.quantity = Double.parseDouble(taskParameters[4]);
        this.dependencies = taskParameters[5].split(",");
      }

      public void print() {
        System.out.println(String.format(" --Task %s: %s, trade: %s, quantity: %s, dependency: %s", id, activity, trade, quantity, Arrays.deepToString(dependencies)));
      }
    }
  }

  public static void main(String[] args) {
    try {
      DataParser p = new DataParser();
      Location[] locations = p.parseData("dataset_2.csv");
      Arrays.stream(locations).forEach(l -> l.print());
    } catch (NumberFormatException | IOException e) {
      e.printStackTrace();
    }
  }
}

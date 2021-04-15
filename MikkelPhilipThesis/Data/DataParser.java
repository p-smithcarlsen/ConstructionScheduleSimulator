package MikkelPhilipThesis.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import MikkelPhilipThesis.ScheduleComponents.Location;

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

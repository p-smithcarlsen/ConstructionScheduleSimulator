package Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import ScheduleComponents.Location;
import ScheduleComponents.Trade;

public class DataParser {

  private Location[] locations;
  private int locationSize = 0;
  private Trade[] trades;
  private int tradeSize = 0;
  NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  public DataParser() {}

  public void parseData(String fileName) throws NumberFormatException, IOException {
    File f = new File(String.format("Data/ScheduleData/%s", fileName));
    BufferedReader br = new BufferedReader(new FileReader(f));
    
    locations = new Location[Integer.parseInt(br.readLine())];
    String line = br.readLine();
    while (true) {
      if (line.startsWith("L")) {
        readLocation(line);
      } else if (line.startsWith("T")) {
        readTask(line);
      } else {
        break;
      }
      line = br.readLine();
    }
    
    trades = new Trade[Integer.parseInt(line)];
    for (int i = 0; i < trades.length; i++)
      readTrade(br.readLine());

    br.close();
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
    locations[locationSize-1].addTask(line.split(";"));
  }

  /**
   * 
   * @param line
   */
  public void readTrade(String line) {
    trades[tradeSize] = new Trade(line.split(";"));
    tradeSize++;
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
  public Trade[] getTrades() {
    return trades;
  }

  public static void main(String[] args) {
    try {
      DataParser p = new DataParser();
      p.parseData("dataset_2.csv");
      Location[] locations = p.getLocations();
      Trade[] trades = p.getTrades();

      Arrays.stream(locations).forEach(l -> l.print());
      System.out.println();
      Arrays.stream(trades).forEach(t -> t.print());
    } catch (NumberFormatException | IOException e) {
      e.printStackTrace();
    }
  }
}

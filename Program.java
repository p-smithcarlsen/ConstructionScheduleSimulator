import java.io.IOException;
import java.util.Arrays;

import Data.DataParser;
import ScheduleComponents.Location;
import ScheduleComponents.Trade;

public class Program {
  public static void main(String[] args) throws IOException {
    // create parser
    DataParser p = new DataParser();
    p.parseData("dataset_2.csv");
    Location[] locations = p.getLocations();
    Trade[] trades = p.getTrades();

    Arrays.stream(locations).forEach(l -> l.print());
    Arrays.stream(trades).forEach(t -> t.print());
  }

  public static void runSmallSchedule() {

  }

  public static void runMediumSchedule() {

  }

  public static void runLargeSchedule() {
    
  }
}
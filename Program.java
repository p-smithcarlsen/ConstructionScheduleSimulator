package MikkelPhilipThesis;

import java.io.IOException;
import java.util.Arrays;

import MikkelPhilipThesis.Data.DataParser;
import MikkelPhilipThesis.ScheduleComponents.Location;

public class Program {
  public static void main(String[] args) throws IOException {
    // create parser
    DataParser p = new DataParser();
    Location[] locations = p.parseData("dataset_4.csv");
    Arrays.stream(locations).forEach(l -> l.print());
  }

  public static void runSmallSchedule() {

  }
}
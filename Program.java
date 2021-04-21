import java.io.IOException;
import java.util.Arrays;

import Data.DataGenerator;
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
    DataGenerator g = new DataGenerator();
    



        // // Create file
        // DataGenerator g = new DataGenerator();
        // File f = g.createNewFile("Data/ScheduleData");
        
        // int locations, tasks;
        // if (args.length < 2) {
        //   locations = 5;                              // Adjust number of locations
        //   tasks = 5;                                  // Adjust number of tasks
        // } else {
        //   locations = Integer.parseInt(args[1]);
        //   tasks = Integer.parseInt(args[2]);
        // }
    
        // // Write to file
        // FileWriter w;
        // try {
        //   w = new FileWriter(f);
        //   w.write(g.generateTaskData(locations, tasks));
        //   w.write(g.generateTradeData());
        //   w.close();
      
        //   // Write info to console
        //   BufferedReader br = new BufferedReader(new FileReader(f));
        //   br.lines().forEach(s -> System.out.println(s));
        //   br.close();
        // } catch (IOException e) {
        //   e.printStackTrace();
        // }
  }

  public static void runMediumSchedule() {

  }

  public static void runLargeSchedule() {
    
  }
}
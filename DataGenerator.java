import java.io.File;
import java.io.IOException;

import ScheduleComponents.Task;

public class DataGenerator {
  public static void main(String[] args) throws IOException {
    String headers = "Task, Location, Dependencies, Resources, Deadline";
    Task[] tasks = new Task[10];
    File f = new File("ScheduleData/csvfile.csv");
    f.createNewFile();
  }
}

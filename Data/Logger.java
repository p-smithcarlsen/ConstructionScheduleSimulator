package Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ScheduleComponents.Contractor;
import ScheduleComponents.Workforce;

public class Logger {

  private File f;
  private FileWriter w;
  
  public Logger(int fileNumber) throws IOException {
    String path = "Data/Database/simulationLog";
    if (fileNumber > 0) path += "_" + fileNumber;
    f = new File(path);

    if (f.exists()) f.delete();
    f.createNewFile();
    w = new FileWriter(f);
  }

  public void log(Workforce workforce) throws IOException {
    for (Contractor c : workforce.contractors) {
      int workers = 0;
      for (int i : c.workerDemand) { workers += i; }
      w.write(String.format("%s: %d%n", c.trade.toString(), workers));
    }
  }

  public void end() throws IOException {
    w.close();
  }
}

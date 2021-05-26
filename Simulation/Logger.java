package Simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import ScheduleComponents.Alarm;
import ScheduleComponents.Contractor;
import ScheduleComponents.Workforce;
import ScheduleComponents.Contractor.Trade;

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
      w.write(String.format("WorkerCount:%s=%d%n", c.trade.toString(), workers));
    }
  }

  public void log(List<Alarm> alarms) throws IOException {
    for (Alarm a : alarms) {
      w.write(String.format("Delay:Trade=%s,Day=%d%n", a.trade, a.day));
    }
  }

  public void log(Map<Trade, Integer> extraWorkerSupply) throws IOException {
    for (Trade t : extraWorkerSupply.keySet()) {
      w.write(String.format("ExtraWorkers:%s=%d%n", t.toString(), extraWorkerSupply.get(t)));
    }
  }

  public void logScheduledDeadline(int deadline) throws IOException {
    w.write(String.format("ScheduledDeadline:Day=%d%n", deadline));
  }

  public void logProjectEnd(int end) throws IOException {
    w.write(String.format("ProjectEnd:Day=%d%n", end));
  }

  public void end() throws IOException {
    w.close();
  }
}

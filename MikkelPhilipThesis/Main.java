package MikkelPhilipThesis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import MikkelPhilipThesis.Data.DataGenerator;

public class Main {
  public static void main(String[] args) throws IOException {
    // Create file
    DataGenerator g = new DataGenerator();
    File f = g.createNewFile("MikkelPhilipThesis/Data/ScheduleData");

    // Write to file
    FileWriter w = new FileWriter(f);
    int locations = 5;                              // Adjust number of locations
    int tasks = 5;                                  // Adjust number of tasks
    w.write(g.generateData(locations, tasks));
    w.close();

    // Write info to console
    BufferedReader br = new BufferedReader(new FileReader(f));
    br.lines().forEach(s -> System.out.println(s));
    br.close();
  }
}
package MikkelPhilipThesis;

import java.io.IOException;

import MikkelPhilipThesis.Data.DataParser;

public class Program {
  public static void main(String[] args) throws IOException {
    // create parser
    DataParser p = new DataParser("dataset_4");
    Location locations = p.parseData();

  }

  public static void runSmallSchedule() {

  }
}
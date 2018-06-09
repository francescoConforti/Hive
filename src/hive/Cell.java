package hive;

import developmentStages.DevelopingBee;

public class Cell {

  private DevelopingBee resident;
  int nectar;
  
  public Cell(DevelopingBee toPopulate) {
    resident = toPopulate;
    nectar = 0;
  }
  
  public boolean hasResident() {
    return resident != null;
  }
}

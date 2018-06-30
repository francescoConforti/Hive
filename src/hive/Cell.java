package hive;

import developmentStages.DevelopingBee;

public class Cell {

  private DevelopingBee resident;
  boolean capped;
  
  public Cell(DevelopingBee toPopulate) {
    resident = toPopulate;
    capped = false;
  }
  
  public boolean hasResident() {
    return resident != null;
  }
  
  public DevelopingBee getResident() {
    return resident;
  }
  
  public void setResident(DevelopingBee newResident) {
    resident = newResident;
  }
}

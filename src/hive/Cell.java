package hive;

import developmentStages.DevelopingBee;

public class Cell {

  private DevelopingBee resident;
  boolean capped;
  boolean clean;
  
  public Cell(DevelopingBee toPopulate) {
    resident = toPopulate;
    capped = false;
    clean = true;
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
  
  public boolean isCapped() {
    return capped;
  }
  
  public void setCapped(boolean b) {
    capped = b;
  }
  
  public boolean isClean() {
    return clean;
  }
  
  public void setClean(boolean b) {
    clean = b;
  }
}

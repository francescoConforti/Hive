package hive;

import java.util.ArrayList;
import java.util.List;

import commons.DFAConstants;
import developmentStages.DevelopingBee;
import developmentStages.Egg;
import developmentStages.Larva;
import developmentStages.Pupa;

public class Hive {
  
  private int MAX_NECTAR;
  private int MAX_POLLEN;
  private int MAX_CELLS;
  
  private int food;
  private int materials;
  private List<Cell> workerCells;
  private List<Cell> droneCells;
  private Cell queenCell;
  
  public Hive(int maxNectar, int maxPollen, int maxCells) {
    MAX_NECTAR = maxNectar;
    MAX_POLLEN = maxPollen;
    MAX_CELLS = maxCells;
    workerCells = new ArrayList<>();
    droneCells = new ArrayList<>();
    queenCell = new Cell(null);
  }
  
  public int getMaxNectar() {
    return MAX_NECTAR;
  }
  
  public int getMaxPollen() {
    return MAX_POLLEN;
  }
  
  public int getMaxCells() {
    return MAX_CELLS;
  }
  
  public List<Cell> getDroneCells(){
    return droneCells;
  }
  
  public List<Cell> getWorkerCells(){
    return workerCells;
  }
  
  public boolean hasFreeCells() {
    return workerCells.size() + droneCells.size() < MAX_CELLS;
  }
  
  /*
   * An egg is always laid in a new Cell
   */
  public void addWorkerCell() {
    workerCells.add(new Cell(new Egg()));
  }
  
  public void addDroneCell() {
    droneCells.add(new Cell(new Egg()));
  }
  
  public void updateCells() {
    for(Cell c : workerCells) {
      updateCell(c, "worker");
    }
    for(Cell c : droneCells) {
      updateCell(c, "drone");
    }
    updateCell(queenCell, "queen");
  }
  
  private void updateCell(Cell cell, String type) {
    DevelopingBee db = null;
    if(cell.hasResident()) {
      db = cell.getResident();
      db.increaseAge();
      if(db.getAge() == db.getMaxAge()) { // Change phase
        if(db instanceof Egg) {
          switch(type) {
          case "worker":
            cell.setResident(new Larva(9, DFAConstants.WORKER_LARVA_DAILY_FOOD));
            break;
          case "drone":
            cell.setResident(new Larva(9, DFAConstants.DRONE_LARVA_DAILY_FOOD));
            break;
          case "queen":
            cell.setResident(new Larva(8, DFAConstants.QUEEN_LARVA_DAILY_FOOD));
            break;
          }
          System.out.println("An egg hatched into a larva");
        }
        else if (db instanceof Larva) {
          switch(type) {
          case "worker":
            cell.setResident(new Pupa(11));
            break;
          case "drone":
            cell.setResident(new Pupa(14));
            break;
          case "queen":
            cell.setResident(new Pupa(8));
            break;
          }
          System.out.println("A larva became a pupa");
        }
        else {
          // TODO: create new agent and destroy cell
        }
      }
    }
  }
  
}

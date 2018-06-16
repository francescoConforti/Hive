package hive;

import java.util.ArrayList;
import java.util.List;

import developmentStages.Egg;

public class Hive {
  
  private int MAX_NECTAR;
  private int MAX_POLLEN;
  private int MAX_CELLS;
  
  private int nectar;
  private int pollen;
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
  
  /*
  public int getMaxCells() {
    return MAX_CELLS;
  }
  */
  
  public boolean hasFreeCells() {
    return workerCells.size() + droneCells.size() < MAX_CELLS;
  }
  
  public void addWorkerCell() {
    workerCells.add(new Cell(new Egg()));
  }
  
  public void addDroneCell() {
    droneCells.add(new Cell(new Egg()));
  }
  
}

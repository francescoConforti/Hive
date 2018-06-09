package hive;

import java.util.ArrayList;
import java.util.List;

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
  
}

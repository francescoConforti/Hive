package hive;

import java.util.ArrayList;
import java.util.List;

import commons.DFAConstants;
import developmentStages.DevelopingBee;
import developmentStages.Egg;
import developmentStages.Larva;
import developmentStages.Pupa;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Hive {
  
  private final int MAX_FOOD;
  private final int MAX_MATERIALS;
  private final int MAX_CELLS;
  
  private int food;
  private int materials;
  private List<Cell> workerCells;
  private List<Cell> droneCells;
  private Cell queenCell;
  private int workerNum;
  private int droneNum;
  private int queenNum;
  
  public Hive(int maxFood, int maxMaterials, int maxCells) {
    MAX_FOOD = maxFood;
    MAX_MATERIALS = maxMaterials;
    MAX_CELLS = maxCells;
    workerCells = new ArrayList<>();
    droneCells = new ArrayList<>();
    queenCell = new Cell(null);
    workerNum = 1;
    droneNum = 1;
    queenNum = 1;
  }
  
  public int getMaxNectar() {
    return MAX_FOOD;
  }
  
  public int getMaxPollen() {
    return MAX_MATERIALS;
  }
  
  public int getMaxCells() {
    return MAX_CELLS;
  }
  
  public boolean canGetMoreFood() {
    return food < MAX_FOOD;
  }
  
  public boolean canGetMoreMaterials() {
    return materials < MAX_MATERIALS;
  }
  
  public void increaseFood() {
    ++food;
  }
  
  public void increaseMaterials() {
    ++materials;
  }
  
  public boolean hasFreeCells() {
    return workerCells.size() + droneCells.size() < MAX_CELLS;
  }
  
  public List<Cell> getDroneCells(){
    return droneCells;
  }
  
  public List<Cell> getWorkerCells(){
    return workerCells;
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
  
  public void updateCells(ContainerController cc) {
    for(Cell c : workerCells) {
      updateCell(c, "worker", cc);
    }
    for(Cell c : droneCells) {
      updateCell(c, "drone", cc);
    }
    updateCell(queenCell, "queen", cc);
  }
  
  private void updateCell(Cell cell, String type, ContainerController cc){
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
          AgentController ac = null;
          switch(type) {
          case "worker":
            workerCells.remove(cell);
            try {
              ac = cc.createNewAgent("worker" + workerNum, "worker.Worker", null);
              ++workerNum;
            } catch (StaleProxyException e) {
              e.printStackTrace();
            }
            break;
          case "drone":
            droneCells.remove(cell);  //  TODO: can't remove cell, change implementation
            try {
              ac = cc.createNewAgent("drone" + droneNum, "drone.Drone", null);
              ++droneNum;
            } catch (StaleProxyException e) {
              e.printStackTrace();
            }
            break;
          case "queen":
            queenCell = null;
            try {
              ac = cc.createNewAgent("queen" + queenNum, "queen.Queen", null);
              ++queenNum;
            } catch (StaleProxyException e) {
              e.printStackTrace();
            }
            break;
          }
          try {
            ac.start();
          } catch (StaleProxyException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
  
}

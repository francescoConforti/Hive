package hive;

import java.util.ArrayList;
import java.util.Iterator;
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
  
  public synchronized boolean canGetMoreFood() {
    return food < MAX_FOOD;
  }
  
  public synchronized boolean canGetMoreMaterials() {
    return materials < MAX_MATERIALS;
  }
  
  public synchronized void increaseFood() {
    ++food;
  }
  
  public synchronized void increaseMaterials() {
    ++materials;
  }
  
  public synchronized boolean hasFreeCells() {
    boolean freeWorker = false, freeDrone = false;
    for (Iterator<Cell> iter = droneCells.iterator(); iter.hasNext() && !freeWorker; ) {
      Cell c = iter.next();
      if(!c.hasResident() && c.isClean()) {
        c.setResident(new Egg());
        freeWorker = true;
      }
    }
    for (Iterator<Cell> iter = droneCells.iterator(); iter.hasNext() && !freeDrone; ) {
      Cell c = iter.next();
      if(!c.hasResident() && c.isClean()) {
        freeDrone = true;
      }
    }
    return ((freeDrone && freeWorker) || (workerCells.size() + droneCells.size() < MAX_CELLS));
  }
  
  public synchronized void addWorkerCell() {
    boolean done = false;
    for (Iterator<Cell> iter = workerCells.iterator(); iter.hasNext() && !done; ) {
      Cell c = iter.next();
      if(!c.hasResident() && c.isClean()) {
        c.setResident(new Egg());
        done = true;
      }
    }
    if(!done) {
      workerCells.add(new Cell(new Egg()));
    }
  }
  
  public synchronized void addDroneCell() {
    boolean done = false;
    for (Iterator<Cell> iter = droneCells.iterator(); iter.hasNext() && !done; ) {
      Cell c = iter.next();
      if(!c.hasResident() && c.isClean()) {
        c.setResident(new Egg());
        done = true;
      }
    }
    if(!done) {
      droneCells.add(new Cell(new Egg()));
    }
  }
  
  public synchronized void updateCells(ContainerController cc) {
    for(Cell c : workerCells) {
      updateCell(c, "worker", cc);
    }
    for(Cell c : droneCells) {
      updateCell(c, "drone", cc);
    }
    updateCell(queenCell, "queen", cc);
  }
  
  private synchronized void updateCell(Cell cell, String type, ContainerController cc){
    DevelopingBee db = null;
    if(cell.hasResident()) {
      db = cell.getResident();
      db.increaseAge();
      if(db.getAge() == db.getMaxAge()) { // Change phase
        if(db instanceof Egg) {
          switch(type) {
          case "worker":
            cell.setResident(new Larva(9, DFAConstants.WORKER_LARVA_MAX_FOOD));
            break;
          case "drone":
            cell.setResident(new Larva(9, DFAConstants.DRONE_LARVA_MAX_FOOD));
            break;
          case "queen":
            cell.setResident(new Larva(8, DFAConstants.QUEEN_LARVA_MAX_FOOD));
            break;
          }
          System.out.println("An egg hatched into a larva");
        }
        else if (db instanceof Larva) {
          if(((Larva)db).getFood() == ((Larva)db).getMaxFood() && cell.isCapped()) {  // Larva can become a pupa
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
          else {  // Larva dies
            cell.setClean(false);
            cell.setResident(null);
            System.out.println("A larva died");
          }
        }
        else {
          AgentController ac = null;
          switch(type) {
          case "worker":
            try {
              ac = cc.createNewAgent("worker" + workerNum, "worker.Worker", null);
              ++workerNum;
            } catch (StaleProxyException e) {
              e.printStackTrace();
            }
            break;
          case "drone":
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
          cell.setClean(false);
          cell.setCapped(false);
          try {
            ac.start();
          } catch (StaleProxyException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  public boolean feedWorker() {
    return doFeed(workerCells);
  }

  public boolean feedDrone() {
    return doFeed(droneCells);
  }
  
  private boolean doFeed(List<Cell> list) {
    boolean feedingDone = false;
    for (Iterator<Cell> iter = list.iterator(); iter.hasNext() && !feedingDone; ) {
      Cell cell = iter.next();
      if(cell.hasResident()) {
        DevelopingBee resident = cell.getResident();
        if(resident instanceof Larva) {
          Larva larva = (Larva) resident;
          if(larva.getFood() < larva.getMaxFood()) {
            larva.feed();
            feedingDone = true;
          }
        }
      }
    }
    return feedingDone;
  }
  
}

package drone;

import jade.core.Agent;

public class Drone extends Agent{

  private static final long serialVersionUID = -1774514423272260611L;

  @Override
  protected void setup() {
    System.out.println("Drone " + getAID().getLocalName() + " ready");
    
    addBehaviour(new MatingBehaviour());
  }
  
  @Override
  protected void takeDown() {
    System.out.println("Drone " + getAID().getLocalName() + " dead");
  }
}

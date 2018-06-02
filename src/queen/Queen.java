package queen;

import commons.DFAConstants;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Queen extends Agent{

  private static final long serialVersionUID = 2365835675346077157L;
  
  private int MAX_SPERM = 15;
  
  private int spermatheca;
  
  @Override
  protected void setup() {
    spermatheca = 0;
    System.out.println("Queen "  + getAID().getLocalName() + " ready");
    
    // Advertise as ready for mating in DF
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setType(DFAConstants.MATING);
    sd.setName("Mating_ready_queen");
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    } catch (FIPAException e) {
      e.printStackTrace();
    }
    
    addBehaviour(new SpermGatheringBehaviour());
  }

  @Override
  protected void takeDown() {
    
    System.out.println("Queen"  + getLocalName() + " dead");
    
    // TODO: announce need for new queen?
  }
  
  /*
  protected int getMaxSperm() {
    return MAX_SPERM;
  }
  
  protected int getSperm() {
    return spermatheca;
  }
  
  protected void increaseSperm() {
    ++spermatheca;
  }
  
  protected void decreaseSperm() {
    --spermatheca;
  }
  */
  
  private class SpermGatheringBehaviour extends Behaviour {

    private static final long serialVersionUID = -744558547993671025L;
    
    private int step = 0;

    @Override
    public void action() {
      switch(step) {
        case 0:
          //Accept mating requests
          
      }
    }

    @Override
    public boolean done() {
      return ((Queen)myAgent).spermatheca == MAX_SPERM;
    }
    
    @Override
    public int onEnd() {
      // Deregister from DF (mating)
      try {
        DFService.deregister(myAgent);
      } catch (FIPAException e) {
        e.printStackTrace();
      }
      
      // TODO: add next behaviour
      return 0;
    }

  }

}

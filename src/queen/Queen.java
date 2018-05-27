package queen;

import commons.DFAConstants;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Queen extends Agent{

  private static final long serialVersionUID = 2365835675346077157L;
  
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
  }

  @Override
  protected void takeDown() {
    // Deregister from DF
    try {
      DFService.deregister(this);
    } catch (FIPAException e) {
      e.printStackTrace();
    }
    
    System.out.println("Queen"  + getAID().getLocalName() + " dead");
    
    // TODO: announce need for new queen?
  }
}

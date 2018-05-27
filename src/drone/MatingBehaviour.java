package drone;

import commons.DFAConstants;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class MatingBehaviour extends OneShotBehaviour{

  private static final long serialVersionUID = 1473331424316557620L;
  
  private int QUEEN_SEARCH_TIMEOUT = 5000; // millis
  
  private AID[] queens;

  @Override
  public void action() {
    
    boolean matingDone = false;
    
    // Search queens in DF
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType(DFAConstants.MATING);
    template.addServices(sd);
    try {
      DFAgentDescription[] result = DFService.searchUntilFound(myAgent, myAgent.getDefaultDF(),
          template, null, QUEEN_SEARCH_TIMEOUT);
      if(result != null) {
        queens = new AID[result.length];
        System.out.println("Drone " + myAgent.getAID().getLocalName() + ": found " + queens.length + " queen(s)");
        for(int i = 0; i < result.length; i++) {
          queens[i] = result[i].getName();
        }
      }
    } catch (FIPAException e) {
      e.printStackTrace();
    }
    
    // Approach the queens in order, if one was found
    if(queens != null) {
      for(int i = 0; i < queens.length && !matingDone; i++) {
        // TODO: arrange mating
      }
    }
    
    myAgent.doDelete();
  }

}

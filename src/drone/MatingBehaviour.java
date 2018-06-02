package drone;

import commons.DFAConstants;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MatingBehaviour extends CyclicBehaviour{

  private static final long serialVersionUID = 1473331424316557620L;
  
  private int QUEEN_SEARCH_TIMEOUT = 5000; // millis
  
  private AID[] queens;
  private int step = 0;
  private MessageTemplate mt;

  @Override
  public void action() {
    
    switch(step) {
      case 0:
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

        if(queens != null) {
          ++step;
        }
        break;

      case 1:
        // Send mating request to first queen found
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(queens[0]);
        request.setContent(myAgent.getLocalName());
        request.setConversationId(DFAConstants.MATING_REQUEST);
        request.setReplyWith(DFAConstants.MATING_REQUEST + System.currentTimeMillis());
        myAgent.send(request);
        
        // Prepare template to receive response
        mt = MessageTemplate.and(MessageTemplate.MatchConversationId(DFAConstants.MATING_REQUEST),
                                 MessageTemplate.MatchInReplyTo(request.getReplyWith()));
        ++step;
        break;
        
      case 2:
        // Receive mating response
        ACLMessage reply = myAgent.receive(mt);
        if(reply != null) {
          if(reply.getPerformative() == ACLMessage.INFORM) {
            // mating done
            System.out.println("Drone " + myAgent.getLocalName() + 
                " mated with queen " + queens[0].getLocalName());
            myAgent.doDelete();
          }
          else {
            // Try again from the beginning
            step = 0;
          }
        }
        else {
          block();
        }
        break;
    }
  }

}

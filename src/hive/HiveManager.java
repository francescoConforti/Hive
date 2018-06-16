package hive;

import commons.DFAConstants;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class HiveManager extends Agent{

  private static final long serialVersionUID = -7097029207715182632L;
  
  private Hive hive;
  
  @Override
  protected void setup() {
    // Initialize hive structure
    int maxNectar = 100, maxPollen = 100, maxCells = 100;
    Object[] args = getArguments();
    if(args != null) {
      if(args.length >= 1) {
        maxNectar = (Integer)args[0];
      }
      if(args.length >= 2) {
        maxPollen = (Integer)args[1];
      }
      if(args.length >= 3) {
        maxCells = (Integer)args[2];
      }
    }
    hive = new Hive(maxNectar, maxPollen, maxCells);
    System.out.println("Hive initialized");
    
    addBehaviour(new EggReceiveBehaviour(this, DFAConstants.EGG_LAY_OR_RECEIVE_TIMER));
    // TODO
    //addBehaviour(new ResourceReceiveBehaviour());
    //addBehaviour(new AgingBehaviour()); // For cell resident developement
  }

  /*
   * This behaviour is used to receive eggs frome the queen and store
   * the in the hive structure
   */
  private class EggReceiveBehaviour extends TickerBehaviour{

    private static final long serialVersionUID = -8997043740546052765L;

    public EggReceiveBehaviour(Agent a, long period) {
      super(a, period);
      
      // Advertise hive ready to receive eggs in DF
      DFAgentDescription dfd = new DFAgentDescription();
      dfd.setName(getAID());
      ServiceDescription sd = new ServiceDescription();
      sd.setType(DFAConstants.EGG);
      sd.setName("Hive ready for eggs");
      dfd.addServices(sd);
      try {
        DFService.register(myAgent, dfd);
      } catch (FIPAException e) {
        e.printStackTrace();
      }
    }

    @Override
    protected void onTick() {
      String content = "";
      MessageTemplate mt = MessageTemplate.MatchConversationId(DFAConstants.LAY_EGG);
      ACLMessage proposal = myAgent.receive(mt);   
      if(proposal != null) {
        if(proposal.getPerformative() == ACLMessage.REQUEST) {
          if(((HiveManager)myAgent).hive.hasFreeCells()) {
            // Set egg and respond
            if(proposal.getContent().equals(DFAConstants.WORKER_EGG)) {
              ((HiveManager)myAgent).hive.addWorkerCell();
              content = DFAConstants.WORKER_EGG;
            }
            if(proposal.getContent().equals(DFAConstants.DRONE_EGG)) {
              ((HiveManager)myAgent).hive.addDroneCell();
              content = DFAConstants.DRONE_EGG;
            }
            if(proposal.getContent().equals(DFAConstants.QUEEN_EGG)) {
              // TODO
            }
            ACLMessage reply = proposal.createReply();
            reply.setPerformative(ACLMessage.CONFIRM);
            reply.setContent(content);
            myAgent.send(reply);
          }
          else {
            // Communicate refusal
            ACLMessage reply = proposal.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent(myAgent.getLocalName());
            myAgent.send(reply);
          }
        }
      }
      else {
        block();
      }
      
    }
    
  }
}

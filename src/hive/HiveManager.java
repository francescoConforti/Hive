package hive;

import commons.DFAConstants;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
    int maxFood = 100, maxMaterials = 100, maxCells = 100;
    Object[] args = getArguments();
    if(args != null) {
      if(args.length >= 1) {
        maxFood = (Integer)args[0];
      }
      if(args.length >= 2) {
        maxMaterials = (Integer)args[1];
      }
      if(args.length >= 3) {
        maxCells = (Integer)args[2];
      }
    }
    hive = new Hive(maxFood, maxMaterials, maxCells);
    System.out.println("Hive initialized");
    
    // Advertise hive services in DF
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    // Advertise hive ready to receive resources
    ServiceDescription sd = new ServiceDescription();
    sd.setType(DFAConstants.GATHERING);
    sd.setName("Hive ready for resources");
    dfd.addServices(sd);
    // Advertise hive ready to receive eggs in DF
    sd = new ServiceDescription();
    sd.setType(DFAConstants.EGG);
    sd.setName("Hive ready for eggs");
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    } catch (FIPAException e) {
      e.printStackTrace();
    }
  
    
    addBehaviour(new EggReceiveBehaviour(this, DFAConstants.EGG_LAY_OR_RECEIVE_TIMER));
    addBehaviour(new ResourceReceiveBehaviour());
    addBehaviour(new AgingBehaviour(this, DFAConstants.DAY_IN_MILLIS)); // For cell resident developement
  }
  /*
   * This behaviour receives resources from workers and sends them to the hive
   */
  private class ResourceReceiveBehaviour extends CyclicBehaviour{

    private static final long serialVersionUID = -863596333338272115L;
    
    @Override
    public void action() {
      String content = "";
      MessageTemplate mt = MessageTemplate.MatchConversationId(DFAConstants.RESOURCE_EXCHANGE);
      ACLMessage proposal = myAgent.receive(mt);   
      if(proposal != null) {
        if(proposal.getPerformative() == ACLMessage.REQUEST) {
          if(proposal.getContent().equals(DFAConstants.FOOD_EXCHANGE)) {
            if(((HiveManager)myAgent).hive.canGetMoreFood()) {
              ((HiveManager)myAgent).hive.increaseFood();
              content = DFAConstants.FOOD_EXCHANGE;
            }
          }
          else if(proposal.getContent().equals(DFAConstants.MATERIALS_EXCHANGE)) {
            if(((HiveManager)myAgent).hive.canGetMoreMaterials()) {
              ((HiveManager)myAgent).hive.increaseMaterials();
              content = DFAConstants.MATERIALS_EXCHANGE;
            }
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
      else {
        block();
      }
    }
    
    
    
  }
  
  /*
   * This behaviour is used to receive eggs frome the queen and store
   * them in the hive structure
   */
  private class EggReceiveBehaviour extends TickerBehaviour{

    private static final long serialVersionUID = -8997043740546052765L;

    public EggReceiveBehaviour(Agent a, long period) {
      super(a, period);
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
              // TODO: queen egg
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

  /**
   *  This behaviour ages the eggs, larvae and pupas in the cells
   *  and manages their developement
   */
  private class AgingBehaviour extends TickerBehaviour{

    private static final long serialVersionUID = -3860859943421646102L;

    public AgingBehaviour(Agent a, long period) {
      super(a, period);
    }

    @Override
    protected void onTick() {
      ((HiveManager)myAgent).hive.updateCells(getContainerController());
    }
    
  }
}

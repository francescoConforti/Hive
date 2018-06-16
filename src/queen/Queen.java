package queen;

import java.util.Random;

import commons.DFAConstants;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Queen extends Agent{

  private static final long serialVersionUID = 2365835675346077157L;
  
  private int MAX_SPERM = 15;
  
  private int spermatheca;
  
  @Override
  protected void setup() {
    spermatheca = 0;
    System.out.println("Queen " + getAID().getLocalName() + " ready");
    addBehaviour(new SpermGatheringBehaviour(this));
  }

  @Override
  protected void takeDown() {
    
    System.out.println("Queen "  + getLocalName() + " dead");
    
    // TODO: announce need for new queen?
  }
  
  /*
  protected int getMaxSperm() {
    return MAX_SPERM;
  }
  
  protected int getSperm() {
    return spermatheca;
  }
  */
  
  protected void increaseSperm() {
    ++spermatheca;
  }
  
  protected void decreaseSperm() {
    --spermatheca;
  }
  
  private class SpermGatheringBehaviour extends Behaviour {

    private static final long serialVersionUID = -744558547993671025L;
    
    private int step = 0;
    
    public SpermGatheringBehaviour(Agent a) {
      super(a);
      // Advertise as ready for mating in DF
      DFAgentDescription dfd = new DFAgentDescription();
      dfd.setName(getAID());
      ServiceDescription sd = new ServiceDescription();
      sd.setType(DFAConstants.MATING);
      sd.setName("Mating_ready_queen");
      dfd.addServices(sd);
      try {
        DFService.register(myAgent, dfd);
      } catch (FIPAException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void action() {
      switch(step) {
        case 0:
          //Accept mating requests
          MessageTemplate mt = MessageTemplate.MatchConversationId(DFAConstants.MATING_REQUEST);
          ACLMessage proposal = myAgent.receive(mt);
          if(proposal != null) {
            if(proposal.getPerformative() == ACLMessage.REQUEST) {
              ACLMessage reply = proposal.createReply();
              reply.setPerformative(ACLMessage.INFORM);
              reply.setContent(myAgent.getLocalName());
              myAgent.send(reply);
              ((Queen)myAgent).increaseSperm();
            }
          }
          else {
            block();
          }
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
      
      System.out.println("Queen " + myAgent.getLocalName() + " is done with mating");
      myAgent.addBehaviour(new EggLayingBehaviour(myAgent));
      return 0;
    }

  }
  
  /*
   * Behaviour the queen uses for laying eggs
   * egg laying request is sent to the hive where eggs are stored
   * when spermatheca is empty stop this behaviour and restart mating
   */
  private class EggLayingBehaviour extends Behaviour{

    private static final long serialVersionUID = 3011923277493937678L;
    private final int HIVE_SEARCH_TIMEOUT = 30000;
    private final int HIVE_FULL_TIMEOUT = 120000;
    
    private AID hive;
    private int step;
    private Random rand;
    private MessageTemplate mt;

    public EggLayingBehaviour(Agent a) {
      super(a);
      step = 0;
      rand = new Random();
    }
    
    @Override
    public void action() {
      switch(step) {
        case 0: // Search hive in DF
          AID[] hives;
          DFAgentDescription template = new DFAgentDescription();
          ServiceDescription sd = new ServiceDescription();
          sd.setType(DFAConstants.EGG);
          template.addServices(sd);
          try {
            DFAgentDescription[] result = DFService.searchUntilFound(myAgent, myAgent.getDefaultDF(),
                template, null, HIVE_SEARCH_TIMEOUT);
            if(result != null) {
              hives = new AID[result.length];
              System.out.println("Queen " + myAgent.getAID().getLocalName() + ": found " + hives.length + " hive(s)");
              for(int i = 0; i < result.length; i++) {
                hives[i] = result[i].getName();
              }
              hive = hives[0];
              ++step;
            }
          } catch (FIPAException e) {
            e.printStackTrace();
          }
          break;
        
        case 1:  // Lay egg
          boolean isDrone;
          ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
          request.addReceiver(hive);
          request.setContent(myAgent.getLocalName());
          request.setConversationId(DFAConstants.LAY_EGG);
          request.setReplyWith(DFAConstants.LAY_EGG + System.currentTimeMillis());
          isDrone = rand.nextInt(((Queen)myAgent).MAX_SPERM) == 0;
          if(isDrone) {
            request.setContent(DFAConstants.DRONE_EGG);
          }
          else {
            request.setContent(DFAConstants.WORKER_EGG);
          }
          myAgent.send(request);
          
          // Prepare template to receive response
          mt = MessageTemplate.and(MessageTemplate.MatchConversationId(DFAConstants.LAY_EGG),
                                   MessageTemplate.MatchInReplyTo(request.getReplyWith()));
          step = 2;
          break;
          
        case 2: // Receive hive response
          ACLMessage reply = myAgent.receive(mt);
          if(reply != null) {
            if(reply.getPerformative() == ACLMessage.CONFIRM) {
              if(reply.getContent().equals(DFAConstants.WORKER_EGG)) {
                System.out.println("Queen " + getLocalName() + " laid a worker egg");
                ((Queen)myAgent).decreaseSperm();
              }
              else if(reply.getContent().equals(DFAConstants.DRONE_EGG)) {
                System.out.println("Queen " + getLocalName() + " laid a drone egg");
                // drone eggs are not fertilized
              }
            }
            else if(reply.getPerformative() == ACLMessage.REFUSE) {
              // wait some time
              try {
                Thread.sleep(HIVE_FULL_TIMEOUT);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            // Lay another egg
            step = 1;
          }
          else {
            block();
          }
          break;
      }
    }
    
    @Override
    public boolean done() {
      return ((Queen)myAgent).spermatheca == 0;
    }
    
    @Override
    public int onEnd() {
      System.out.println("Queen " + myAgent.getLocalName() + " is done with egg laying");
      myAgent.addBehaviour(new SpermGatheringBehaviour(myAgent));
      return 0;
    }

  }

}

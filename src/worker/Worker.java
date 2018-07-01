package worker;

import java.util.Random;

import commons.DFAConstants;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Worker extends Agent{

  private static final long serialVersionUID = -5162119966888825259L;

  private final int MAX_AGE = 42;
  
  private int age;
  
  /*
   * (non-Javadoc)
   * @see jade.core.Agent#setup()
   * In reality a worker bee changes roles depending on its age.
   * For semplicity all workers are the same in this project
   */
  protected void setup() {
    age = 0;
    addBehaviour(new AgingBehaviour(this, DFAConstants.DAY_IN_MILLIS));
    addBehaviour(new GatheringBehaviour(this));
  }
  
  // Periodically ages the bee
  private class AgingBehaviour extends TickerBehaviour{

    private static final long serialVersionUID = 7674421941132224978L;

    public AgingBehaviour(Agent a, long period) {
      super(a, period);
    }

    @Override
    protected void onTick() {
      ++(((Worker)myAgent).age);  // increase age
      if(((Worker)myAgent).age == ((Worker)myAgent).MAX_AGE) {
        myAgent.doDelete(); // Die when too old
        System.out.println("Worker " + myAgent.getAID() + " dead");
      }
    }
    
  }

  /*
   * Gather food or materials and store them in the hive
   */
  private class GatheringBehaviour extends CyclicBehaviour{

    private static final long serialVersionUID = 5527774996031187135L;
    
    private final int HIVE_FULL_TIMEOUT = 120000;
    
    private AID hive;
    private Random rand = new Random();
    private int step = 0;
    private MessageTemplate mt;
    
    public GatheringBehaviour(Agent a) {
      super(a);
    }

    @Override
    public void action() {
      switch(step) {
        case 0: // Search hive in DF
          AID[] hives;
          DFAgentDescription template = new DFAgentDescription();
          ServiceDescription sd = new ServiceDescription();
          sd.setType(DFAConstants.GATHERING);
          template.addServices(sd);
          try {
            DFAgentDescription[] result = DFService.search(myAgent, template);;
            if(result != null && result.length > 0) {
              hives = new AID[result.length];
              System.out.println("Worker " + myAgent.getAID().getLocalName() + ": found " + hives.length + " hive(s)");
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
        
        case 1:  // send resource
          boolean isFood = rand.nextBoolean();
          ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
          request.addReceiver(hive);
          request.setConversationId(DFAConstants.RESOURCE_EXCHANGE);
          request.setReplyWith(DFAConstants.FOOD_EXCHANGE + System.currentTimeMillis());
          if(isFood) {
            request.setReplyWith(DFAConstants.FOOD_EXCHANGE + System.currentTimeMillis());
            request.setContent(DFAConstants.FOOD_EXCHANGE);

          }
          else {
            request.setReplyWith(DFAConstants.MATERIALS_EXCHANGE + System.currentTimeMillis());
            request.setContent(DFAConstants.MATERIALS_EXCHANGE);
          }
          // Prepare template to receive response
          mt = MessageTemplate.and(MessageTemplate.MatchConversationId(DFAConstants.RESOURCE_EXCHANGE),
              MessageTemplate.MatchInReplyTo(request.getReplyWith()));
          myAgent.send(request);
          step = 2;
          break;
          
        case 2: // Receive hive response
          ACLMessage reply = myAgent.receive(mt);
          if(reply != null) {
            if(reply.getPerformative() == ACLMessage.CONFIRM) {
              System.out.println("wORKER " + getLocalName() + " succesfully sent materials to hive");
            }
            else if(reply.getPerformative() == ACLMessage.REFUSE) {
              // wait some time
              try {
                Thread.sleep(HIVE_FULL_TIMEOUT);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            // Restart gathering
            step = 0;
          }
          else {
            block();
          }
          break;
      }
    }
    
  }
}

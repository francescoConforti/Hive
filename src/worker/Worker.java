package worker;

import java.util.Random;

import commons.DFAConstants;
import jade.core.AID;
import jade.core.Agent;
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
    addBehaviour(new GatheringBehaviour(this, DFAConstants.GATHERING_TIMER));
    addBehaviour(new FeedingBehaviour(this, DFAConstants.FEEDING_TIMER));
    addBehaviour(new BuildingBehaviour(this, DFAConstants.BUILDING_TIMER));
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
        System.out.println("Worker " + myAgent.getLocalName() + " dead");
      }
    }
    
  }

  /*
   * Gather food or materials and store them in the hive
   */
  private class GatheringBehaviour extends TickerBehaviour{

    private static final long serialVersionUID = 5527774996031187135L;
    
    private AID hive;
    private Random rand = new Random();
    private int step = 0;
    private MessageTemplate mt;
    
    public GatheringBehaviour(Agent a, long period) {
      super(a, period);
    }

    @Override
    protected void onTick() {
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
            System.out.println("worker " + getLocalName() + " succesfully sent item to hive");
          }
          else if(reply.getPerformative() == ACLMessage.REFUSE) {
            System.out.println("worker " + getLocalName() + " was unable to send items to hive");
            try {
              Thread.sleep(DFAConstants.DAY_IN_MILLIS);
            } catch (InterruptedException e) {
              doDelete(); // This happens when closing JADE
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

  /**
   *  Feed larvae in hive
   */
  private class FeedingBehaviour extends TickerBehaviour{

    private static final long serialVersionUID = 5527884996031187135L;
    
    private AID hive;
    private Random rand = new Random();
    private int step = 0;
    private MessageTemplate mt;
    
    public FeedingBehaviour(Agent a, long period) {
      super(a, period);
    }

    @Override
    protected void onTick() {
      switch(step) {
      case 0: // Search hive in DF
        AID[] hives;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(DFAConstants.FEEDING);
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
      
      case 1:  // feed
        boolean isWorker = rand.nextBoolean();
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(hive);
        request.setConversationId(DFAConstants.FEED_ACTIVITY);
        request.setReplyWith(DFAConstants.FEED_ACTIVITY + System.currentTimeMillis());
        request.setReplyWith(DFAConstants.FEED_ACTIVITY + System.currentTimeMillis());
        // TODO: feed queen
        if(isWorker) {
          request.setContent(DFAConstants.FEED_WORKER);
        }
        else {
          request.setContent(DFAConstants.FEED_DRONE);
        }
        // Prepare template to receive response
        mt = MessageTemplate.and(MessageTemplate.MatchConversationId(DFAConstants.FEED_ACTIVITY),
            MessageTemplate.MatchInReplyTo(request.getReplyWith()));
        myAgent.send(request);
        step = 2;
        break;
        
      case 2: // Receive hive response
        ACLMessage reply = myAgent.receive(mt);
        if(reply != null) {
          if(reply.getPerformative() == ACLMessage.CONFIRM) {
            System.out.println("worker " + getLocalName() + " succesfully fed larva");
          }
          else if(reply.getPerformative() == ACLMessage.REFUSE) {
            System.out.println("worker " + getLocalName() + " was unable to perform feeding");
            try {
              Thread.sleep(DFAConstants.DAY_IN_MILLIS);
            } catch (InterruptedException e) {
              doDelete(); // This happens when closing JADE
            }
          }
          // Restart feeding
          step = 0;
        }
        else {
          block();
        }
        break;
    }
    }
    
  }

  /**
   *  This behaviour is used to cap larvae cells and expand hive
   */
  private class BuildingBehaviour extends TickerBehaviour{

    private static final long serialVersionUID = 5527774996531187135L;
    
    private AID hive;
    private int step = 0;
    private MessageTemplate mt;
    
    public BuildingBehaviour(Agent a, long period) {
      super(a, period);
    }

    @Override
    protected void onTick() {
      switch(step) {
      case 0: // Search hive in DF
        AID[] hives;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(DFAConstants.BUILDING);
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
      case 1:  // check for cell to cap
        ACLMessage capRequest = new ACLMessage(ACLMessage.REQUEST);
        capRequest.addReceiver(hive);
        capRequest.setConversationId(DFAConstants.BUILD_ACTIVITY);
        capRequest.setReplyWith(DFAConstants.BUILD_ACTIVITY + System.currentTimeMillis());
        capRequest.setContent(DFAConstants.CAPPING);
        // Prepare template to receive response
        mt = MessageTemplate.and(MessageTemplate.MatchConversationId(DFAConstants.BUILD_ACTIVITY),
            MessageTemplate.MatchInReplyTo(capRequest.getReplyWith()));
        myAgent.send(capRequest);
        ++step;
        break;
      case 2: // Receive hive response
        ACLMessage reply = myAgent.receive(mt);
        if(reply != null) {
          if(reply.getPerformative() == ACLMessage.CONFIRM) {
            System.out.println("worker " + getLocalName() + " succesfully capped a cell");
            step = 0;
          }
          else if(reply.getPerformative() == ACLMessage.REFUSE) {
            ++step;
          }
        }
        else {
          block();
        }
        break;
      case 3:  // check for cell to clean
        ACLMessage cleanRequest = new ACLMessage(ACLMessage.REQUEST);
        cleanRequest.addReceiver(hive);
        cleanRequest.setConversationId(DFAConstants.BUILD_ACTIVITY);
        cleanRequest.setReplyWith(DFAConstants.BUILD_ACTIVITY + System.currentTimeMillis());
        cleanRequest.setContent(DFAConstants.CLEANING);
        // Prepare template to receive response
        mt = MessageTemplate.and(MessageTemplate.MatchConversationId(DFAConstants.BUILD_ACTIVITY),
            MessageTemplate.MatchInReplyTo(cleanRequest.getReplyWith()));
        myAgent.send(cleanRequest);
        ++step;
        break;
      case 4: // Receive hive response
        ACLMessage cleanReply = myAgent.receive(mt);
        if(cleanReply != null) {
          if(cleanReply.getPerformative() == ACLMessage.CONFIRM) {
            System.out.println("worker " + getLocalName() + " succesfully cleaned a cell");
            step = 0;
          }
          else if(cleanReply.getPerformative() == ACLMessage.REFUSE) {
            ++step;
          }
        }
        else {
          block();
        }
        break;
      case 5:  // expand hive
        ACLMessage expandRequest = new ACLMessage(ACLMessage.REQUEST);
        expandRequest.addReceiver(hive);
        expandRequest.setConversationId(DFAConstants.BUILD_ACTIVITY);
        expandRequest.setReplyWith(DFAConstants.BUILD_ACTIVITY + System.currentTimeMillis());
        expandRequest.setContent(DFAConstants.EXPANDING);
        // Prepare template to receive response
        mt = MessageTemplate.and(MessageTemplate.MatchConversationId(DFAConstants.BUILD_ACTIVITY),
            MessageTemplate.MatchInReplyTo(expandRequest.getReplyWith()));
        myAgent.send(expandRequest);
        ++step;
        break;
      case 6: // Receive hive response
        ACLMessage expandReply = myAgent.receive(mt);
        if(expandReply != null) {
          if(expandReply.getPerformative() == ACLMessage.CONFIRM) {
            System.out.println("worker " + getLocalName() + " succesfully expanded hive");
          }
          else if(expandReply.getPerformative() == ACLMessage.REFUSE) {
            try {
              Thread.sleep(DFAConstants.DAY_IN_MILLIS);
            } catch (InterruptedException e) {
              doDelete(); // This happens when closing JADE
            }
          }
        }
        else {
          block();
        }
        step = 0;
        break;
      default:
        step = 0;
        break;
      }
    }
  }
}

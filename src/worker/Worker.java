package worker;

import jade.core.Agent;

public class Worker extends Agent{

  private static final long serialVersionUID = -5162119966888825259L;
  
  private int age;

  protected void setup() {
    age = 0;
  }
}

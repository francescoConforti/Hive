package developmentStages;

public class Pupa implements DevelopingBee{

  private final int MAX_AGE;
  
  private int age;
  
  public Pupa(int maxAge) {
    MAX_AGE = maxAge;
  }
  
  @Override
  public int getAge() {
    return age;
  }

  @Override
  public int getMaxAge() {
    return MAX_AGE;
  }

  @Override
  public void increaseAge() {
    ++age;
  }

}

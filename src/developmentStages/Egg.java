package developmentStages;

public class Egg implements DevelopingBee{

  private final int MAX_AGE = 3;
  
  private int age = 0;
  
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

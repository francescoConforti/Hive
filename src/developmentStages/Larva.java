package developmentStages;

public class Larva implements DevelopingBee{

  private final int MAX_AGE;
  private final int MAX_FOOD;
  
  private int age;
  private int food;
  
  public Larva(int maxAge, int maxFood) {
    MAX_AGE = maxAge;
    MAX_FOOD = maxFood;
    age = 0;
    food = 0;
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
  
  public int getFood() {
    return food;
  }

  public int getMaxFood() {
    return MAX_FOOD;
  }

  public void feed() {
    ++food;
  }

}

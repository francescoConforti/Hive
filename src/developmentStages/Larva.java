package developmentStages;

public class Larva implements DevelopingBee{

  private final int MAX_AGE;
  private final int DAILY_FOOD_NEEDED;
  
  private int age;
  private int food;
  
  public Larva(int maxAge, int dailyFood) {
    MAX_AGE = maxAge;
    DAILY_FOOD_NEEDED = dailyFood;
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

  public int getDailyFood() {
    return DAILY_FOOD_NEEDED;
  }

  public void feed(int foodAmount) {
    food += foodAmount;
  }

}

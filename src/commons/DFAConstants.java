package commons;


public class DFAConstants {

  // Mating
  public final static String MATING = "mating";
  public final static String MATING_REQUEST = "mating request";
  public final static String MATING_ACCEPT = "mating accepted";
  public final static String MATING_REFUSED = "mating refused";
  
  // Eggs
  public final static int EGG_LAY_OR_RECEIVE_TIMER = 500;
  public final static String EGG = "egg";
  public final static String LAY_EGG = "lay egg";
  public final static String WORKER_EGG = "worker egg";
  public final static String DRONE_EGG = "drone egg";
  public final static String QUEEN_EGG = "queen egg";
  
  // Time
  public final static int DAY_IN_MILLIS = 1500;
  
  // Larvae
  public final static int FEEDING_TIMER = 50;
  public final static int WORKER_LARVA_MAX_FOOD = 10;
  public final static int DRONE_LARVA_MAX_FOOD = 1;
  public final static int QUEEN_LARVA_MAX_FOOD = 25;
  public final static String FEEDING = "feeding";
  public final static String FEED_ACTIVITY = "feed activity";
  public final static String FEED_WORKER = "feed worker";
  public final static String FEED_DRONE = "feed drone";
  
  // Resources
  public final static int GATHERING_TIMER = 50;
  public final static String GATHERING = "gathering";
  public final static String RESOURCE_EXCHANGE = "resource exchange";
  public final static String FOOD_EXCHANGE = "food exchange";
  public final static String MATERIALS_EXCHANGE = "materials exchange";
}

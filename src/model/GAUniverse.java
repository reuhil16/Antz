package model;

import java.util.HashMap;

public class GAUniverse extends Universe {
  
  public GAMaster master;
  public HashMap<Character, Integer> energyCosts;
  
  
  public void moveOneStep() {
    master.update();
    super.moveOneStep();
  }

}

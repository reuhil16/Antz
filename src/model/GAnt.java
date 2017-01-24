package model;

public class GAnt extends Ant implements Comparable<GAnt>{
  
  public int energy = 250;

  public GAnt(GAUniverse universe, AntType type) {
    super(universe, type);
    // TODO Auto-generated constructor stub
  }
  
  public void moveOneStep() { 
    int initialEnergy = ((GAUniverse)universe).energyCosts.get( universe.getState(position) );
    int finalEnergy = ((GAUniverse)universe).energyCosts.get( type.getState(lastDir, universe.getState(position)));
    if (initialEnergy > finalEnergy){
      energy += 3 * (initialEnergy - finalEnergy) / 4;
    } else {
      energy -= finalEnergy - initialEnergy;
    }
    if (energy >= GAMaster.stepEnergy) {
      super.moveOneStep();
    }
    energy -= GAMaster.stepEnergy;
  }

  public int compareTo(GAnt o) {
    return o.energy - energy;
  }

}

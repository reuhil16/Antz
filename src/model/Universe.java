/*
 * COSC326 - 2016 S2 - Étude 12 - Supersizing Ants
 * Thomas Farr, Reuben Hilder, Ben Scott
 * Java 8
 */

package model;

import java.awt.Point;
import java.util.HashMap;

public class Universe {
  public HashMap<String, AntType>  species = new HashMap<>();
  public HashMap<Point, Character> world   = new HashMap<>();
  public Ant[]     population;
  public Character defaultState;

  public void moveOneStep () {
    for (Ant ant : population) {
      ant.moveOneStep();
    }
  }

  public Character getState (Point position) {
    if (world.containsKey(position)) {
      return world.get(position);
    }
    return defaultState;
  }

  public void moveNSteps (int n) {
    for (int i = 0; i < n; i++) {
      moveOneStep();
    }
  }
}

/*
 * COSC326 - 2016 S2 - Étude 12 - Supersizing Ants
 * Thomas Farr, Reuben Hilder, Ben Scott
 * Java 8
 */

package model;

import java.awt.Point;
import java.util.HashMap;
import java.util.Set;

public class Universe {
  public HashMap<String, AntType> species = new HashMap<>();
  private HashMap<Point, Character> world = new HashMap<>();
  public Ant[] population;
  public UnassignedStateResolver resolver;
  public char[] states;
  public int width;
  public int height;
  public boolean wrap;

  public Universe() {
    resolver = this::defaultStateResolver;
  }
  
  public void clean(){
    population = null;
    species = new HashMap<>();
    world = new HashMap<>();
  }

  public void moveOneStep() {
    for (Ant ant : population) {
      ant.moveOneStep();
    }
  }

  public Character getState(Point position) {
    if (world.containsKey(position)) {
      return world.get(position);
    }
    return resolver.resolveState(position);
  }
  
  public void setState(Point p, Character state) {
    world.put(p, state);
  }
  
  public Set<Point> getDefinedPoints() {
    return world.keySet();
  }

  public void moveNSteps(int n) {
    for (int i = 0; i < n; i++) {
      moveOneStep();
    }
  }

  public char defaultStateResolver(Point p) {
    return states[0];
  }
  
  public char horizonSplitter(Point p){
    if (p.y > 0) {
      return states[0];
    }
    return states[1];
  }

  interface UnassignedStateResolver {
    char resolveState(Point p);
  }
}

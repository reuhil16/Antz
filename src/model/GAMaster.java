package model;

import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class GAMaster {

  private double mutation = 0.05;
  private int eliteism = 5;
  private int simulationLength = 500;
  private int stepCount;
  private Random rng = new Random();
  private int generationSize = 50;
  private int generationNum;
  private int indevidualNum;
  public static final int stepEnergy = 5;
  private Universe universe;
  
  public int spawnXRange;
  public int spawnXMin;
  public int spawnYRange;
  public int spawnYMin;

  public GAMaster(Universe universe) {
    this.universe = universe;
  }

  private AntType generateChild(AntType father, AntType mother) {
    AntType child = new AntType(generationNum + ":" + indevidualNum);
    for (char state : father.DNA.keySet()) {
      int[] directions = new int[4];
      char[] states = new char[4];
      for (int i = 0; i < 4; i++) {

        if (rng.nextDouble() < mutation) {
          directions[i] = rng.nextInt(4);
        } else if (rng.nextBoolean()) {
          directions[i] = father.getMove(i, state);
        } else {
          directions[i] = mother.getMove(i, state);
        }

        if (rng.nextDouble() < mutation) {
          states[i] = universe.states[rng.nextInt(universe.states.length)];
        } else if (rng.nextBoolean()) {
          states[i] = father.getState(i, state);
        } else {
          states[i] = mother.getState(i, state);
        }

      }

      child.addChromosome(state, directions, states);
      
    }

    return child;
  }

  private void nextGen() {
    generationNum++;
    indevidualNum = 0;
    stepCount = 0;
    GAnt[] oldPop = (GAnt[]) universe.population;
    Arrays.sort(oldPop);
    AntType[] newPop = new AntType[generationSize];
    int totalEnergy = 0;
      for (int i = 0; i < oldPop.length; i++) {
        oldPop[i].energy -= oldPop[oldPop.length - 1].energy;
      }
    
    for (int i = 0; i < eliteism; i++) {
      newPop[i] = oldPop[i].type;
    }
    
    for (int i = 0; i < oldPop.length; i++) {
      totalEnergy += oldPop[i].energy;
    }
    
    for (int i = eliteism; i < generationSize; i++) {
      AntType father = null;
      AntType mother = null;
      //select father
      int energyCost = rng.nextInt(totalEnergy);
      for (GAnt a : oldPop) {
        if (a.energy > energyCost)  {
          father = a.type;
          energyCost = rng.nextInt(totalEnergy - a.energy);
          break;
        } else {
          energyCost -= a.energy;
        }
      }
      
      //select mother
      for (GAnt a : oldPop) {
        if (a.type.equals(father))
          continue;
        if (a.energy > energyCost)  {
          mother = a.type;
          break;
        } else {
          energyCost -= a.energy;
        }
      }
      
      newPop[i] = generateChild(father, mother);
      indevidualNum ++;
      
    }
    
    universe.clean();
    universe.population = new GAnt[generationSize];
    HashSet<Point> usedSpawns = new HashSet<>();
    for(int i = 0; i < generationSize; i++) {
      universe.species.put(newPop[i].name, newPop[i]);
      universe.population[i] = new GAnt((GAUniverse) universe, newPop[i]);
      Point p;
      do {
      p = new Point(rng.nextInt(spawnXRange) + spawnXMin, rng.nextInt(spawnYRange) + spawnYMin);
      } while (usedSpawns.contains(p));
      universe.population[i].position = p;
      usedSpawns.add(p);
    }
  }

  public void initialise() {
    universe.clean();
    universe.population = new GAnt[generationSize];
    HashSet<Point> usedSpawns = new HashSet<>();
    AntType newType;
    char[] states;
    int[] directions;
    for(int i = 0; i < generationSize; i++) {
      newType = new AntType("0:" + indevidualNum);
      
      for(char state : universe.states) {
        states = new char[4];
        directions = new int[4];
        for(int j = 0; j < 4; j++){
          states[j] = universe.states[rng.nextInt(universe.states.length)];
          directions[j] = rng.nextInt(4);
        }
        newType.addChromosome(state, directions, states);
      }
      
      universe.species.put(newType.name, newType);
      universe.population[i] = new GAnt((GAUniverse) universe, newType);
      Point p;
      do {
      p = new Point(rng.nextInt(spawnXRange) + spawnXMin, rng.nextInt(spawnYRange) + spawnYMin);
      } while (usedSpawns.contains(p));
      universe.population[i].position = p;
      usedSpawns.add(p);
      indevidualNum++;
    }
  }
  
  public void update() {
    stepCount++;
    if (stepCount > simulationLength) {
      nextGen();
    }
  }

}

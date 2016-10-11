package Model;

import java.util.HashMap;

public class AntType {
  public final String name;
  private final HashMap<Character, Chromosome> DNA = new HashMap<>();

  public AntType (String name) {
    this.name = name;
  }

  public int getMove (int lastDir, Character state) {
    return DNA.get(state).directions[lastDir];
  }

  public Character getState (int lastDir, Character state) {
    return DNA.get(state).states[lastDir];
  }

  public void addChromosome (char state, int[] directions, char[] states) {
    DNA.put(state, new Chromosome(directions, states));
  }

  class Chromosome {
    int[]  directions;
    char[] states;

    public Chromosome (int[] directions, char[] states) {
      this.directions = directions;
      this.states = states;
    }
  }
}

package Model;

import java.util.HashMap;

public class AntType {
	private final HashMap<Character, Chromosome> DNA = new HashMap<>();
	
	public int getMove(int lastDir, char state){
		return DNA.get(state).directions[lastDir];
	}
	
	public char getState(int lastDir, char state){
		return DNA.get(state).states[lastDir];
	}
	
	class Chromosome {
		int[] directions;
		char[] states;
	}
}

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import Model.*;
public class Application {
	
	private static Universe model;

	public static void main(String[] args) {
		model = new Universe();
		HashSet<Character> worldStates = new HashSet<>();
		ArrayList<Ant> ants = new ArrayList<>();
		
		Scanner sc = new Scanner(System.in);
		String[] line;
		
		while(sc.hasNextLine()) {
			line = sc.nextLine().trim().split("\\s+");
			
			if (line.length == 0)
				continue;
			
			if (line.length == 2){
				if (line[0].equalsIgnoreCase("ant")){
					if (validateAntName(line[1])){
						model.species.put(line[1], parseAnt(sc, line[1]));
						continue;
					} else {
						printAndExit("Duplicate or invalid Ant name:", line);
					}
				} 
				
				if (line[0].equalsIgnoreCase("World")){
					model.defaultState = line[1].charAt(0);
					for (char c : line[1].toCharArray())
						worldStates.add(c);
					continue;
				} 
			} 
			
			if (line.length == 3) {
				int x = 0, y = 0;
				
				if (model.species.containsKey(line[0])){
					try {
						x = Integer.valueOf(line[1]);
						y = Integer.valueOf(line[2]);
					} catch (NumberFormatException e){
						printAndExit("Invalid ant position:", line);
					}
					Ant ant = new Ant(model, model.species.get(line[0]));
					ant.position = new Point(x, y);
					ants.add(ant);
					continue;
				} 
				
				if (line[2].length() == 1){
					try {
						x = Integer.valueOf(line[0]);
						y = Integer.valueOf(line[1]);
					} catch (NumberFormatException e){
						printAndExit("Invalid ant position:", line);
					}
					model.world.put(new Point(x, y), line[2].charAt(0));
					continue;
				}
			}
			
			printAndExit("Invalid line:", line);
		}
		if (worldStates.isEmpty())
			printAndExit("Allowed states not specified.", null);
		model.population = ants.toArray(new Ant[ants.size()]);
		for (AntType a : model.species.values()){
			for (Character c : worldStates){
				if (a.getState(0, c) == null)
					printAndExit("Missing DNA for ant type " + a.name + ": " + c, null);
				for (int i = 0; i < 4; i++) {
					if(!worldStates.contains(a.getState(i, c)))
						printAndExit("DNA for ant type " + a.name + ", " + c 
								+ " contains invalit target state: " 
								+ a.getState(i, c), null);
				}
			}
		}
		for (Point p : model.world.keySet()){
			if (!worldStates.contains(model.world.get(p))){
				printAndExit("Invalid world state '" + model.world.get(p) 
				+ "' at position " + p.toString(), null);
			}
		}
		
	}
	
	public static AntType parseAnt(Scanner sc, String name){
		String[] line;
		AntType result = new AntType(name);
		
		while(sc.hasNextLine()) {
			line = sc.nextLine().trim().split("\\s+");
			if (line[0].equalsIgnoreCase("END"))
				break;
			if (line.length != 3 || line[0].length() != 1 || 
					line[1].length() != 4 || line[2].length() != 4) {
				printAndExit("Invalid DNA Line:", line);
			}
			int[] directions = new int[4];
			for (int i = 0; i < 4; i++){
				switch(line[1].charAt(i)){
				case 'N':
					directions[i] = 0;
					break;
				case 'E':
					directions[i] = 1;
					break;
				case 'S':
					directions[i] = 2;
					break;
				case 'W':
					directions[i] = 3;
					break;
				default:
					printAndExit("Invalid Direction in DNA line:", line);
					break;
				}
			}
			result.addChromosome(line[0].charAt(0), directions, line[2].toCharArray());
			
		}
		
		return result;
	}
	
	public static boolean validateAntName(String name){
		if (model.species.containsKey(name))
			return false;
		if (name.equalsIgnoreCase("size"))
			return false;
		try {
			Integer.valueOf(name);
			return false;
		} catch (NumberFormatException e){}
		return true;
	}
	
	public static void printAndExit(String message, String[] line){
		System.err.println(message);
		if (line != null && line.length > 0){
			System.err.print(line[0]);
			for (int i = 1; i < line.length; i++) {
				System.err.print(" ");
				System.err.print(line[i]);
			}
			System.err.println();
		}
		System.exit(1);
	}

}

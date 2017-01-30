/*
 * COSC326 - 2016 S2 - Étude 12 - Supersizing Ants
 * Thomas Farr, Reuben Hilder, Ben Scott
 * Java 8
 */

package input;

import model.Ant;
import model.AntType;
import model.GAMaster;
import model.GAUniverse;
import model.Universe;

import java.awt.Point;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class InputHandler {
  private static Universe universe;
  private static int lineCount;

  public static Universe initialiseUniverse (InputStream in) {
    Scanner sc = new Scanner(in);
    String[] line;
    String inputLine;
    lineCount = 0;
    /*
    if(sc.hasNext() && sc.next().equals("GA")) {
      GAUniverse gAUniverse = new GAUniverse();
      universe = gAUniverse;
      gAUniverse.master = new GAMaster(gAUniverse);
      universe.defaultStates = new char[]{'w', 'b'};
      universe.width = 100;
      universe.height = 100;
      universe.wrap = false;
      universe.states = new char[]{'w', 'b', 'r', 'y', 'g', 'c', 'p'};
      gAUniverse.energyCosts = new HashMap<>();
      
      gAUniverse.energyCosts.put('w', 0);
      gAUniverse.energyCosts.put('b', 100);
      gAUniverse.energyCosts.put('r', 0);
      gAUniverse.energyCosts.put('g', 0);
      gAUniverse.energyCosts.put('y', 40);
      gAUniverse.energyCosts.put('c', 8);
      gAUniverse.energyCosts.put('p', 100);
      
      gAUniverse.master.spawnXMin = 0;
      gAUniverse.master.spawnXRange = 150;
      gAUniverse.master.spawnYMin = -4;
      gAUniverse.master.spawnYRange = 8;
      
      gAUniverse.master.initialise();
      
      gAUniverse.setState(new Point(-20, 0), 'g');
      
      sc.close();
      
      return universe;
    }
    */
    
    universe = new Universe();
    HashSet<Character> worldStates = new HashSet<>();
    ArrayList<Ant> ants = new ArrayList<>();
    
    int x = 0, y = 0;

    
    while (sc.hasNextLine()) {
      inputLine = sc.nextLine();
      lineCount++;
      
      //Remove comments
      if (inputLine.contains("//"))
        inputLine = inputLine.substring(0, inputLine.indexOf("//"));
      line = inputLine.trim().split("\\s+");

      //Remove blank lines 
      if (line.length == 0 || (line.length == 1 && line[0].isEmpty()) ) {
        continue;
      }

      switch(line[0].toLowerCase()) {
        case "ant":
          if(line.length != 2) {
            printAndExit("Unexpected number of arguments", line);
          }
          universe.species.put(line[1], parseAnt(sc, line));
          break;
          
        case "world":
          if(line.length != 2) {
            printAndExit("Unexpected number of arguments", line);
          }
          for (char c : line[1].toCharArray()) {
            worldStates.add(c);
          }
          universe.states = line[1].toCharArray();
          break;
          
        case "size":
          if (universe.wrap) {
            printAndExit("Duplicate size declaration:", line);
          }
          try {
            x = Integer.valueOf(line[1]);
            y = Integer.valueOf(line[2]);
          } catch (NumberFormatException e) {
            printAndExit("Invalid size:", line);
          }
          if (x <= 0 || y <= 0){
            printAndExit("Invalid size:", line);
          }
          universe.wrap = true;
          universe.width = x;
          universe.height = y;
          break;
          
        case "ga":
          parseGA(sc);
          break;
          
        default:
          if (line.length == 3) {

            if (universe.species.containsKey(line[0])) {
              try {
                x = Integer.valueOf(line[1]);
                y = Integer.valueOf(line[2]);
              } catch (NumberFormatException e) {
                printAndExit("Invalid ant position", line);
              }
              Ant ant = new Ant(universe, universe.species.get(line[0]));
              ant.position = new Point(x, y);
              ants.add(ant);
              break;
            }
            if (line[2].length() == 1 && worldStates.contains(line[2].charAt(0))) {
              try {
                x = Integer.valueOf(line[0]);
                y = Integer.valueOf(line[1]);
              } catch (NumberFormatException e) {
                printAndExit("Invalid cell position", line);
              }
              universe.setState(new Point(x, y), line[2].charAt(0));
              break;
            }
          }

          printAndExit("Invalid line", line);
          break;
      }
        
      /*
      if (line.length == 2) {
        
        if (line[0].equalsIgnoreCase("ant")) {
          if (validateAntName(line[1])) {
            universe.species.put(line[1], parseAnt(sc, line[1]));
            continue;
          } else {
            printAndExit("Duplicate or invalid Ant name:", line);
          }
        }
        

        if (line[0].equalsIgnoreCase("World")) {
          universe.defaultStates = new  char[] {line[1].charAt(0)};
          for (char c : line[1].toCharArray()) {
            worldStates.add(c);
          }
          universe.states = line[1].toCharArray();
          continue;
        }
      }
      

      if (line.length == 3) {

        if (universe.species.containsKey(line[0])) {
          try {
            x = Integer.valueOf(line[1]);
            y = Integer.valueOf(line[2]);
          } catch (NumberFormatException e) {
            printAndExit("Invalid ant position:", line);
          }
          Ant ant = new Ant(universe, universe.species.get(line[0]));
          ant.position = new Point(x, y);
          ants.add(ant);
          continue;
        }
        
        if (line[0].equalsIgnoreCase("size")) {
          if (universe.wrap) {
            printAndExit("Duplicate size declaration:", line);
          }
          try {
            x = Integer.valueOf(line[1]);
            y = Integer.valueOf(line[2]);
          } catch (NumberFormatException e) {
            printAndExit("Invalid size:", line);
          }
          if (x <= 0 || y <= 0){
            printAndExit("Invalid size:", line);
          }
          universe.wrap = true;
          universe.width = x;
          universe.height = y;
          continue;
        }
        

        if (line[2].length() == 1) {
          try {
            x = Integer.valueOf(line[0]);
            y = Integer.valueOf(line[1]);
          } catch (NumberFormatException e) {
            printAndExit("Invalid ant position:", line);
          }
          universe.setState(new Point(x, y), line[2].charAt(0));
          continue;
        }
      }

      printAndExit("Invalid line:", line);
      */
    }

    if (worldStates.isEmpty()) {
      printAndExit("Allowed states not specified.", null);
    }

    universe.population = ants.toArray(new Ant[ants.size()]);

    for (AntType a : universe.species.values()) {
      for (Character c : worldStates) {
        if (a.getState(0, c) == null) {
          printAndExit("Missing DNA for ant type " + a.name + ": " + c, null);
        }
        for (int i = 0; i < 4; i++) {
          if (!worldStates.contains(a.getState(i, c))) {
            printAndExit("DNA for ant type " + a.name + ", " + c
                         + " contains invalid target state: " + a
                             .getState(i, c), null);
          }
        }
      }
    }

    for (Point p : universe.getDefinedPoints()) {
      if (!worldStates.contains(universe.getState(p))) {
        printAndExit(
            "Invalid world state '" + universe.getState(p) + "' at position "
            + p.toString(), null);
      }
    }
    
    if (universe.wrap){
      for (Ant ant : universe.population){
        ant.position.x = ant.position.x % universe.width;
        ant.position.y = ant.position.y % universe.height;
      }
    }
    
    return universe;

    /*
    if (args.length > 0) {
      try {
        universe.moveNSteps(Integer.valueOf(args[0]));
      } catch (NumberFormatException ignored) {
      }

      System.out.println(Arrays.toString(universe.population));
    }
    */
  }
  
  public static void parseGA(Scanner sc) {
    GAUniverse universe = new GAUniverse();
    GAUniverse gAUniverse = new GAUniverse();
    universe = gAUniverse;
    gAUniverse.master = new GAMaster(gAUniverse);
    universe.width = 100;
    universe.height = 100;
    universe.wrap = false;
    universe.states = new char[]{'w', 'b', 'r', 'y', 'g', 'c', 'p'};
    gAUniverse.energyCosts = new HashMap<>();
    
    gAUniverse.energyCosts.put('w', 0);
    gAUniverse.energyCosts.put('b', 100);
    gAUniverse.energyCosts.put('r', 0);
    gAUniverse.energyCosts.put('g', 0);
    gAUniverse.energyCosts.put('y', 40);
    gAUniverse.energyCosts.put('c', 8);
    gAUniverse.energyCosts.put('p', 100);
    
    gAUniverse.master.spawnXMin = 0;
    gAUniverse.master.spawnXRange = 150;
    gAUniverse.master.spawnYMin = -4;
    gAUniverse.master.spawnYRange = 8;
    
    gAUniverse.master.initialise();
    
    gAUniverse.setState(new Point(-20, 0), 'g');
  }

  public static AntType parseAnt (Scanner sc, String[] line) {
    if (!validateAntName(line[1])) {
      printAndExit("Duplicate or invalid Ant name", line);
    } 
    AntType result = new AntType(line[1]);

    String inputLine;

    while (sc.hasNextLine()) {
      inputLine = sc.nextLine();
      if (inputLine.contains("//"))
        inputLine = inputLine.substring(0, inputLine.indexOf("//"));
      line = inputLine.trim().split("\\s+");

      if (line.length == 0 || (line.length == 1 && line[0].isEmpty()) ) {
        continue;
      }

      if (line[0].equalsIgnoreCase("END")) {
        break;
      }

      if (line.length != 3 || line[0].length() != 1 || line[1].length() != 4
          || line[2].length() != 4) {
        printAndExit("Invalid DNA Line:", line);
      }

      int[] directions = new int[4];
      for (int i = 0; i < 4; i++) {
        switch (line[1].toUpperCase().charAt(i)) {
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

      result
          .addChromosome(line[0].charAt(0), directions, line[2].toCharArray());
    }

    return result;
  }

  public static boolean validateAntName (String name) {
    if (universe.species.containsKey(name)) {
      return false;
    }

    if (name.equalsIgnoreCase("size")) {
      return false;
    }

    try {
      Integer.valueOf(name);
      return false;
    } catch (NumberFormatException ignored) {
    }

    return true;
  }

  public static void printAndExit (String message, String[] line) {
    System.err.print(message);
    System.err.print(" at line ");
    System.err.print(lineCount);
    System.err.println(":");

    if (line != null && line.length > 0) {
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

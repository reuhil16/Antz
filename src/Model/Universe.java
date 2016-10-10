package Model;

import java.awt.Point;
import java.util.HashMap;

public class Universe {
	
	public HashMap<String, AntType> species = new HashMap<>();
	
	public HashMap<Point, Character> world = new HashMap<>();
	
	public Ant[] population;
	
	public Character defaultState;
	
	public void moveOneStep() {
            
            for(Ant ant: population){
                ant.moveOneStep();
            }

	}

    public void moveNSteps(int n){
        
	for(int i=0; i<n; i++){
            moveOneStep();
        }
    }
    
}

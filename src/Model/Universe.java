package Model;

import java.awt.Point;
import java.util.HashMap;

public class Universe {
	
	public HashMap<String, AntType> species;
	
	public HashMap<Point, Character> world;
	
	public Ant[] population;
	
	
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

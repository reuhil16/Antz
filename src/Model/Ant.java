package Model;

import java.awt.Point;

public class Ant {

    private Universe universe;
	private AntType type;
	public int lastDir;
	public Point position;
	
    public Ant(Universe universe, AntType type) {
		this.type = type;
                this.universe = universe;
	}
	
	public void moveOneStep() {

            int curDir = lastDir;

            lastDir = type.getMove(curDir,universe.world.get(position));
            universe.world.put(position, type.getState(curDir,universe.get(position)));

            switch(curDir){
                case 0: position.translate(1,0); break;
                case 1: position.translate(-1,0); break;
                case 2: position.translate(0,-1); break;
                case 3: position.translate(0,1); break;
            }
		
	}
}

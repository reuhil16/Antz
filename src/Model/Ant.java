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

            lastDir = type.getMove(lastDir,universe.getState(position));
            
            switch(lastDir){
                case 0: position.translate(1,0); break;
                case 1: position.translate(-1,0); break;
                case 2: position.translate(0,-1); break;
                case 3: position.translate(0,1); break;
            }

            universe.world.put(position, type.getState(lastDir,universe.getState(position)));
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(type.name);
		sb.append(" at ");
		sb.append(position);
		return sb.toString();
	}
}

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;

public class Model {
	
	public HashMap<String, AntType> species;
	
	public HashMap<Point2D, Character> world;
	
	public Ant[] population;
	
	

	class Ant {
		private AntType type;
		public int lastDir;
		public Point position;
		
		public Ant(AntType type) {
			this.type = type;
		}

	}
}

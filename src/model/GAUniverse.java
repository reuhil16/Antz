package model;

import java.awt.Point;
import java.util.HashMap;

public class GAUniverse extends Universe {
  
  public GAMaster master;
  public HashMap<Character, Integer> energyCosts;
  private boolean live;
  private HashMap<Point, Integer> liveCells;
  
  
  public GAUniverse() {
    resolver = this::horizonSplitter;
  }

  public void moveOneStep() {
    master.update();
    super.moveOneStep();
    if(live){
      
      CHECK_CELLS:
      for (Point p : liveCells.keySet()) {
        for(int y = p.y + 1; y <= height; y++) {
          if (getState(new Point(p.x,y)) != 'w') {
            liveCells.put(p, 0);
            continue CHECK_CELLS;
          }
        }
        if (liveCells.get(p) >= 1 && getState(new Point(p.x, p.y-1)).charValue() == 'b') {
          setState(new Point(p.x, p.y-1), 'y');
        } else  if (liveCells.get(p) >= 4 && getState(new Point(p.x, p.y-1)) == 'c') {
          setState(new Point(p.x, p.y-1), 'p');
        } else {
        liveCells.put(p, liveCells.get(p) + 1); 
        }
      }
    }
  }
  
  @Override
  public void setState(Point p, Character state) {
    if (state.charValue() == 'g') {
      liveCells.put(p, 0);
    } else {
      liveCells.remove(p);
    }
    super.setState(p, state);
    if (Math.abs(p.x) > width) {
      width = Math.abs(p.x);
    }
    if (Math.abs(p.y) > height) {
      height = Math.abs(p.y);
    }
  }
  
  @Override
  public void clean() {
    liveCells = new HashMap<>();
    live = true;
    super.clean();
  }

}

package Model;

import java.awt.*;

public class Ant {
  public  int      lastDir;
  public  Point    position;
  private Universe universe;
  private AntType  type;

  public Ant (Universe universe, AntType type) {
    this.type = type;
    this.universe = universe;
  }

  public void moveOneStep () {
    int curDir = type.getMove(lastDir, universe.getState(position));

    universe.world.put(new Point(position),
                       type.getState(lastDir, universe.getState(position)));

    lastDir = curDir;

    switch (lastDir) {
      case 0:
        position.translate(0, 1);
        break;
      case 1:
        position.translate(1, 0);
        break;
      case 2:
        position.translate(0, -1);
        break;
      case 3:
        position.translate(-1, 0);
        break;
    }
  }

  @Override
  public String toString () {
    StringBuilder sb = new StringBuilder(type.name);
    sb.append(" at ");
    sb.append(position);
    return sb.toString();
  }
}

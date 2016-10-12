/*
 * COSC326 - 2016 S2 - Étude 12 - Supersizing Ants
 * Thomas Farr, Reuben Hilder, Ben Scott
 * Java 8
 */

package model;

import java.awt.Point;

public class Ant {
  private   int      lastDir;
  public  Point    position;
  private final Universe universe;
  public final AntType  type;

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
    
    if (universe.wrap){
      position.x = wrapPosition(position.x, universe.width);
      position.y = wrapPosition(position.y, universe.height);
    }
  }

  private static int wrapPosition(int n, int size) {
    int max = size / 2;
    int min = -size / 2 + (size % 2 == 0 ? 1 : 0);

    if (n < min) {
      n = max - (n - min + 1);
    } else if (n > max) {
      n = min + (n - max - 1);
    }
    return n;
  }

  @Override
  public String toString () {
    StringBuilder sb = new StringBuilder(type.name);
    sb.append(" at ");
    sb.append(position);
    return sb.toString();
  }
}

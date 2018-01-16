import java.util.Arrays;

import bc.*;

public class Player {	
	public static char[][] fetchMapData(Planet p, GameController gc) {
		PlanetMap m = gc.startingMap(p);
		long width = m.getWidth();
		long height = m.getHeight();
		char[][] map = new char[(int) height][(int) width];
		for (int i = 0; i < (int) height; i++) {
			for (int j = 0; j < (int) width; j++) {
				map[i][j] = m.isPassableTerrainAt(new MapLocation(gc.planet(), j, i)) == 1 ? '0' : '1';
			}
		}
		return map;
	}

	public static void main(String[] args) {
        // Connect to the manager, starting the game
        GameController gc = new GameController();
        // Fetch the map of the current planet and store it in an array
 		char[][] currentMap = fetchMapData(gc.planet(), gc);
 		
        // Direction is a normal java enum.
        Direction[] directions = Direction.values();

        while (true) {
            System.out.println("Current round: "+gc.round());
            
            // VecUnit is a class that you can think of as similar to ArrayList<Unit>, but immutable.
            VecUnit units = gc.myUnits();
            
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);

                // Most methods on gc take unit IDs, instead of the unit objects themselves.
                if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), Direction.North)) {
                    gc.moveRobot(unit.id(), Direction.North);
                }
            }
            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}

import java.util.ArrayList;

import bc.*;

public class FalconMap {
	public GameController gc;
	public MapNode[][] map;
	
	public int width;
	public int height;
	public Team team; 
	public Planet planet;
	
	public FalconMap() {
		// For testing only please don't use this or remove it
	}
	
	public FalconMap(GameController gcx) {
		 this.gc = gcx;
		 this.initMap(gcx);
		 this.team = gcx.team();
	}
	
	public void initMap(GameController gc) {
		// Init the map as an array of Nodes with 
		this.planet = gc.planet();
		PlanetMap m = gc.startingMap(this.planet);
		this.width = (int) m.getWidth();
		this.height = (int) m.getHeight();
		this.map = new MapNode[(int) height][(int) width];
		
		char tag;
		int karbonite;
		VecUnit initialUnits = m.getInitial_units();
		
		for (int i = 0; i < (int) height; i++) {
			for (int j = 0; j < (int) width; j++) {
				MapLocation tmp = new MapLocation(gc.planet(), j, i);
				karbonite = (int) m.initialKarboniteAt(tmp);
				tag = karbonite > 0 ? '1' : '0';
				map[i][j] = new MapNode(j, i, karbonite, tag, (boolean) (m.isPassableTerrainAt(tmp) == 1));
			}
		}
		
		// Now check every unit and populate into the map
		for (int i = 0; i < initialUnits.size(); i++) {
			Unit u = initialUnits.get(i);
			int ux = u.location().mapLocation().getX();
			int uy = u.location().mapLocation().getY();
			if (u.team() == this.team) {
				map[uy][ux].setTag('w');
			} else {
				map[uy][ux].setTag('W');
			}
		}
	}
	
	public MapNode get(int x, int y) {
		return map[y][x];
	}
	
	public void printMap() {
		for (int i = 0; i < this.map.length; i++) {
			for (int j = 0; j < this.map[0].length; j++) {
				System.out.print(this.map[i][j].getTag());
			}
			System.out.println();
		}
	}
	
	public void decreaseKarbonite(int x, int y, int amount) {
		map[y][x].removeKarbonite(amount);
	}
	
	public void setKarbonite(int x, int y, int amount) {
		map[y][x].setKarbonite(amount);
	}
	
	public boolean isPassable(int x, int y) {
		return this.map[y][x].isPassable();
	}
	
	public void changePassability(int x, int y, boolean passable) {
		map[y][x].passable = passable;
	}
	
	public boolean isOnMap(int x, int y) {
		return x >= 0 && x < this.width && y >= 0 && y < this.height;
	}
	
	public MapLocation ringSearch(int centerX, int centerY, char targetChar) {
		// Search by expanding rings
		int maxRadius = (int) Math.max(Math.max(this.width - 1 - centerX, centerX), Math.max(this.height - 1 - centerY, centerY));
			
		for (int radius = 1; radius < maxRadius; radius++) {
			// I hate how this is written
			for (int x = centerX - radius; x <= centerX + radius; x += 2*radius) {
				for (int y = centerY - radius; y < centerY + radius; y++) {
					if (this.isOnMap(x, y) && targetChar == map[y][x].getTag()) {
						return new MapLocation(this.planet, x, y);
//						return new int[] {x, y};
					}
				}
			}
			for (int y = centerY - radius; y <= centerY + radius; y += 2*radius) {
				for (int x = centerX - radius; x < centerX + radius; x++) {
					if (this.isOnMap(x, y) && targetChar == map[y][x].getTag()) {
						return new MapLocation(this.planet, x, y);
//						return new int[] {x, y};
					}
				}
			}
		}
		
		// Nothing found. Just be careful when handling these return values
		return null;
	}
}

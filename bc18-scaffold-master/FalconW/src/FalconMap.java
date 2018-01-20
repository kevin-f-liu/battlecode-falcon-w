import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import bc.*;

public class FalconMap {
	public GameController gc;
	public MapNode[][] map;
	public HashMap<Character, ArrayList<MapNode>> nodeContentMap;
	
	public int width;
	public int height;
	public Team team; 
	public Planet planet;
	
	public FalconMap() {
		// For testing only please don't use this or remove it
		this.nodeContentMap = new HashMap<Character, ArrayList<MapNode>>();
	}
	
	public FalconMap(GameController gcx) {
		 this.gc = gcx;
		 this.nodeContentMap = new HashMap<Character, ArrayList<MapNode>>();
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
				tag = '0'; // Default tag to nothing
				MapNode node = new MapNode(j, i, karbonite, tag, (boolean) (m.isPassableTerrainAt(tmp) == 1));
				map[i][j] = node;
				this.updateNodeTag(j, i, tag); // Init the nodes in nodeContentMap
			}
		}
		
		// Now check every unit and update the map
		for (int i = 0; i < initialUnits.size(); i++) {
			Unit u = initialUnits.get(i);
			int ux = u.location().mapLocation().getX();
			int uy = u.location().mapLocation().getY();
			if (u.team() == this.team) {
				this.updateNodeTag(ux, uy, 'q');
			} else {
				this.updateNodeTag(ux, uy, 'W');
			}
		}
	}
	
	/**
	 * Properly update the node in the map. Changes the tag of the node in map
	 * as well as the node's position in nodeContentMap
	 * @param x
	 * @param y
	 * @param newTag
	 */
	public void updateNodeTag(int x, int y, char newTag) {
		// Get the node first
		MapNode node = map[y][x];
		char oldTag = node.getTag();
		node.setTag(newTag);
		if (nodeContentMap.containsKey(oldTag)) {
			// Remove 
			nodeContentMap.get(oldTag).remove(node);
		}
		if (nodeContentMap.containsKey(newTag)) {
			// Add node to map if key exists already
			nodeContentMap.get(newTag).add(node);
		} else {
			ArrayList<MapNode> nodeList = new ArrayList<MapNode>();
			nodeList.add(node);
			nodeContentMap.put(newTag, nodeList);
		}
		
	}
	
	public Planet getPlanet() {
		return this.planet;
	}
	
	/**
	 * Get's a node
	 * @param x
	 * @param y 
	 * @return The node instance
	 */
	public MapNode get(int x, int y) {
		return map[y][x];
	}
	
	public char getNodetag(int x, int y) { 
		return this.map[y][x].getTag();
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
	
	/**
	 * Do a search for the nearest mapnode with contentTag matching targetChar
	 * @param centerX 
	 * @param centerY
	 * @param targetChar
	 * @return MapLocation of the found node
	 */
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
	
	public void printMap() {
		for (int i = 0; i < this.map.length; i++) {
			for (int j = 0; j < this.map[0].length; j++) {
				System.out.print(this.map[i][j].getTag());
			}
			System.out.println();
		}
	}
	
	public void printContentMap() {
		Set<Character> keys = this.nodeContentMap.keySet();
		for (Character key : keys) {
			System.out.print(key + ": ");
			System.out.println(this.nodeContentMap.get(key));
		}
	}
}

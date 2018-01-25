import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import bc.*;

public class FalconMap {
	public HashMap<UnitType, Character> unitLegend; // Stores the character for every unit
	
	public GameController gc;
	public MapNode[][] map;
	public HashMap<Character, ArrayList<MapNode>> nodeContentMap;
	public ArrayList<MapNode> karboniteDeposits;
	public ArrayList<ArrayList<MapNode>> karboniteBlobs; // Ordered in best to worst
	public ArrayList<MapNode> impassableTerrain;
	
	public int width;
	public int height;
	public Team team; 
	public Planet planet;
	
	public FalconMap() {
		// For testing only please don't use this or remove it
		this.nodeContentMap = new HashMap<Character, ArrayList<MapNode>>();
		this.initUnitLegend();
		this.karboniteDeposits = new ArrayList<MapNode>();
		
	}
	
	public FalconMap(GameController gcx) {
		 this.gc = gcx;
		 this.team = gcx.team();
		 this.nodeContentMap = new HashMap<Character, ArrayList<MapNode>>();
		 
		 this.initUnitLegend();
		 this.initMap(gcx);
	}
	
	public void initUnitLegend() {
		this.unitLegend = new HashMap<UnitType, Character>();
		this.unitLegend.put(UnitType.Worker, 'w');
		this.unitLegend.put(UnitType.Knight, 'k');
		this.unitLegend.put(UnitType.Ranger, 'r');
		this.unitLegend.put(UnitType.Mage, 'm');
		this.unitLegend.put(UnitType.Healer, 'h');
		this.unitLegend.put(UnitType.Factory, 'f');
		this.unitLegend.put(UnitType.Rocket, 'r');
	}
	
	public void initMap(GameController gc) {
		// Init the map as an array of Nodes with 
		this.planet = gc.planet();
		PlanetMap m = gc.startingMap(this.planet);
		this.width = (int) m.getWidth();
		this.height = (int) m.getHeight();
		this.map = new MapNode[(int) height][(int) width];
		this.karboniteDeposits = new ArrayList<MapNode>();
		this.impassableTerrain = new ArrayList<MapNode>();
		
		char tag;
		int karbonite;
		VecUnit initialUnits = m.getInitial_units();
		MapLocation tmp;
		
		for (int i = 0; i < (int) height; i++) {
			for (int j = 0; j < (int) width; j++) {
				tmp = new MapLocation(gc.planet(), j, i);
				karbonite = (int) m.initialKarboniteAt(tmp);
				tag = '0'; // Default tag to nothing
				MapNode node = new MapNode(j, i, karbonite, tag, -1, (boolean) (m.isPassableTerrainAt(tmp) == 1));
				map[i][j] = node;
				if (m.isPassableTerrainAt(tmp) == 0) {
					this.impassableTerrain.add(node);
				}
				if (karbonite > 0) {
					this.karboniteDeposits.add(node);
				}
				this.updateNodeTag(j, i, tag, -1); // Init the nodes in nodeContentMap
			}
		}
		
		// Now check every unit and update the map
		for (int i = 0; i < initialUnits.size(); i++) {
			Unit u = initialUnits.get(i);
			int ux = u.location().mapLocation().getX();
			int uy = u.location().mapLocation().getY();
			if (u.team() == this.team) {
				System.out.println(u.id() + " : TEAM " + u.team() + " | " + this.team);

				this.updateNodeTag(ux, uy, 'w', u.id());
			} else {
				System.out.println(u.id() + " : TEAM " + u.team() + " | " + this.team);

				this.updateNodeTag(ux, uy, 'W', u.id());
			}
		}
		
		// Do preprocessing on karbonite
		this.initKarboniteBlobs();
	}
	
	public void updateUnits(VecUnit allUnits) {
		// Update unit tags every turn
		Set<MapNode> modified = new HashSet<MapNode>();
		Set<MapNode> original = new HashSet<MapNode>(); // Original PLUS nodes with new tags
		boolean ally;
		for (int i = 0; i < allUnits.size(); i++) {
			Unit u = allUnits.get(i);
			MapLocation unitLoc = u.location().mapLocation();
			ally = u.team() == this.team;
			
			char unitTag = this.unitLegend.get(u.unitType());
			this.updateNodeTag(unitLoc.getX(), unitLoc.getY(), ally ? unitTag : Character.toUpperCase(unitTag), u.id()); // Handles nodeContentMap updates
			modified.add(this.get(unitLoc.getX(), unitLoc.getY()));
		}
		// Iterate through all the stored MapNodes in nodeContentMap, and add to orig set
		for (Character tag : this.nodeContentMap.keySet()) {
			if (tag != '0') {
				ArrayList<MapNode> nodeList = this.nodeContentMap.get(tag);
				for (MapNode node : nodeList) {
					original.add(node);
				}
			}
		}
		original.removeAll(modified); // Get the difference between the original and the modified nodes
		for (MapNode node : original) {
			// Every Node here has had the unit destroyed or moved
			this.removeNodeTag(node.x, node.y);
		}
	}
	
	public void updateKarbonite() {
		// This logic works on the assumption that no new karbonite is created. Thus, only on Earth
		if (this.planet == Planet.Earth) {
			ArrayList<MapNode> toRemove = new ArrayList<MapNode>();
			for (MapNode node : this.karboniteDeposits) {
				try {
					int karbonite = (int) gc.karboniteAt(new MapLocation(this.planet, node.x, node.y));
					if (this.setKarbonite(node.x, node.y, karbonite, true)) {// This handles removal from deposites list
						toRemove.add(node);
					}
				} catch (RuntimeException ex) {
					// Do nothing if karbonite outside vision range
				}
			}
			// Now remove
			for (MapNode node: toRemove) {
				this.karboniteDeposits.remove(node);
			}
		} else {
			// Mars Logic
		}
	}
	
	/**
	 * Properly update the node in the map. Changes the tag of the node in map
	 * as well as the node's position in nodeContentMap
	 * @param x
	 * @param y
	 * @param newTag
	 */
	public void updateNodeTag(int x, int y, char newTag, int unitID) {
		// Get the node first
		MapNode node = map[y][x];
		char oldTag = node.getTag();
		if (this.nodeContentMap.containsKey(oldTag)) {
			// Remove 
			this.nodeContentMap.get(oldTag).remove(node);
		}
		if (this.nodeContentMap.containsKey(newTag)) {
			// Add node to map if key exists already
			this.nodeContentMap.get(newTag).add(node);
		} else {
			ArrayList<MapNode> nodeList = new ArrayList<MapNode>();
			nodeList.add(node);
			this.nodeContentMap.put(newTag, nodeList);
		}
		node.setTag(newTag);
		node.setUnitID(-1); // if the new tag is 0
		if (newTag != '0') {
			node.setUnitID(unitID);
			node.setPassable(false);
		}
	}
	
	public void removeNodeTag(int x, int y) {
		// Get the node first
		MapNode node = map[y][x];
		char oldTag = node.getTag();
		if (this.nodeContentMap.containsKey(oldTag)) {
			// Remove from map
			this.nodeContentMap.get(oldTag).remove(node);
		}
		node.setTag('0'); // Set to blank
		node.setUnitID(-1);
		this.nodeContentMap.get('0').add(node);
		node.setPassable(true);
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
	
	/**
	 * decrease the karbonite at the given node
	 * @param x
	 * @param y
	 * @param amount
	 * @param returnRemoveFlag Because this function affects the arraylist karboniteDeposits
	 *                         it is sometimes neccessary to be careful and not remove the node
	 *                         but rather return a flag that says it SHOULD be removed
	 * @return toRemove flag. If returnRemoveFlag is false, output is always false
	 */
	public boolean decreaseKarbonite(int x, int y, int amount, boolean returnRemoveFlag) {
		map[y][x].removeKarbonite(amount);
		if (!returnRemoveFlag && map[y][x].getKarbonite() <= 0) {
			this.karboniteDeposits.remove(this.map[y][x]);
		} else if (returnRemoveFlag && map[y][x].getKarbonite() <= 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Set the karbonite at the given node
	 * @param x
	 * @param y
	 * @param amount
	 * @param returnRemoveFlag Because this function affects the arraylist karboniteDeposits
	 *                         it is sometimes neccessary to be careful and not remove the node
	 *                         but rather return a flag that says it SHOULD be removed
	 * @return toRemove flag. If returnRemoveFlag is false, output is always false
	 */
	public boolean setKarbonite(int x, int y, int amount, boolean returnRemoveFlag) {
		map[y][x].setKarbonite(amount);
		if (!returnRemoveFlag && amount == 0) {
			this.karboniteDeposits.remove(this.map[y][x]);
		} else if (returnRemoveFlag && amount == 0) {
			return true;
		}
		return false;
	}
	
	public int getKarbonite(int x, int y) {
		return map[y][x].getKarbonite();
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
	 * Do a search for the nearest karbonite
	 */
	public MapLocation searchForKarbonite(int centerX, int centerY) {
		// Search by expanding rings
		int maxRadius = (int) Math.max(Math.max(this.width - 1 - centerX, centerX), Math.max(this.height - 1 - centerY, centerY));
			
		for (int radius = 1; radius <= maxRadius; radius++) {
//			System.out.println("Center (" + centerX + ", " + centerY + ") Radius: " + radius);
			// I hate how this is written
			for (int x = centerX - radius; x <= centerX + radius; x += 2*radius) {
				for (int y = centerY - radius; y < centerY + radius; y++) {
					if (this.isOnMap(x, y) && map[y][x].getKarbonite() > 0) {
						return new MapLocation(this.planet, x, y);
//						return new int[] {x, y};
					}
				}
			}
			for (int y = centerY - radius; y <= centerY + radius; y += 2*radius) {
				for (int x = centerX - radius; x < centerX + radius; x++) {
					if (this.isOnMap(x, y) && map[y][x].getKarbonite() > 0) {
						return new MapLocation(this.planet, x, y);
//						return new int[] {x, y};
					}
				}
			}
		}
		
		// Nothing found. Just be careful when handling these return values
		return null;
	}
	
	/**
	 * Does a BFS to first create groupings of karbonite, then orders them based on an important heuristic
	 * Score = (1 - n1 / nt) * k / a
	 * n1 = Number of nodes with 2 non orthogonal neighbours + Number with 1 neighbour 
	 * nt = Total Nodes
	 * k = total karbonite
	 * a = area of a circumscribing rectangle
	 */
	public void initKarboniteBlobs() {
		ArrayList<MapNode> visited = new ArrayList<MapNode>();
		ArrayDeque<MapNode> unvisited = new ArrayDeque<MapNode>();

		ArrayList<ArrayList<MapNode>> blobs = new ArrayList<ArrayList<MapNode>>();
		ArrayList<Double> blobScores = new ArrayList<Double>();
		ArrayList<MapNode> blob = new ArrayList<MapNode>();
		int maxx, maxy, minx, miny, numSparseNodes, numNeighbours, totalKarbonite; // For efficiency
		for (MapNode node : this.karboniteDeposits) {
			// Start of a new blob
			if (!visited.contains(node)) {
				blob = new ArrayList<MapNode>();
				unvisited.clear();
				unvisited.add(node);
				
				maxx = node.x;
				minx = node.x;
				maxy = node.y;
				miny = node.y;
				
				numSparseNodes = 0;
				totalKarbonite = 0;
				
				while (!unvisited.isEmpty()) {
					MapNode n = unvisited.removeFirst();
					totalKarbonite += n.getKarbonite();
					if (n.x < minx) minx = n.x;
					else if (n.x > maxx) maxx = n.x;
					if (n.y < miny) miny = n.y;
					else if (n.y > maxy) maxy = n.y;
					visited.add(n);
					blob.add(n);
					numNeighbours = 0;
					for (MapNode neighbour : this.karboniteDeposits) {
						// Karbonite within 1 move of another is considered the same blob
						if (!visited.contains(neighbour) && neighbour != n && Math.abs(neighbour.x - n.x) <= 1 && Math.abs(neighbour.y - n.y) <= 1) {
							numNeighbours++;
							unvisited.push(neighbour);
						}
					}
					
					if (numNeighbours == 1 || numNeighbours == 2) {
						numSparseNodes++;
					}
				}
				double score = (1.0 - (double) numSparseNodes / blob.size()) * totalKarbonite / ((maxx - minx + 1) * (maxy - miny + 1));
				// Insert in descending order
				int i = 0;
				try {
					while (blobScores.get(i) > score) i++;
				} catch (IndexOutOfBoundsException ex) {}
				blobs.add(i, blob);
				blobScores.add(i, score);
			}
		}
		
		this.karboniteBlobs = blobs;
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
	
	/**
	 * Gets the unit ids of a specific tag in order of closest to furthest from a target x, y
	 * Be careful to only call after calling update for that round
	 * @param x x coordinate of target
	 * @param y y coordinate of the target
	 * @param tag the tag of the type of unit you are searching for. Remembering that capital means enemy
	 * @return Arraylist of integers representing the unit id's of the units
	 * @return null return should be caught and means map has not been fully initialized
	 */
	public ArrayList<Integer> getClosestUnits(int x, int y, char tag) {
		if (!this.nodeContentMap.containsKey(tag)) {
			return null;
		}
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ArrayList<Integer> pathLengths = new ArrayList<Integer>();
		ArrayList<MapNode> relevantNodes = this.nodeContentMap.get(tag);
		MapNode[][] tempMap = new MapNode[this.height][this.width];
		for (int i = 0; i < this.map.length; i++) {
			for (int j = 0; j < this.map[0].length; j++) {
				tempMap[i][j] = new MapNode(this.map[i][j]);
			}
		}
		
		// Perform BFS from the target and fill the whole map with nodes
		ArrayDeque<MapNode> unvisited = new ArrayDeque<MapNode>();
		ArrayList<MapNode> visited = new ArrayList<MapNode>();
		ArrayList<MapNode> neighbours = new ArrayList<MapNode>();
		MapNode start = new MapNode(this.map[y][x]); // clone
		
		unvisited.add(start);
		MapNode current;
		// Should fill the temp map
		while (!unvisited.isEmpty()) {
			neighbours.clear();
			current = unvisited.removeFirst(); // Nodes in unvisited are cloes of nodes in falconmap
			
			// Get all the neighbours
			if (this.isOnMap(current.x, current.y + 1)) neighbours.add(tempMap[current.y + 1][current.x]); // Up
			if (this.isOnMap(current.x + 1, current.y)) neighbours.add(tempMap[current.y][current.x + 1]); // Right
			if (this.isOnMap(current.x, current.y - 1)) neighbours.add(tempMap[current.y - 1][current.x]); // Down
			if (this.isOnMap(current.x - 1, current.y)) neighbours.add(tempMap[current.y][current.x - 1]); // Left
			for (MapNode n : neighbours) {
				if ((n.isPassable() || n.getUnitID() > 0) && n.getParent() == null) {
					n.setParent(current); // Have node reference parent
					unvisited.addLast(n);
				}
			}
		}
		
		// Print map
//		System.out.println("TEMP MAP");
//		for (MapNode[] ma : tempMap) {
//			for (MapNode n : ma) {
//				System.out.print(n.parent);
//			}
//			System.out.println();
//		}
		
		// Iterate through all units of the given tag
		for (MapNode n : this.nodeContentMap.get(tag)) {
			MapNode backtrackCurrent = tempMap[n.y][n.x]; // Cloned node with parent reference
			int pathLength = 0;
			while (backtrackCurrent.parent != null) {
				backtrackCurrent = backtrackCurrent.parent;
				pathLength++;
			}	
			int i = 0;
			try { 
				while (pathLengths.get(i) < pathLength) i++;
			} catch (IndexOutOfBoundsException ex) {}
			pathLengths.add(i, pathLength);
			ret.add(i, n.getUnitID());
		}
		return ret;
	}
	
	public void printMap() {
		char t = '0';
		for (int i = 0; i < this.map.length; i++) {
			for (int j = 0; j < this.map[0].length; j++) {
//				System.out.println("(" + j + ", " + i + ")|" + this.map[j][i].isPassable() + "|" + this.map[j][i].getTag());
				t = this.map[i][j].getTag();
				if (!this.map[i][j].isPassable() && t == '0') t = 'X';
				System.out.print(t);
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

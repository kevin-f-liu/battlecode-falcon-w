import java.util.ArrayList;
import bc.*;

// Implement A* pathfinding algorithm
// Maintains its own map of nodes
public class PathFinder {
	private class AStarNode extends MapNode{
		// AStarNode is a MapNode with path references
		public AStarNode parentNode;
		public AStarNode nextNode;
		public int gCost = 0;
		public double hCost = 0;
		public double fCost = 0;
		
		public AStarNode(int x, int y, char content, boolean walkable) {
			super(x, y, 0, content, walkable);

			this.parentNode = null;
			this.nextNode = null;
		}
		
		public void setFCost() {
			this.fCost = this.gCost + this.hCost;
		}
	}
	
	public AStarNode[][] map;
	public Planet planet;
	public int height;
	public int width;
	
	public int startx;
	public int starty;
	public int endx;
	public int endy;
	public boolean targeting;
	public AStarNode current;

	private ArrayList<AStarNode> closedSet;
	private ArrayList<AStarNode> openSet;
	public ArrayList<AStarNode> path;
	
	public PathFinder(FalconMap map) {
		this.height = map.height;
		this.width = map.width;
		this.targeting = false;
		this.closedSet = new ArrayList<AStarNode>();
		this.openSet = new ArrayList<AStarNode>();
		initMap(map);
	}
	
	private void initMap(FalconMap map) {
		// Does a clone of a FalconMap 
		this.map = new AStarNode[height][width];
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				this.map[i][j] = new AStarNode(j, i, map.get(j, i).getTag(), map.isPassable(j, i));
			}
		}
	}
	
	public void updateMap(FalconMap map) {
		// Only updates the map, assuming the incoming map dimensions are the same
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				this.map[i][j].setPassable(map.isPassable(j, i));
				this.map[i][j].setTag(map.get(j, i).getTag());
			}
		}
	}
	
	public double getHDistance(AStarNode a, AStarNode b) {
		return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
	}
	
	private ArrayList<AStarNode> getNeighbours(int x, int y) {
		// Get every walkable neighbour
		ArrayList<AStarNode> neighbours = new ArrayList<AStarNode>();
		
		for (int i = y - 1; i <= y + 1; i ++) {
			for (int j = x - 1; j <= x + 1; j ++) {
				if (j != x || i != y) {
					if (j >= 0 && i >= 0 && i < this.height && j < this.width && this.map[i][j].isPassable()) {
						neighbours.add(this.map[i][j]);
					}
				}
			}
		}
		
		return neighbours;
	}
	
	public void recalculate(FalconMap newMap) {
		// When the map is updated
		updateMap(newMap);
		calculatePath(this.current.x, this.current.y, this.endx, this.endy);
	}
	
	public void target(int startx, int starty, int endx, int endy) {
		// Basic call, specifies current location
		this.targeting = true;
		if (this.map == null) {
			throw (new RuntimeException("You need to init a map first"));
		}
		calculatePath(startx, starty, endx, endy);
	}
	
	public void retarget(int x, int y) {
		// Same Map
		// Change target so must recalculate as well
		if (this.current == null) {
			throw (new RuntimeException("You can't retarget something that was never targeted in the first place"));
		}
		this.endx = x;
		this.endy = y;
		this.startx = this.current.x;
		this.starty = this.current.y;	
		calculatePath(this.startx, this.starty, this.endx, this.endy);
	}
	
	public MapLocation getTarget() {
		return new MapLocation(this.planet, endx, endy);
	}
	
	public boolean isTargeting() {
		return this.targeting;
	}
	
	public void advanceStep() {
		if (this.current == null) {
			throw (new RuntimeException("You must calculate a path first"));
		}
		if (this.current.nextNode == null) {
			// Done pathing
			this.targeting = false;
		} else {
			this.current = this.current.nextNode;
		}
	}
	
	public Direction nextStep() {
		// Returns the direction that should be moved to follow the path
		if (this.current == null) {
			throw (new RuntimeException("You must calculate a path first"));
		}
		
		if (this.current.nextNode == null) {
			// Done pathing
			this.targeting = false;
			return null;
		}
		
		AStarNode next = this.current.nextNode;
		
		Direction ret = null;
		// NOTE: This part is confusing because of the way BattleCode describes the arrays. 
		// (0, 0) is the bottom left in terms of playing, however, the map that is queried is returned with (0, 0) being
		// the top left. Therefore to translate the internal map to the actual map, we need to reverse the north south directions.
		if (this.current.y < next.y) {
			// North
			ret = Direction.North;
			if (this.current.x < next.x) {
				// Northeast
				ret = Direction.Northeast;
			} else if (this.current.x > next.x) {
				// Northwest
				ret = Direction.Northwest;
			}
		} else if (this.current.y > next.y) {
			// South
			ret = Direction.South;
			if (this.current.x < next.x) {
				// Southeast
				ret = Direction.Southeast;
			} else if (this.current.x > next.x) {
				// Southwest
				ret = Direction.Southwest;
			}
		} else if (this.current.x < next.x) {
			// East
			ret = Direction.East;
		} else if (this.current.x > next.x) {
			// West
			ret = Direction.West;
		} else {
			// This should never happen
			ret = Direction.Center;
		}
		return ret;
	}
	
	private void createPath(AStarNode startNode, AStarNode endNode) {
		// Fills the path list and links all elements together
		this.path = new ArrayList<AStarNode>();
		AStarNode current = endNode;
		this.path.add(current);
		while (current.x != startNode.x || current.y != startNode.y) {
			current.parentNode.nextNode = current;
			current = current.parentNode;
			this.path.add(0, current);
		}
		this.current = this.path.get(0);
	}
	
	public ArrayList<int[]> getPath() {
		// Debugging method that gets just the coordinates
		ArrayList<int[]> ret = new ArrayList<int[]>();
		for (AStarNode n : this.path) {
			ret.add(new int[] {n.x, n.y});
		}
		return ret;
	}
	
	public void calculatePath(int startx, int starty, int endx, int endy) {
		// Reset everything
		this.openSet = new ArrayList<AStarNode>();
		this.closedSet = new ArrayList<AStarNode>();
		this.path = new ArrayList<AStarNode>();
		
		this.startx = startx;
		this.starty = starty;
		this.endx = endx;
		this.endy = endy;
		
		AStarNode startNode = this.map[starty][startx];
		AStarNode endNode = this.map[endy][endx];
		AStarNode currentNode = startNode;
		currentNode.gCost = 0;
		currentNode.hCost = this.getHDistance(currentNode, endNode);
		this.openSet.add(currentNode);
		
		while (!this.openSet.isEmpty()) {
			// First find the best node in the open set, by fCost
			AStarNode bestNode = null;
			for (AStarNode n : this.openSet) {
				if (bestNode == null) {
					bestNode = n;
				}
				if (n.fCost < bestNode.fCost) {
					bestNode = n;
				}	
			}
			currentNode = bestNode;
			
			// Move to it and add to closedSet
			this.openSet.remove(bestNode);
			this.closedSet.add(bestNode);
			
			// Find all neighbouring nodes and add to open set if not in closed set or open set
			ArrayList<AStarNode> neighbours = this.getNeighbours(currentNode.x, currentNode.y);
			for (AStarNode n : neighbours) {
				if (!this.closedSet.contains(n) && !this.openSet.contains(n)) {
					this.openSet.add(n);
				}
				
				// Update the neighbours gcost and set it's parent should it be cheaper to go from the current node
				int gCostFromCurrent = currentNode.gCost + 1; // Always a cost of 1 to go anywhere
				if (n.gCost > gCostFromCurrent || n.gCost == 0) {
					n.parentNode = currentNode;
					n.gCost = gCostFromCurrent;
					n.hCost = this.getHDistance(n, endNode);
					n.setFCost();
				}
			}
			
			// If Current is at the end, that's it.
			if (currentNode.x == endNode.x && currentNode.y == endNode.y) {
				System.out.println("FOUND END");
				this.createPath(startNode, currentNode);
				break;
			}
		}
	}
}

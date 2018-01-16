import java.util.ArrayList;
import bc.Direction;

// Implement A* pathfinding algorithm
// Maintains its own map of nodes
public class PathFinder {
	// Node class to facilitate pathing
	private class Node {
		public int x;
		public int y;
		public Node parentNode;
		public Node nextNode;
		public char content;
		public int gCost = 0;
		public double hCost;
		public double fCost;
		
		public Node(int x, int y, char content) {
			this.x = x;
			this.y = y;
			this.content = content;
			this.parentNode = null;
			this.nextNode = null;
		}
		
		public boolean isWalkable() {
			return this.content == '0' || this.content == 'b';
		}
		
		public void setFCost() {
			this.fCost = this.gCost + this.hCost;
		}
		
		public String toString() {
			return "(" + this.x + ", " + this.y + ")";
		}
	}
	
	public Node[][] map;
	public int height;
	public int width;
	public int startx;
	public int starty;
	public int endx;
	public int endy;
	public boolean targeting;
	private ArrayList<Node> closedSet;
	private ArrayList<Node> openSet;
	public ArrayList<Node> path;
	private Node current;
	
	public PathFinder(char[][] map) {
		this.height = map.length;
		this.width = map[0].length;
		this.targeting = false;
		this.closedSet = new ArrayList<Node>();
		this.openSet = new ArrayList<Node>();
		createNodeMap(map);
	}
	
	private void createNodeMap(char[][] map) {
		this.map = new Node[height][width];
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				this.map[i][j] = new Node(j, i, map[i][j]);
			}
		}
	}
	
	public double getHDistance(Node a, Node b) {
		return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
	}
	
	private ArrayList<Node> getNeighbours(int x, int y) {
		// Get every walkable neighbour
		ArrayList<Node> neighbours = new ArrayList<Node>();
		
		for (int i = y - 1; i <= y + 1; i ++) {
			for (int j = x - 1; j <= x + 1; j ++) {
				if (j != x || i != y) {
					if (j >= 0 && i >= 0 && i < this.height && j < this.width && this.map[i][j].isWalkable()) {
						neighbours.add(this.map[i][j]);
					}
				}
			}
		}
		
		return neighbours;
	}
	
	public void updateMap(char[][] newMap) {
		createNodeMap(newMap);
	}
	
	public void recalculate(char[][] newMap) {
		// Heuristic to slightly modify path, to avoid recalculating everything
		// Currently just clears current state and reprocesses a path
		createNodeMap(newMap);
		calculatePath(this.current.x, this.current.y, this.endx, this.endy);
	}
	
	public void target(int startx, int staty, int endx, int endy) {
		// Basic call
		this.targeting = true;
		if (this.map == null) {
			throw (new RuntimeException("You need to init a map first"));
		}
		calculatePath(startx, startx, endx, endy);
	}
	
	public void retarget(int x, int y) {
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
	
	public boolean isTargeting() {
		return this.targeting;
	}
	
	public Direction advanceStep() {
		// Returns the direction that should be moved to follow the path
		if (this.current == null) {
			throw (new RuntimeException("You must calculate a path first"));
		}
		
		Node prev = new Node(this.current.x, this.current.y, this.current.content);
		if (this.current.nextNode == null) {
			// Done pathing
			this.targeting = false;
			return null;
		}
		this.current = this.current.nextNode;
		
		Direction ret = null;
		// NOTE: This part is confusing because of the way BattleCode describes the arrays. 
		// (0, 0) is the bottom left in terms of playing, however, the map that is queried is returned with (0, 0) being
		// the top left. Therefore to translate the internal map to the actual map, we need to reverse the north south directions.
		if (this.current.y > prev.y) {
			// North
			ret = Direction.North;
			if (this.current.x > prev.x) {
				// Northeast
				ret = Direction.Northeast;
			} else if (this.current.x < prev.x) {
				// Northwest
				ret = Direction.Northwest;
			}
		} else if (this.current.y < prev.y) {
			// South
			ret = Direction.South;
			if (this.current.x > prev.x) {
				// Southeast
				ret = Direction.Southeast;
			} else if (this.current.x < prev.x) {
				// Southwest
				ret = Direction.Southwest;
			}
		} else if (this.current.x > prev.x) {
			// East
			ret = Direction.East;
		} else if (this.current.x < prev.x) {
			// West
			ret = Direction.West;
		} else {
			ret = Direction.Center;
		}
		return ret;
	}
	
	private void createPath(Node startNode, Node endNode) {
		// Fills the path list and links all elements together
		this.path = new ArrayList<Node>();
		Node current = endNode;
		this.path.add(current);
		while (current.x != startNode.x && current.y != startNode.y) {
			current.parentNode.nextNode = current;
			current = current.parentNode;
			this.path.add(0, current);
		}
		this.current = this.path.get(0);
	}
	
	public ArrayList<int[]> getPath() {
		// Debugging method that gets just the coordinates
		ArrayList<int[]> ret = new ArrayList<int[]>();
		for (Node n : this.path) {
			ret.add(new int[] {n.x, n.y});
		}
		return ret;
	}
	
	public void calculatePath(int startx, int starty, int endx, int endy) {
		// Reset everything
		this.openSet = new ArrayList<Node>();
		this.closedSet = new ArrayList<Node>();
		this.path = new ArrayList<Node>();
		
		this.startx = startx;
		this.starty = starty;
		this.endx = endx;
		this.endy = endy;
		
		Node startNode = new Node(startx, starty, '0');
		Node endNode = new Node(endx, endy, '0');
		Node currentNode = startNode;
		currentNode.gCost = 0;
		currentNode.hCost = this.getHDistance(currentNode, endNode);
		this.openSet.add(currentNode);
		
		while (!this.openSet.isEmpty()) {
			// First find the best node in the open set, by fCost
			Node bestNode = null;
			for (Node n : this.openSet) {
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
			ArrayList<Node> neighbours = this.getNeighbours(currentNode.x, currentNode.y);
			for (Node n : neighbours) {
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

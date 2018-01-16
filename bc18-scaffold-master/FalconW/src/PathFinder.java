import java.util.ArrayList;

// Implement A* pathfinding algorithm
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
			return this.content == '0';
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
	private ArrayList<Node> closedSet;
	private ArrayList<Node> openSet;
	public ArrayList<Node> path;
	private Node current;
	
	public PathFinder(char[][] map, int height, int width) {
		this.map = new Node[height][width];
		this.height = height;
		this.width = width;
		this.closedSet = new ArrayList<Node>();
		this.openSet = new ArrayList<Node>();
		createNodeMap(map);
	}
	
	private void createNodeMap(char[][] map) {
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
	
	public void recalculate(char[][] newMap) {
		// Heuristic to slightly modify path, to avoid recalculating everything
	}
	
	public int[] advanceStep() {
		// Also returns the coordinates of the next move
		this.current = this.current.nextNode;
		int[] ret = {this.current.x, this.current.y};
		return ret;
	}
	
	private void createPath(Node startNode, Node endNode) {
		this.path = new ArrayList<Node>();
		Node current = endNode;
		this.path.add(current);
		while (current.x != startNode.x && current.y != startNode.y) {
			current.parentNode.nextNode = current;
			current = current.parentNode;
			this.path.add(0, current);
		}
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

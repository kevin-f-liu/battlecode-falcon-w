import bc.*;

public class MapNode {
	public int x;
	public int y;
	public int karbonite;
	public boolean passable;
	char contentTag; // '0' = Nothing, 'w' = worker, 'k' = knight, 'r' = ranger, 'm' = mage, 'f' = factory, 'r' = rocket, uppercase = Other team	
	int unitID; // The id of the unit on this node. -1 if no unit.
	
	MapNode parent; // Cannot be initialized in constructors
	
	public MapNode(int x, int y, int karbonite, char contentTag, int unitID, boolean passable) {
		this.x = x;
		this.y = y;
		this.karbonite = karbonite;
		this.contentTag = contentTag;
		this.passable = passable;
		this.unitID = unitID;
	}
	
	/**
	 * Copy constructor
	 * @param n
	 */
	public MapNode(MapNode n) {
		this.x = n.x;
		this.y = n.y;
		this.karbonite = n.karbonite;
		this.contentTag = n.contentTag;
		this.passable = n.passable;
		this.unitID = n.unitID;
	}
	
	public void setParent(MapNode n) {
		this.parent = n;
	}
	
	public MapNode getParent() {
		return this.parent;
	}
	
	public void setPassable(boolean b) {
		this.passable = b;
	}
	
	public boolean isPassable() {
		// Note that only karbonite and empty spaces are walkable
		// A node with no unit is '0'
		return this.passable;
	}
	
	public int getUnitID() {
		return this.unitID;
	}
	
	public void setUnitID(int unitID) {
		this.unitID = unitID;
	}
	
	public void removeKarbonite(int amount) {
		this.karbonite -= amount;
	}
	
	public void setKarbonite(int amount) {
		this.karbonite = amount;
	}
	
	public int getKarbonite() {
		return this.karbonite;
	}
	
	public void setTag(char newTag) {
		this.contentTag = newTag;
	}
	
	public char getTag() {
		return this.contentTag;
	}
	
	public boolean isEnemyUnit() {
		return Character.isUpperCase(this.contentTag);
	}
	
	public String toString() {
		// Prints the location and the Karbonite
		return "(" + this.x + ", " + this.y + " | " + this.contentTag + ")";
	}
}

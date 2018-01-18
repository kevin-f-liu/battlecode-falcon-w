import bc.*;

public class MapNode {
	public int x;
	public int y;
	public int karbonite;
	public boolean passable;
	char contentTag; // '0' = Nothing, '1' = Karbonite, 'w' = worker, 'k' = knight, 'r' = ranger, 'm' = mage, 'f' = factory, 'r' = rocket, uppercase = Other team	
	
	public MapNode(int x, int y, int karbonite, char contentTag, boolean passable) {
		this.x = x;
		this.y = y;
		this.karbonite = karbonite;
		this.contentTag = contentTag;
		this.passable = passable;
	}
	
	public boolean isWalkable() {
		// Note that only karbonite and empty spaces are walkable
		// A node with no unit is '0'
		return this.passable;
	}
	
	public void removeKarbonite(int amount) {
		this.karbonite -= amount;
	}
	
	public void setKarbonite(int amount) {
		this.karbonite = amount;
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

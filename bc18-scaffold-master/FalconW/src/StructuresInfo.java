import java.util.ArrayList;
import bc.*;

public class StructuresInfo {
	
	//================================================================================
	// Properties
	//================================================================================
	
	// Number of workers assigned per structure
	public static final int NUM_WORKERS_FOR_STRUCTURE = 4;
	
	private GameController gc;
	private FalconMap gameMap;
	private UnitType structureType;
	private Status structureStatus;
	private MapLocation structureLocation;
	private ArrayList<Integer> assignedWorkersID;
	
	//================================================================================
	// Constructor
	//================================================================================
	
	public StructuresInfo(GameController gc, FalconMap gameMap) {
		 this.gc = gc;
		 this.gameMap = gameMap;
		 this.structureType = null;
		 this.structureStatus = Status.Planned;
		 this.structureLocation = null;
		 this.assignedWorkersID = new ArrayList<Integer>();
	}
	
	//================================================================================
	// Accessors
	//================================================================================
	
	// The type of structure (factory or rocket)
	
	public UnitType getStructureType() {
		return this.structureType;
	}
	
	public void setStructureType(UnitType type) {
		this.structureType = type;
	}
	
	// The structure status (see Status enum definition)
	
	public Status getStructureStatus() {
		return this.structureStatus;
	}
	
	public void setStructureStatus(Status status) {
		this.structureStatus = status;
	}
	
	// The structure MapLocation
	
	public MapLocation getStructureLocation() {
		return this.structureLocation;
	}
	
	public void setStructureLocation(MapLocation loc) {
		this.structureLocation = loc;
	}
	
	// The ArrayList of IDs of workers currently assigned to build structure
	
	public ArrayList<Integer> getAssignedWorkersID() {
		return assignedWorkersID;
	}
	
	//================================================================================
	// Class functions
	//================================================================================
	
	/**
	 * Gets currently available squares around the structure for building workers to occupy
	 * 
	 * @return ArrayList of free adjacent MapLocations, not including Center
	 */
	public ArrayList<MapLocation> getFreeSquaresAroundStructure() {
		ArrayList<MapLocation> freeAdjSquares = new ArrayList<MapLocation>();
		
		if (structureLocation == null) {
			return freeAdjSquares;
		}
		
		for (Direction dir : Direction.values()) {
			int adjX = structureLocation.add(dir).getX();
			int adjY = structureLocation.add(dir).getY();
			
			if (dir != Direction.Center && gameMap.isOnMap(adjX, adjY) && gameMap.isPassable(adjX, adjY)) {
				freeAdjSquares.add(structureLocation.add(dir));
			}
		}
		
		return freeAdjSquares;
	}
	
	/**
	 * Assign worker to build current structure
	 * 
	 * @param Assigned worker's ID
	 */
	public void assignWorker(int id) {
		if (assignedWorkersID.size() < NUM_WORKERS_FOR_STRUCTURE) {
			assignedWorkersID.add(id);
		}
	}
	
	/**
	 * Advance structure status by 1 step (see Status enum)
	 */
	public void advanceProductionCycle() {
		switch (structureStatus) {
			case Planned: structureStatus = Status.Targeted;
				break;
			case Targeted: structureStatus = Status.Blueprinted;
				break;
			case Blueprinted: structureStatus = Status.Complete;
         		break;
			case Complete: structureStatus = Status.Complete;
				break;
			default: structureStatus = Status.Planned;
				break;
		}
	}
	
	/**
	 * Reset when build is complete
	 */
	public void completeStructure() {
		structureStatus = StructuresInfo.Status.Complete;
		assignedWorkersID.clear();
		System.out.println("Structure at " + structureLocation + " COMPLETE");
	}
	
	/**
	 * Direct worker to blueprint structure
	 * 
	 * @param Worker
	 */
	public void blueprint(Unit unit) {
		Direction dir = unit.location().mapLocation().directionTo(structureLocation);
		if (gc.canBlueprint(unit.id(), UnitType.Factory, dir)) {
			gc.blueprint(unit.id(), UnitType.Factory, dir);
	    	System.out.println("Blueprint complete at " + structureLocation.getX() + ", " + structureLocation.getY());
	    	setStructureStatus(Status.Blueprinted);
		}
	}

	/**
	 * Direct worker to build structure
	 * 
	 * @param Worker
	 */
	public void build(Unit unit) {
		Unit blueprint = gc.senseUnitAtLocation(structureLocation);
		if (gc.canBuild(unit.id(), blueprint.id())) {
			gc.build(unit.id(), blueprint.id());
			System.out.println("worker " + unit.id() + " building blueprint");
		}
		if (blueprint.structureIsBuilt() > 0) {
			completeStructure();
		}
	}
	
	//================================================================================
	// Custom enums
	//================================================================================
	
	// The build status of the structure
	public enum Status {
		Planned, // MapLocation set but not blueprinted
		Targeted, // Workers are currently moving towards structure
		Blueprinted, // Blueprinted but not built
		Complete // Structure is ready to use
	}
}

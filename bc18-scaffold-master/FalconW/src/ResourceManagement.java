import java.util.HashMap;

import bc.*;
import java.util.ArrayList;

// Class that serves to make Macro Decisions on Player's Resource Management
public class ResourceManagement {

	//================================================================================
	// Properties
	//================================================================================
	
	// Percentage of karbonite allocated for combat
	public static final int COMBAT_KARBONITE_PERCENT = 50;
	
	private GameController gc;
	private FalconMap gameMap;
	private ArrayList<StructuresInfo> structures;
	private HashMap<Integer, Unit> workers;
	private HashMap<Integer, Unit> factories;
	private HashMap<Integer, Unit> rockets;

	//================================================================================
	// Constructors
	//================================================================================
	
	public ResourceManagement(GameController gc) {
		this.gc = gc;
		this.gameMap = new FalconMap(gc);
		this.structures = new ArrayList<StructuresInfo>();
		this.workers = new HashMap<Integer, Unit>();
		this.factories = new HashMap<Integer, Unit>();
		this.rockets = new HashMap<Integer, Unit>();
	}
	
	//================================================================================
	// Public functions
	//================================================================================
	
	/**
	 * Updates resource manager with current round info
	 * 
	 * @param Game map
	 * @param worker units
	 * @param factory units
	 * @param rocket units
	 */
	public void updateRM(FalconMap gameMap,
			HashMap<Integer, Unit> workers,
			HashMap<Integer, Unit> factories,
			HashMap<Integer, Unit> rockets) {
		this.gameMap = gameMap;
		this.workers = workers;
		this.factories = factories;
		this.rockets = rockets;
	}

	/**
	 * Gets the maximum amount of karbonite to be used for combat
	 * 
	 * @return Amount of karbonite allowed
	 */
	public int karboniteForCombat() {
		return (isSavingForStructure() ? 0 : (int)gc.karbonite() * COMBAT_KARBONITE_PERCENT / 100);
	}

	/**
	 * Determines if currently building structures
	 * 
	 * @return True if there are structures to be built, false otherwise.
	 */
	public boolean isBuildingStructures() {
		for (StructuresInfo structure : structures) {
			if (structure.getStructureStatus() != StructuresInfo.Status.Complete) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Determines if currently saving up karbonite to build structures
	 * 
	 * @return True if there are structures planned but not yet blueprinted, false otherwise.
	 */
	public boolean isSavingForStructure() {
		for (StructuresInfo structure : structures) {
			if (structure.getStructureStatus() == StructuresInfo.Status.Planned 
				|| structure.getStructureStatus() == StructuresInfo.Status.Targeted) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Determines the amount of karbonite to save up to build future structure
	 * 
	 * @return Amount of karbonite to save
	 */
	public int karboniteToSave() {
		int amount = 0;
		for (StructuresInfo structure : structures) {
			if (structure.getStructureStatus() == StructuresInfo.Status.Planned 
					|| structure.getStructureStatus() == StructuresInfo.Status.Targeted) {
				amount += bc.bcUnitTypeBlueprintCost(structure.getStructureType());
			}
		}
		return amount;
	}
	
	/**
	 * Replicates workers from existing workers, if required
	 */
	public void replicate() {
		long workersToReplicate = Math.min(workersRequired(), gc.karbonite() / bc.bcUnitTypeReplicateCost(UnitType.Worker));
		System.out.println("Workers required: " + workersRequired());
		System.out.println("Workers to replicate: " + workersToReplicate);
		if (workersToReplicate < 1) {
			return;
		}

		for(Unit w : workers.values()) {
			// Check worker ability heat
			if (w.abilityHeat() < 10) {
				for (Direction dir : Direction.values()) {
					// Check if direction is valid
					if (gc.canReplicate(w.id(), dir)) {
						gc.replicate(w.id(), dir);
						System.out.println("Worker replicated.");
						workersToReplicate--;
						if (workersToReplicate < 1) {
							return;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Direct workers to build factories, if required
	 */
	public void buildFactories() {
		int factoriesNeeded = factoriesRequired();
		System.out.println("Factories needed: " + factoriesNeeded);
		if (factoriesNeeded <= 0) {
			return;
		}
		while (factoriesNeeded > 0) {
			StructuresInfo structureInfo = new StructuresInfo(gc, gameMap);
			structureInfo.setStructureType(UnitType.Factory);
			structureInfo.setStructureLocation(getOptimalFactoryLocation());
			structureInfo.setStructureStatus(StructuresInfo.Status.Planned);
			structures.add(structureInfo);
			factoriesNeeded--;
		}
	}

	/**
	 * Checks if unit is assigned to structure building duty
	 * 
	 * @param Unit to check
	 * 
	 * @return True if worker is assigned to build structure, false otherwise.
	 */
	public boolean isBuildingStructure(int id) {
		for (StructuresInfo structure : structures) {
			if (structure.getAssignedWorkersID().contains(id)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if workers need to be assigned to structure building duty
	 * 
	 * @return True if additional workers needed, false otherwise (or if not enough karbonite).
	 */
	public boolean needWorkersForStructure() {
		for (StructuresInfo structure : structures) {
			// If enough karbonite is available but structure is not blueprinted
			if (structure.getStructureStatus() == StructuresInfo.Status.Planned
					&& getFreeKarbonite() >= bc.bcUnitTypeBlueprintCost(structure.getStructureType())) {
				return true;
			}
			// If structure is in build but not enough workers are assigned to it
			if ( ( structure.getStructureStatus() == StructuresInfo.Status.Blueprinted
					|| structure.getStructureStatus() == StructuresInfo.Status.Targeted )
					&& structure.getAssignedWorkersID().size() < StructuresInfo.NUM_WORKERS_FOR_STRUCTURE) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Determines the next structure to work on
	 * 
	 * @param Unit ID
	 * 
	 * @return MapLocation of next structure that requires workers
	 */
	public MapLocation findNextStructureTarget(int workerID) {
		for (StructuresInfo structure : structures) {
			switch (structure.getStructureStatus()) {
				case Complete:
					break;
				case Targeted:
				case Blueprinted: 
					if (structure.getAssignedWorkersID().size() < StructuresInfo.NUM_WORKERS_FOR_STRUCTURE
						&& !structure.getFreeSquaresAroundStructure().isEmpty()) {
						structure.assignWorker(workerID);
						return structure.getFreeSquaresAroundStructure().get(0);
					}
				case Planned: 
					if (getFreeKarbonite() >= bc.bcUnitTypeBlueprintCost(structure.getStructureType())
						&& !structure.getFreeSquaresAroundStructure().isEmpty()) {
						structure.advanceProductionCycle();
						structure.assignWorker(workerID);
						return structure.getFreeSquaresAroundStructure().get(0);
					}
				default:
					break;
				
			}
		}
		return null;
	}
	
	/**
	 * Determines the structure that the unit is assigned to
	 * 
	 * @param Unit ID
	 * 
	 * @return StructuresInfo of the structure
	 */
	public StructuresInfo findAssignedStructure(int workerID) {
		for (StructuresInfo structure : structures) {
			if (structure.getAssignedWorkersID().contains(workerID)) {
				return structure;
			}
		}
		return null;
	}
	
	/**
	 * Assign structure task to indicated worker
	 * 
	 * @param Unit
	 */
	public void assignTask(Unit unit) {
		StructuresInfo structure = findAssignedStructure(unit.id());
		switch (structure.getStructureStatus()) {
			case Complete: structure.completeStructure();
				break;
			case Blueprinted: structure.build(unit);
				break;
			case Targeted: structure.blueprint(unit);
				break;
			case Planned: structure.blueprint(unit);
				break;
			default:
				break;
		}
	}
	
	//================================================================================
	// Private functions
	//================================================================================
	
	/**
	 * Determines the number of additional workers required according
	 * to round-dependent function.
	 * 
	 * @return Number of workers required.
	 */
	private int workersRequired() {
		// Function for the minimum number of workers required depends on planet
		int minWorkers = (gc.planet() == Planet.Earth ? (int)gc.round() / 30 + 10 : (int)gc.round() / 50 - 10);
		System.out.println("minWorkers: " + minWorkers + ", workers.size() = " + workers.size());
		return Math.max(0, minWorkers - workers.size());
	}
	
	/**
	 * Determines the number of additional factories required according
	 * to round-dependent function.
	 * 
	 * @return Number of factories required.
	 */
	private int factoriesRequired() {
		int minFactories = (int)gc.round() / 20 + 1;
		int factoriesNeeded = Math.max(0, minFactories - factories.size());
		for (int i = 0; i < structures.size(); i++) {
			if (structures.get(i).getStructureType() == UnitType.Factory 
					&& ( structures.get(i).getStructureStatus() == StructuresInfo.Status.Planned
					|| structures.get(i).getStructureStatus() == StructuresInfo.Status.Targeted ) ) {
				factoriesNeeded--;
			}
		}
		return factoriesNeeded;
	}
	
	/**
	 * Gets MapLocation array of 4 corners of the map
	 * 
	 * @return array of corner MapLocations
	 */
	private MapLocation[] getCorners() {
		MapLocation corners[] = new MapLocation[4];
		corners[0] = new MapLocation(Planet.Earth, 0, 0);
		corners[1] = new MapLocation(Planet.Earth, 0, gameMap.width - 1);
		corners[2] = new MapLocation(Planet.Earth, gameMap.height - 1, 0);
		corners[3] = new MapLocation(Planet.Earth, gameMap.height - 1, gameMap.width - 1);
		
		return corners;
	}
	
	/**
	 * Determines the MapLocation of the closest map corner to starting units
	 * 
	 * @return MapLocation of closest map corner
	 */
	private MapLocation getStartingCorner() {
		PlanetMap earthMap_initial = gc.startingMap(Planet.Earth);
		VecUnit startingUnits = earthMap_initial.getInitial_units();
		int minDistance = Integer.MAX_VALUE;
		MapLocation targetCorner = null;
		for (int i = 0; i < startingUnits.size(); i++) {
			if (startingUnits.get(i).team() == gc.team()) {
				for (MapLocation corner : getCorners()) {
					int distanceToCorner = (int) startingUnits.get(i).location().mapLocation().distanceSquaredTo(corner);
					if (minDistance > distanceToCorner) {
						minDistance = distanceToCorner;
						targetCorner = corner;
					}
				}	
			}
		}
		return targetCorner;
	}
	
	/**
	 * Determines if factory can be built on specified MapLocation
	 * 
	 * @param MapLocation to check
	 * 
	 * @return True if square meets criteria, false otherwise. 
	 */
	private boolean canBuildFactory(MapLocation target) {
		int x = target.getX();
		int y = target.getY();
		
		// Minimum number of free adjacent squares to build factory (including Center direction)
		int minFreeAdjSquares = 7;
		int freeAdjSquares = 0;
		
		// Make sure target square is valid
		if (!gameMap.isOnMap(x,y) || !gameMap.isPassable(x,y)) {
			return false;
		}
		
		// Do not build adjacent structures
		for (StructuresInfo structure : structures) {
			if (structure.getStructureLocation().isAdjacentTo(target) ) {
				return false;
			}
		}
		
		// Count number of free adjacent squares
		for (Direction dir : Direction.values()) {
			int adjX = target.add(dir).getX();
			int adjY = target.add(dir).getY();
			
			if (gameMap.isOnMap(adjX, adjY) && gameMap.isPassable(adjX, adjY)) {
				// Do not build adjacent structures (including check for enemy structures)
				if (gameMap.getNodetag(adjX, adjY) == 'f' || gameMap.getNodetag(adjX, adjY) == 'r') {
					return false;
				}
				freeAdjSquares++;
			}
		}
		
		if (freeAdjSquares >= minFreeAdjSquares) {
			return true;
		}
		return false;
	}
	
	/**
	 * Finds optimal location for new factory
	 * 
	 * @return MapLocation of the factory
	 */
	private MapLocation getOptimalFactoryLocation() {	
		MapLocation startingCorner = getStartingCorner();
		int diagonalDist = (int) Math.sqrt(Math.pow(gameMap.height, 2) + Math.pow(gameMap.width, 2)) + 1;

		// Loop through squares within incrementing radii of starting corner
		for (int dist = 1; dist <= diagonalDist; dist++) {
			// Add all squares within radius to list
			VecMapLocation squaresToCheck = gc.allLocationsWithin(startingCorner, dist*dist);
			for (int i = 0; i < squaresToCheck.size(); i++) {
				MapLocation target = squaresToCheck.get(i);
				// Only check squares in outer radius belt because others 
				// have been checked in previous iterations
				if (!target.isWithinRange((dist-1)*(dist-1) , startingCorner) && canBuildFactory(target)) {
					return target;
				}
			}
		}		
		return null;
	}
	
	/**
	 * Total karbonite - amount blocked off for targeted structure build
	 * 
	 * @return Amount of available karbonite
	 */
	private int getFreeKarbonite() {
		int reserved = 0;
		for (StructuresInfo structure : structures) {
			if (structure.getStructureStatus() == StructuresInfo.Status.Targeted) {
				reserved += bc.bcUnitTypeBlueprintCost(structure.getStructureType());
			}
		}
		return Math.max(0, (int) gc.karbonite() - reserved);
	}
}

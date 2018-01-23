import java.util.HashMap;

import bc.*;

// Class that serves to make Macro Decisions on Player's Resource Management
public class ResourceManagement {

	private GameController gc;
	private FalconMap gameMap;
	private HashMap<Integer, Unit> workers;
	private HashMap<Integer, Unit> factories;
	private HashMap<Integer, Unit> rockets;
	
	public static final int NUM_WORKERS_FOR_STRUCTURE = 4;
	public static final int COMBAT_KARBONITE_PERCENT = 50;
	private boolean structureQueued;
	private int workersForStructure;
	private MapLocation[] targetSquaresAroundStructure;
	private boolean savingForStructure;
	private int karboniteToSave;
	private int karboniteForCombat;
	
	public ResourceManagement(GameController gc, 
			FalconMap gameMap,
			HashMap<Integer, Unit> workers,
			HashMap<Integer, Unit> factories,
			HashMap<Integer, Unit> rockets) {
		this.gc = gc;
		this.gameMap = gameMap;
		this.workers = workers;
		this.factories = factories;
		this.rockets = rockets;
	}
	
	/**
	 * Checks if structure is in queue
	 * 
	 * @return True if structure is needed, false otherwise.
	 */
	public boolean structureQueued() {
		return this.structureQueued;
	}
	
	/**
	 * Gets the number of workers needed to build structure
	 * 
	 * @return The number of workers required
	 */
	public int workersForStructure() {
		return this.workersForStructure;
	}
	
	/**
	 * Decreases workersForStructure by 1
	 */
	public void decreaseWorkersForStructure() {
		if (this.workersForStructure > 0 ) {
			this.workersForStructure--;
		}
		else {
			this.structureQueued = false;
		}
	}
	
	/**
	 * Gets the target squares to which workers should be directed
	 * to build factory
	 * 
	 * @return Array of MapLocation of free adjacent squares to target location
	 */
	public MapLocation[] getSquaresAroundStructure() {
		return this.targetSquaresAroundStructure;
	}
	
	/**
	 * Checks if currently saving up karbonite to build structure
	 * 
	 * @return True if saving, false otherwise.
	 */
	public boolean isSavingForStructure() {
		return this.savingForStructure;
	}
	
	/**
	 * Gets the number of karbonite to save up to build future structure
	 * 
	 * @return Karbonite required
	 */
	public int karboniteToSave() {
		return this.karboniteToSave;
	}
	
	/**
	 * Gets the maximum amount of karbonite to be used for combat
	 * 
	 * @return Amount of karbonite allowed
	 */
	public int karboniteForCombat() {
		return (savingForStructure ? 0 : (int)gc.karbonite() * COMBAT_KARBONITE_PERCENT / 100);
	}
	
	/**
	 * Replicates requested number of workers from existing workers.
	 * 
	 * @param number of workers requested
	 */
	public void replicate(int workersRequested) {
		long workersToReplicate = Math.min(workersRequested, gc.karbonite() / bc.bcUnitTypeReplicateCost(UnitType.Worker));
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
						System.out.println("REPLICATED");
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
	 * Determines the number of additional workers required according
	 * to round-dependent function.
	 * 
	 * @return Number of workers required.
	 */
	public int workersRequired() {
		// Function for the minimum number of workers required depends on planet
		int minWorkers = (gc.planet() == Planet.Earth ? (int)gc.round() / 50 + 10 : (int)gc.round() / 50 - 10);
		return Math.max(0, minWorkers - workers.size());
	}
	
	/**
	 * Determines the number of additional factories required according
	 * to round-dependent function.
	 * 
	 * @return Number of factories required.
	 */
	public int factoriesRequired() {
		int minFactories = (int)gc.round() / 20 + 1;
		return Math.max(0, minFactories - factories.size());
	}
	
	/**
	 * Finds optimal location for new factory
	 * 
	 * @return MapLocation of the factory
	 */
	public MapLocation getOptimalFactoryLocation() {	
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
	 * Direct workers to build a factory at targeted MapLocation
	 * 
	 * @param Requested MapLocation
	 */
	public void startFactoryBuild(MapLocation target) {
		this.workersForStructure = NUM_WORKERS_FOR_STRUCTURE;
    	this.structureQueued = true;
    	this.targetSquaresAroundStructure = getFreeAdjSquares(target, NUM_WORKERS_FOR_STRUCTURE);
    	this.savingForStructure = true;
    	this.karboniteToSave = (int) bc.bcUnitTypeBlueprintCost(UnitType.Factory);
	}
	
	/**
	 * Direct worker to blueprint factory
	 * 
	 * @param Worker 
	 * @param Requested MapLocation
	 */
	public void blueprintFactory(Unit worker, MapLocation target) {
		Direction dir = target.directionTo(worker.location().mapLocation());
		if (gc.canBlueprint(worker.id(), UnitType.Factory, dir)) {
			gc.blueprint(worker.id(), UnitType.Factory, dir);
	    	this.savingForStructure = false;
	    	this.karboniteToSave = 0;
		}
	}
	
	/**
	 * Direct worker to build blueprint
	 * 
	 * @param Worker 
	 * @param MapLocation of Factory
	 */
	public void buildFactory(Unit worker, MapLocation target) {
		Unit blueprint = gc.senseUnitAtLocation(target);
		if (gc.canBuild(worker.id(), blueprint.id())) {
			gc.build(worker.id(), blueprint.id());
		}
	}
	
	/**
	 * Gets free adjacent squares to target MapLocation
	 * 
	 * @param Requested MapLocation
	 * @param Number of squares needed
	 * 
	 * @return MapLocation array of adjacent squares
	 */
	public MapLocation[] getFreeAdjSquares(MapLocation target, int numSquares) {
		MapLocation[] freeAdjSquares = new MapLocation[numSquares];
		int index = 0;
		
		for (Direction dir : Direction.values()) {
			int adjX = target.add(dir).getX();
			int adjY = target.add(dir).getY();
			
			if (dir != Direction.Center && gameMap.isOnMap(adjX, adjY) && gameMap.isPassable(adjX, adjY)) {
				freeAdjSquares[index] = target.add(dir);
				index++;
			}
			
			if (index >= freeAdjSquares.length) {
				return freeAdjSquares;
			}
		}
		
		while (index < freeAdjSquares.length) {
			freeAdjSquares[index] = null;
			index++;
		}
		
		return freeAdjSquares;
	}
	
	/**
	 * Gets MapLocation array of 4 corners of the map
	 * 
	 * @return array of corner MapLocations
	 */
	public MapLocation[] getCorners() {
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
	public MapLocation getStartingCorner() {
		PlanetMap earthMap_initial = gc.startingMap(Planet.Earth);
		VecUnit startingUnits = earthMap_initial.getInitial_units();
		int minDistance = Integer.MAX_VALUE;
		MapLocation targetCorner = null;
		for (int i = 0; i < startingUnits.size(); i++) {
			for (MapLocation corner : getCorners()) {
				int distanceToCorner = (int) startingUnits.get(i).location().mapLocation().distanceSquaredTo(corner);
				if (minDistance > distanceToCorner) {
					minDistance = distanceToCorner;
					targetCorner = corner;
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
	public boolean canBuildFactory(MapLocation target) {
		int x = target.getX();
		int y = target.getY();
		
		// Minimum number of free adjacent squares to build factory (including Center direction)
		int minFreeAdjSquares = 7;
		int freeAdjSquares = 0;
		
		// Make sure target square is valid
		if (!gameMap.isOnMap(x,y) || !gameMap.isPassable(x,y)) {
			return false;
		}
		
		// Count number of free adjacent squares
		for (Direction dir : Direction.values()) {
			int adjX = target.add(dir).getX();
			int adjY = target.add(dir).getY();
			
			if (gameMap.isOnMap(adjX, adjY) && gameMap.isPassable(adjX, adjY)) {
				// Do not build adjacent structures
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
	
}

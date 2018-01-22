import java.util.HashMap;

import bc.*;

// Class that serves to make Macro Decisions on Player's Resource Management
public class ResourceManagement {

	private GameController gc;
	private FalconMap gameMap;
	private HashMap<Integer, Unit> workers;
	private HashMap<Integer, Unit> factories;
	private HashMap<Integer, Unit> rockets;
	private boolean blueprinting;
	
	public ResourceManagement(GameController gc, 
			FalconMap gameMap,
			HashMap<Integer, Unit> workers,
			HashMap<Integer, Unit> factories,
			HashMap<Integer, Unit> rockets){
		this.gc = gc;
		this.gameMap = gameMap;
		this.workers = workers;
		this.factories = factories;
		this.rockets = rockets;
	}
	
	/**
	 * Checks if blueprinting is in queue
	 * 
	 * @return True if unit has intention to blueprint, false otherwise.
	 */
	public boolean isBlueprinting() {
		return blueprinting;
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
	 * Blueprint a factory at targeted MapLocation
	 * 
	 * @param Requested unit
	 * @param Unit's PathFinder
	 * @param Requested MapLocation
	 */
	public void blueprintFactory(Unit unit, PathFinder pf, MapLocation target) {
    	int x = unit.location().mapLocation().getX();
    	int y = unit.location().mapLocation().getY();
    	MapLocation adjSquare = null;
    
    	// algorithm tbd
    	
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

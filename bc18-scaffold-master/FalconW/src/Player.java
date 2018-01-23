import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import bc.*;

public class Player {	
	/**
	 * Main method for decision making
	 * foo() => Get data (Resource management, Macro decisions)
	 * foo2() => Workers move
	 * foo3() => Workers Action
	 * foo4() => Combat Units Actionable
	 * foo4() => Rangers Move
	 * foo5() => Rangers Action
	 * foo6() => Mages Move, etc...
	 * foo_n => endturn()
	 * @param args
	 */
	
	/**
	 * Takes every unit in vision range and update the map. Sensed here to reduce api calls and memory
	 * @param units
	 */
	public static void updateMap(FalconMap map, VecUnit units) {
		 map.updateUnits(units);
		 map.updateKarbonite();
	}
	
	public static void main(String[] args) {
        // Connect to the manager, starting the game
        GameController gc = new GameController();
 		FalconMap gameMap = new FalconMap(gc);
 		Team OUR_TEAM = gc.team();
 		Team ENEMY_TEAM;
 		// Set ENEMY_TEAM constant: this is required for some implemented classes.
 		if (gc.team() == Team.Blue){
 			ENEMY_TEAM = Team.Red;
 		} else{
 			ENEMY_TEAM = Team.Blue;
 		}
 		
 		HashMap<Integer, PathFinder> pathFinders = new HashMap<Integer, PathFinder>();
 		CombatManeuver combatDecisions = new CombatManeuver();
 		
 		  // Get our Units and put them in a wrapper.
 		 VecUnit units = gc.myUnits();
 		 PlayerUnits myUnits = new PlayerUnits(units, OUR_TEAM);

        while (true) {
            System.out.println("Current round: "+gc.round());
     
            // Update karbonite
            updateMap(gameMap, units);
            
           // Update Units
            units = gc.myUnits();
            myUnits.checkNewUnits(units);
            
            // Get Enemy Unit Locations.
            EnemyLocations enemies = new EnemyLocations(gc, ENEMY_TEAM);

            HashMap<Integer, Unit> workers = myUnits.getWorkers();
            HashMap<Integer, Unit> factories = myUnits.getFactories();
            HashMap<Integer, Unit> rockets = myUnits.getRockets();
            MapLocation factoryLocation = null;

            ResourceManagement rm = new ResourceManagement(gc, gameMap, workers, factories, rockets);
            MapLocation[] workerLocationsForFactory = new MapLocation[ResourceManagement.NUM_WORKERS_FOR_STRUCTURE];
            
            System.out.println("workersRequired: " + rm.workersRequired());
            
            if (rm.workersRequired() > 0) {
            	rm.replicate(rm.workersRequired());
            }
            
            if (rm.factoriesRequired() > 0) {
            	factoryLocation = rm.getOptimalFactoryLocation();
            	rm.startFactoryBuild(factoryLocation);
            	workerLocationsForFactory = rm.getSquaresAroundStructure();
            	System.out.println("factoryLocation" + factoryLocation.getX() + ", " + factoryLocation.getY());
            	for (int i = 0; i < workerLocationsForFactory.length; i++) {
            		System.out.println("workerLocationsForFactory: " + workerLocationsForFactory[i].getX() + ", " + workerLocationsForFactory[i].getY());
            	}
            }

            for (Unit unit: workers.values()) {
				MapLocation unitMapLocation = unit.location().mapLocation();
				/**
				 * Worker Portion
				 */
            	if (unit.unitType() == UnitType.Worker) {
            		// Get the worker's pathfinder
            		PathFinder pf;
            		if (!pathFinders.containsKey(new Integer(unit.id()))) {
            			// Add to map if new unit
            			System.out.println(unit.id() + ": new pathfinder");
            			pf = new PathFinder(gameMap);
            			pathFinders.put(unit.id(), pf);
            		} else {
            			pf = pathFinders.get(unit.id());
            		}
            		
            		System.out.println(unit.id() + ": now at " + unitMapLocation);
            		
            		if (rm.structureQueued() && rm.workersForStructure() > 0 && !pf.isTargetingStructure()) {
            			
            			MapLocation target = workerLocationsForFactory[ResourceManagement.NUM_WORKERS_FOR_STRUCTURE - rm.workersForStructure()];
            			System.out.println(unit.id() + ": target " + target.getX() + ", " + target.getY());
            			if (target != null) {
            				// // Aim for target MapLocation
            				// pf.updateMap(gameMap);
            				// System.out.println("map updated");
            				// pf.target(unitMapLocation.getX(), unitMapLocation.getY(), target.getX(), target.getY());
            				// System.out.println("target complete");
            				// pf.targetingStructure = true;
            			}
            			rm.decreaseWorkersForStructure();
            		}
            		
            		if (pf.isTargetingStructure()) {
            			// Not reached destination yet
            			if (pf.isTargeting()) {
            				// // Move
                			// Direction next = pf.nextStep();
                			// System.out.println(unit.id() + ": move direction " + next);
                			// if (next != null && gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), next)) {
                			//  	gc.moveRobot(unit.id(), next);
                			//	    pf.advanceStep();
                    		//	    System.out.println(unit.id() + ": moved to " + gc.unit(unit.id()).location().mapLocation());
                			// }
            			}
            			// Reached destination but have not blueprinted
            			else if (rm.isSavingForStructure()) {
            				rm.blueprintFactory(unit, factoryLocation);
            			}
            			// Blueprinted and build complete
            			else if (gc.senseUnitAtLocation(factoryLocation).health() == gc.senseUnitAtLocation(factoryLocation).maxHealth()) {
            				pf.targetingStructure = false;
            			}
            			// Blueprinted but not build
            			else {
            				rm.buildFactory(unit, factoryLocation);
            			}

            		}
            		else {
		        		// See if the worker is standing on karbonite that is is supposed to mine, if it is, mine it
		        		boolean mining = false;
		        		System.out.println(unit.id() + ": Standing on " + gc.karboniteAt(unitMapLocation) + "k");
		        		if (gc.karboniteAt(unitMapLocation) > 0 && unitMapLocation.equals(pf.getTarget())) {
		        			System.out.println(unit.id() + ": Mining karbonite | " + gc.karboniteAt(unitMapLocation));
		        			mining = true;
		        			gc.harvest(unit.id(), Direction.Center);
		        		} else if (unitMapLocation.equals(pf.getTarget())){
		        			// target is correct but it ran out
		        			if (gameMap.get(unitMapLocation.getX(), unitMapLocation.getY()).getTag() == '1') {
		        				System.out.println("Ran out of karbonite at " + unitMapLocation);
		        				gameMap.get(unitMapLocation.getX(), unitMapLocation.getY()).setTag('0'); // Clear the square
		        			}
		        		}
		        		
		        		// Find the nearest target if not mining
		        		if (!mining) {
		        			if (!pf.isTargeting()) {
		            			MapLocation target = gameMap.searchForKarbonite(unitMapLocation.getX(), unitMapLocation.getY());
		            			System.out.println(unit.id() + ": new target: " + target);
		            			pf.updateMap(gameMap);
		            			pf.target(unitMapLocation.getX(), unitMapLocation.getY(), target.getX(), target.getY());
		            			pf.printPath(unit.id()); // Print the path for debugging
		            		}
		        			
		        			// Move the unit if it didn't mine, either new target or old
		        			Direction next = pf.nextStep();
		        			System.out.println(unit.id() + ": move direction " + next);
		        			if (next != null && gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), next)) {
		        				gc.moveRobot(unit.id(), next);
		        				pf.advanceStep();
		            			System.out.println(unit.id() + ": moved to " + gc.unit(unit.id()).location().mapLocation());
		        			}
		        		}
            		}
            	} 	
            }
            
            /**
             * Code portion start for combat units
             */
            //Update PowerScores for combat decision making
            int[] powerScores = combatDecisions.updatePowerScore(myUnits, enemies);
           
            /**
             *  Decision Making for Rangers
             */
            HashMap<Integer, Unit> rangers = myUnits.getRangers();
            for (Unit ranger: rangers.values()){
            	MapLocation unitMapLocation = ranger.location().mapLocation();
            	// Get Unit's Pathfinder.
            	PathFinder pf;
        		if (!pathFinders.containsKey(new Integer(ranger.id()))) {
        			// Add to map if new unit
        			System.out.println(ranger.id() + ": new pathfinder");
        			pf = new PathFinder(gameMap);
        			pathFinders.put(ranger.id(), pf);
        		} else {
        			pf = pathFinders.get(ranger.id());
        		}
        		
        		System.out.println("Ranger " + ranger.id() + ": now at " + unitMapLocation);
        		
        		// See if the ranger can attack, if so, attack.
        		Unit target = combatDecisions.targetSelection(gc, ranger, ENEMY_TEAM);
        		if (target != null){
        			if (gc.canAttack(ranger.id(), target.id())){
        				gc.attack(ranger.id(), target.id());
        			}
        		}
        		
        		// Otherwise, seek enemy target and march towards it.
        		if (!pf.isTargeting()) {
        			MapLocation targetLoc = combatDecisions.seekTarget(ranger, gameMap, ENEMY_TEAM, pf, gc);
        			System.out.println("Ranger " + ranger.id() + ": new target location: " + target);
        			pf.updateMap(gameMap);
        			pf.target(unitMapLocation.getX(), unitMapLocation.getY(), targetLoc.getX(), targetLoc.getY());
        			pf.printPath(ranger.id()); // Print the path for debugging
        		}
    			
    			Direction next = pf.nextStep();
    			System.out.println("Ranger " + ranger.id() + ": move direction " + next);
    			if (next != null && gc.isMoveReady(ranger.id()) && gc.canMove(ranger.id(), next)) {
    				gc.moveRobot(ranger.id(), next);
    				pf.advanceStep();
        			System.out.println("Ranger " + ranger.id() + ": moved to " + gc.unit(ranger.id()).location().mapLocation());
    			}
    			
    			// See if the ranger can attack again, if so, attempt to attack again.
        		target = combatDecisions.targetSelection(gc, ranger, ENEMY_TEAM);
        		if (target != null){
        			if (gc.canAttack(ranger.id(), target.id())){
        				gc.attack(ranger.id(), target.id());
        			}
        		}
  
            }
            
            /**
             *  Decision Making for Mages
             */
            HashMap<Integer, Unit> mages = myUnits.getMages();
            for (Unit mage: mages.values()){
            	MapLocation unitMapLocation = mage.location().mapLocation();
            	// Get Unit's Pathfinder.
            	PathFinder pf;
        		if (!pathFinders.containsKey(new Integer(mage.id()))) {
        			// Add to map if new unit
        			System.out.println(mage.id() + ": new pathfinder");
        			pf = new PathFinder(gameMap);
        			pathFinders.put(mage.id(), pf);
        		} else {
        			pf = pathFinders.get(mage.id());
        		}
        		
        		System.out.println("Mage " + mage.id() + ": now at " + unitMapLocation);
        		
        		// See if the ranger can attack, if so, attack.
        		Unit target = combatDecisions.targetSelection(gc, mage, ENEMY_TEAM);
        		if (target != null){
        			if (gc.canAttack(mage.id(), target.id())){
        				gc.attack(mage.id(), target.id());
        			}
        		}
        		
        		// Otherwise, seek enemy target and march towards it.
        		if (!pf.isTargeting()) {
        			MapLocation targetLoc = combatDecisions.seekTarget(mage, gameMap, ENEMY_TEAM, pf, gc);
        			System.out.println("Mage " + mage.id() + ": new target location: " + target);
        			pf.updateMap(gameMap);
        			pf.target(unitMapLocation.getX(), unitMapLocation.getY(), targetLoc.getX(), targetLoc.getY());
        			pf.printPath(mage.id()); // Print the path for debugging
        		}
    			
    			Direction next = pf.nextStep();
    			System.out.println("Mage " + mage.id() + ": move direction " + next);
    			if (next != null && gc.isMoveReady(mage.id()) && gc.canMove(mage.id(), next)) {
    				gc.moveRobot(mage.id(), next);
    				pf.advanceStep();
        			System.out.println("Mage " + mage.id() + ": moved to " + gc.unit(mage.id()).location().mapLocation());
    			}
    			
    			// See if the ranger can attack again, if so, attempt to attack again.
        		target = combatDecisions.targetSelection(gc, mage, ENEMY_TEAM);
        		if (target != null){
        			if (gc.canAttack(mage.id(), target.id())){
        				gc.attack(mage.id(), target.id());
        			}
        		}
  
            }
            
            
            /**
             *  Decision Making for Knights
             */
            HashMap<Integer, Unit> knights = myUnits.getKnights();
            for (Unit knight: knights.values()){
            	MapLocation unitMapLocation = knight.location().mapLocation();
            	// Get Unit's Pathfinder.
            	PathFinder pf;
        		if (!pathFinders.containsKey(new Integer(knight.id()))) {
        			// Add to map if new unit
        			System.out.println(knight.id() + ": new pathfinder");
        			pf = new PathFinder(gameMap);
        			pathFinders.put(knight.id(), pf);
        		} else {
        			pf = pathFinders.get(knight.id());
        		}
        		
        		System.out.println("Knight " + knight.id() + ": now at " + unitMapLocation);
        		
        		// See if the ranger can attack, if so, attack.
        		Unit target = combatDecisions.targetSelection(gc, knight, ENEMY_TEAM);
        		if (target != null){
        			if (gc.canAttack(knight.id(), target.id())){
        				gc.attack(knight.id(), target.id());
        			}
        		}
        		
        		// Otherwise, seek enemy target and march towards it.
        		if (!pf.isTargeting()) {
        			MapLocation targetLoc = combatDecisions.seekTarget(knight, gameMap, ENEMY_TEAM, pf, gc);
        			System.out.println("Knight " + knight.id() + ": new target location: " + target);
        			pf.updateMap(gameMap);
        			pf.target(unitMapLocation.getX(), unitMapLocation.getY(), targetLoc.getX(), targetLoc.getY());
        			pf.printPath(knight.id()); // Print the path for debugging
        		}
    			
    			Direction next = pf.nextStep();
    			System.out.println("Knight " + knight.id() + ": move direction " + next);
    			if (next != null && gc.isMoveReady(knight.id()) && gc.canMove(knight.id(), next)) {
    				gc.moveRobot(knight.id(), next);
    				pf.advanceStep();
        			System.out.println("Knight " + knight.id() + ": moved to " + gc.unit(knight.id()).location().mapLocation());
    			}
    			
    			// See if the ranger can attack again, if so, attempt to attack again.
        		target = combatDecisions.targetSelection(gc, knight, ENEMY_TEAM);
        		if (target != null){
        			if (gc.canAttack(knight.id(), target.id())){
        				gc.attack(knight.id(), target.id());
        			}
        		}
  
            }
            
            
            
            
           
            gc.nextTurn();
        }
    }
}
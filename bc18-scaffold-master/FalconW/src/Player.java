import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import bc.*;

public class Player {	
	
	/**
	 * TODO:
	 * Implement karbonite patch checking for accessibility
	 * Implement Area Accessibility
	 * Implement karbonite patch targetting and then fine targetting
	 * 
	 */
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
 		} else {
 			ENEMY_TEAM = Team.Blue;
 		}
 		
 		HashMap<Integer, PathFinder> pathFinders = new HashMap<Integer, PathFinder>();
 		CombatManeuver combatDecisions = new CombatManeuver();
 		ResourceManagement rm = new ResourceManagement(gc);
 		
        while (true) {
            System.out.println("Current round: "+gc.round());
            
            VecUnit units = gc.units(); // Grab all units
            VecUnit myVecUnits = gc.myUnits(); // Grab just our units
            PlayerUnits myUnits = new PlayerUnits(myVecUnits, OUR_TEAM);
            updateMap(gameMap, units); // Update unit locations and karbonite
            myUnits.checkNewUnits(units); // Update new unit references
            EnemyLocations enemies = new EnemyLocations(gc, ENEMY_TEAM); // grab a list of enemy locations
            
            // TEST
            System.out.println("trying to get closest units");
            System.out.println(gameMap.getClosestUnits(0, 0, 'w'));
            
            // Mappings storing each type of unit
            HashMap<Integer, Unit> workers = myUnits.getWorkers();
            HashMap<Integer, Unit> factories = myUnits.getFactories();
            HashMap<Integer, Unit> rockets = myUnits.getRockets();
            MapLocation factoryLocation = null;
            rm.updateRM(gameMap, workers, factories, rockets); // Update the resource manager
            
            MapLocation[] workerLocationsForFactory = new MapLocation[ResourceManagement.NUM_WORKERS_FOR_STRUCTURE]; // Init to 4 locations per factory
            
            System.out.println("workersRequired: " + rm.workersRequired());
            
            /**
             * Replication Logic
             */
            if (rm.workersRequired() > 0) {
            	rm.replicate(rm.workersRequired());
            }
            
            /**
             * Decide when to build factories
             */
            if (rm.factoriesRequired() > 0) {
            	rm.startFactoryBuild();
            	workerLocationsForFactory = rm.getSquaresAroundStructure();
            }

            /**
             * Worker logic
             * 1. Init pathfinder for each worker
             * 2. Check that a structure needs to be build/build it
             * 3. Go mine
             */
            for (Unit unit: workers.values()) {
				MapLocation unitMapLocation = unit.location().mapLocation();
        		System.out.println(unit.id() + ": now at " + unitMapLocation);
        		PathFinder pf; // Worker pathfinder init or get
        		if (!pathFinders.containsKey(new Integer(unit.id()))) {
        			// Add to map if new unit
        			System.out.println(unit.id() + ": new pathfinder");
        			pf = new PathFinder(gameMap);
        			pathFinders.put(unit.id(), pf);
        		} else {
        			pf = pathFinders.get(unit.id());
        		}
            	
        		// Target the worker's pathfinder to the build location
        		if (rm.structureQueued() && rm.workersForStructure() > 0 && !rm.isBuildingStructure(unit)) {
        			MapLocation target = workerLocationsForFactory[ResourceManagement.NUM_WORKERS_FOR_STRUCTURE - rm.workersForStructure() * factories.size()];
        			System.out.println(unit.id() + ": build target (" + target.getX() + ", " + target.getY() + ")");
        			if (target != null) {
        				 pf.updateMap(gameMap);
        				 pf.target(unitMapLocation.getX(), unitMapLocation.getY(), target.getX(), target.getY());
        				 rm.setWorkerToBuild(unit);
        			}
        			rm.decreaseWorkersForStructure();
        		}
            	
        		// Logic to handle travel and building
        		if (rm.isBuildingStructure(unit)) {
        			System.out.println(unit.id() + ": assigned to BUILD");
        			// Advancing with the pathfinder
        			if (pf.isTargeting()) {
        				 // Move
            			 Direction next = pf.nextStep();
            			 if (next != null && gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), next)) {
            			  	gc.moveRobot(unit.id(), next);
            				pf.advanceStep();
            			 }
        			}
        			// Reached destination but have not blueprinted
        			else if (rm.isSavingForStructure()) {
        				System.out.println(unit.id() + ": reached destination for structure, blueprinting " + gc.unit(unit.id()).location().mapLocation());
        				rm.blueprintFactory(unit);
        			}
        			// Build complete
        			else if (rm.getCurrentStructure().structureIsBuilt() > 0) {
        				rm.completeStructure();
        				System.out.println("Structure completed");
            		}
        			// Blueprinted but not build
        			else {
        				System.out.print(unit.id() + ": building factory");
        				rm.buildFactory(unit, factoryLocation);
        			}
        		} 
        		
        		// Mining logic
        		if (!rm.isBuildingStructure(unit)) {
        			System.out.println(unit.id() + ": assigned to MINE");
	        		// See if the worker is standing on karbonite that it is supposed to mine, if it is, mine it
            		boolean mining = false;
            		System.out.println(unit.id() + ": Standing on " + gc.karboniteAt(unitMapLocation) + "k");
            		
            		// Mining
            		if (gc.karboniteAt(unitMapLocation) > 0 && unitMapLocation.equals(pf.getTarget())) {
            			System.out.println(unit.id() + ": Mining karbonite | " + gc.karboniteAt(unitMapLocation));
            			mining = true;
            			gc.harvest(unit.id(), Direction.Center);
            			gameMap.decreaseKarbonite(unitMapLocation.getX(), unitMapLocation.getY(), (int) unit.workerHarvestAmount(), false);
            			if (gc.karboniteAt(unitMapLocation) == 0) {
            				System.out.println("Ran out of karbonite at " + unitMapLocation);
            			}
            		}
            		
            		// Not mining
            		if (!mining) {
            			// Perform check that target karbonite is still there. If it isn't, search again
            			boolean targetKarboniteGone = false;
            			try {
	            			if (gc.karboniteAt(pf.getTarget()) == 0) {
	            				targetKarboniteGone = true;
	            			}
            			} catch (RuntimeException ex) {} // Don't care about checking a location outside vision
            			if (!pf.isTargeting() || targetKarboniteGone) {
                			MapLocation target = gameMap.searchForKarbonite(unitMapLocation.getX(), unitMapLocation.getY());
                			if (target == null) System.out.println(unit.id() + ": COULDN'T FIND TARGET");
                			else System.out.println(unit.id() + ": new target: " + target);
                			
                			pf.updateMap(gameMap);
                			boolean getTarget = pf.target(unitMapLocation.getX(), unitMapLocation.getY(), target.getX(), target.getY());
                			if (!getTarget) {
                				System.out.println(unit.id() + " messed up targetting");
                			}
                			pf.printPath(unit.id()); // Print the path for debugging
                		}
            			
            			
            			if (pf.isTargeting()) {
            				pf.recalculate(gameMap); // Update the game map for all the new units
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
            
//            /**
//             * Code portion start for combat units
//             */
//            //Update PowerScores for combat decision making
//            int[] powerScores = combatDecisions.updatePowerScore(myUnits, enemies);
//           
//            /**
//             *  Decision Making for Rangers
//             */
//            HashMap<Integer, Unit> rangers = myUnits.getRangers();
//            for (Unit ranger: rangers.values()){
//            	MapLocation unitMapLocation = ranger.location().mapLocation();
//            	// Get Unit's Pathfinder.
//            	PathFinder pf;
//        		if (!pathFinders.containsKey(new Integer(ranger.id()))) {
//        			// Add to map if new unit
//        			System.out.println(ranger.id() + ": new pathfinder");
//        			pf = new PathFinder(gameMap);
//        			pathFinders.put(ranger.id(), pf);
//        		} else {
//        			pf = pathFinders.get(ranger.id());
//        		}
//        		
//        		System.out.println("Ranger " + ranger.id() + ": now at " + unitMapLocation);
//        		
//        		// See if the ranger can attack, if so, attack.
//        		Unit target = combatDecisions.targetSelection(gc, ranger, ENEMY_TEAM);
//        		if (target != null){
//        			if (gc.canAttack(ranger.id(), target.id())){
//        				gc.attack(ranger.id(), target.id());
//        			}
//        		}
//        		
//        		// Otherwise, seek enemy target and march towards it.
//        		if (!pf.isTargeting()) {
//        			MapLocation targetLoc = combatDecisions.seekTarget(ranger, gameMap, ENEMY_TEAM, pf, gc);
//        			System.out.println("Ranger " + ranger.id() + ": new target location: " + target);
//        			pf.updateMap(gameMap);
//        			pf.target(unitMapLocation.getX(), unitMapLocation.getY(), targetLoc.getX(), targetLoc.getY());
//        			pf.printPath(ranger.id()); // Print the path for debugging
//        		}
//    			
//    			Direction next = pf.nextStep();
//    			System.out.println("Ranger " + ranger.id() + ": move direction " + next);
//    			if (next != null && gc.isMoveReady(ranger.id()) && gc.canMove(ranger.id(), next)) {
//    				gc.moveRobot(ranger.id(), next);
//    				pf.advanceStep();
//        			System.out.println("Ranger " + ranger.id() + ": moved to " + gc.unit(ranger.id()).location().mapLocation());
//    			}
//    			
//    			// See if the ranger can attack again, if so, attempt to attack again.
//        		target = combatDecisions.targetSelection(gc, ranger, ENEMY_TEAM);
//        		if (target != null){
//        			if (gc.canAttack(ranger.id(), target.id())){
//        				gc.attack(ranger.id(), target.id());
//        			}
//        		}
//  
//            }
//            
//            /**
//             *  Decision Making for Mages
//             */
//            HashMap<Integer, Unit> mages = myUnits.getMages();
//            for (Unit mage: mages.values()){
//            	MapLocation unitMapLocation = mage.location().mapLocation();
//            	// Get Unit's Pathfinder.
//            	PathFinder pf;
//        		if (!pathFinders.containsKey(new Integer(mage.id()))) {
//        			// Add to map if new unit
//        			System.out.println(mage.id() + ": new pathfinder");
//        			pf = new PathFinder(gameMap);
//        			pathFinders.put(mage.id(), pf);
//        		} else {
//        			pf = pathFinders.get(mage.id());
//        		}
//        		
//        		System.out.println("Mage " + mage.id() + ": now at " + unitMapLocation);
//        		
//        		// See if the ranger can attack, if so, attack.
//        		Unit target = combatDecisions.targetSelection(gc, mage, ENEMY_TEAM);
//        		if (target != null){
//        			if (gc.canAttack(mage.id(), target.id())){
//        				gc.attack(mage.id(), target.id());
//        			}
//        		}
//        		
//        		// Otherwise, seek enemy target and march towards it.
//        		if (!pf.isTargeting()) {
//        			MapLocation targetLoc = combatDecisions.seekTarget(mage, gameMap, ENEMY_TEAM, pf, gc);
//        			System.out.println("Mage " + mage.id() + ": new target location: " + target);
//        			pf.updateMap(gameMap);
//        			pf.target(unitMapLocation.getX(), unitMapLocation.getY(), targetLoc.getX(), targetLoc.getY());
//        			pf.printPath(mage.id()); // Print the path for debugging
//        		}
//    			
//    			Direction next = pf.nextStep();
//    			System.out.println("Mage " + mage.id() + ": move direction " + next);
//    			if (next != null && gc.isMoveReady(mage.id()) && gc.canMove(mage.id(), next)) {
//    				gc.moveRobot(mage.id(), next);
//    				pf.advanceStep();
//        			System.out.println("Mage " + mage.id() + ": moved to " + gc.unit(mage.id()).location().mapLocation());
//    			}
//    			
//    			// See if the ranger can attack again, if so, attempt to attack again.
//        		target = combatDecisions.targetSelection(gc, mage, ENEMY_TEAM);
//        		if (target != null){
//        			if (gc.canAttack(mage.id(), target.id())){
//        				gc.attack(mage.id(), target.id());
//        			}
//        		}
//  
//            }
//            
//            
//            /**
//             *  Decision Making for Knights
//             */
//            HashMap<Integer, Unit> knights = myUnits.getKnights();
//            for (Unit knight: knights.values()){
//            	MapLocation unitMapLocation = knight.location().mapLocation();
//            	// Get Unit's Pathfinder.
//            	PathFinder pf;
//        		if (!pathFinders.containsKey(new Integer(knight.id()))) {
//        			// Add to map if new unit
//        			System.out.println(knight.id() + ": new pathfinder");
//        			pf = new PathFinder(gameMap);
//        			pathFinders.put(knight.id(), pf);
//        		} else {
//        			pf = pathFinders.get(knight.id());
//        		}
//        		
//        		System.out.println("Knight " + knight.id() + ": now at " + unitMapLocation);
//        		
//        		// See if the ranger can attack, if so, attack.
//        		Unit target = combatDecisions.targetSelection(gc, knight, ENEMY_TEAM);
//        		if (target != null){
//        			if (gc.canAttack(knight.id(), target.id())){
//        				gc.attack(knight.id(), target.id());
//        			}
//        		}
//        		
//        		// Otherwise, seek enemy target and march towards it.
//        		if (!pf.isTargeting()) {
//        			MapLocation targetLoc = combatDecisions.seekTarget(knight, gameMap, ENEMY_TEAM, pf, gc);
//        			System.out.println("Knight " + knight.id() + ": new target location: " + target);
//        			pf.updateMap(gameMap);
//        			pf.target(unitMapLocation.getX(), unitMapLocation.getY(), targetLoc.getX(), targetLoc.getY());
//        			pf.printPath(knight.id()); // Print the path for debugging
//        		}
//    			
//    			Direction next = pf.nextStep();
//    			System.out.println("Knight " + knight.id() + ": move direction " + next);
//    			if (next != null && gc.isMoveReady(knight.id()) && gc.canMove(knight.id(), next)) {
//    				gc.moveRobot(knight.id(), next);
//    				pf.advanceStep();
//        			System.out.println("Knight " + knight.id() + ": moved to " + gc.unit(knight.id()).location().mapLocation());
//    			}
//    			
//    			// See if the ranger can attack again, if so, attempt to attack again.
//        		target = combatDecisions.targetSelection(gc, knight, ENEMY_TEAM);
//        		if (target != null){
//        			if (gc.canAttack(knight.id(), target.id())){
//        				gc.attack(knight.id(), target.id());
//        			}
//        		}
//  
//            }
//            
            
            
            
            System.gc();
            gc.nextTurn();
        }
    }
}
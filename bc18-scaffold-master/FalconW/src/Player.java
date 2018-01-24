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
 		} else{
 			ENEMY_TEAM = Team.Blue;
 		}
 		
 		HashMap<Integer, PathFinder> pathFinders = new HashMap<Integer, PathFinder>();
 		CombatManeuver combatDecisions = new CombatManeuver();

        while (true) {
            System.out.println("Current round: "+gc.round());
            
            // Get our Units and put them in a wrapper.
            VecUnit units = gc.units();
            VecUnit myVecUnits = gc.myUnits();
            // Update karbonite
            updateMap(gameMap, units);
            
            PlayerUnits myUnits = new PlayerUnits(myVecUnits, OUR_TEAM);
            
            // Get Enemy Unit Locations.
            EnemyLocations enemies = new EnemyLocations(gc, ENEMY_TEAM);

            HashMap<Integer, Unit> workers = myUnits.getWorkers();
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
            		
            		// See if the worker is standing on karbonite that is is supposed to mine, if it is, mine it
            		boolean mining = false;
            		System.out.println(unit.id() + ": Standing on " + gc.karboniteAt(unitMapLocation) + "k");
            		if (gc.karboniteAt(unitMapLocation) > 0 && unitMapLocation.equals(pf.getTarget())) {
            			System.out.println(unit.id() + ": Mining karbonite | " + gc.karboniteAt(unitMapLocation));
            			mining = true;
            			gc.harvest(unit.id(), Direction.Center);
            			if (gc.karboniteAt(unitMapLocation) == 0) {
            				System.out.println("Ran out of karbonite at " + unitMapLocation);
            			}
            		}
            		
            		// Find the nearest target if not mining
            		if (!mining) {
            			System.out.println(unit.id() + ": Not Mining");
            			// Perform check that target karbonite is still there. If it isn't, search again
            			boolean targetKarboniteGone = false;
            			if (gc.karboniteAt(pf.getTarget()) == 0) {
            				targetKarboniteGone = true;
            			}
            			if (!pf.isTargeting() || targetKarboniteGone) {
                			MapLocation target = gameMap.searchForKarbonite(unitMapLocation.getX(), unitMapLocation.getY());
                			if (target == null) System.out.println("NO TARGET");
                			System.out.println(unit.id() + ": new target: " + target);
                			pf.updateMap(gameMap);
                			boolean getTarget = pf.target(unitMapLocation.getX(), unitMapLocation.getY(), target.getX(), target.getY());
                			if (!getTarget) {
                				System.out.println(unit.id() + " messed up targetting");
                			}
                			pf.printPath(unit.id()); // Print the path for debugging
                		}
            			
            			// Move the unit if it didn't mine, either new target or old
            			pf.recalculate(gameMap);
            			if (pf.isTargeting()) {
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
            
            
            
            
            
            System.gc();
            gc.nextTurn();
        }
    }
}

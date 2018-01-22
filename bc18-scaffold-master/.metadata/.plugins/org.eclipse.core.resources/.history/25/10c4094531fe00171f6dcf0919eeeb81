import java.util.ArrayList;

import bc.*;

public class CombatManeuver {

	private ArrayList<Unit> playerCombatUnits;
	private ArrayList<Unit> enemyCombatUnits;
	private final int KNIGHT_POWER = 2; // These can be learned automatically exdee
	private final int MAGE_POWER = 4;
	private final int RANGER_POWER = 6;
	private int playerPowerScore;
	private int enemyPowerScore;

	public CombatManeuver() {
		playerPowerScore = 0;
		enemyPowerScore = 0;
		playerCombatUnits = new ArrayList<Unit>();
		enemyCombatUnits = new ArrayList<Unit>();
	}

	/**
	 * Updates the Power Score of our team, and the power score of enemy team.
	 * @param ourUnits
	 * @param enemies
	 * @return int[] containing playerPowerScore, enemyPowerScore.
	 */
	public int[] updatePowerScore(PlayerUnits ourUnits, EnemyLocations enemies) {
		PlayerUnits enemyUnits = enemies.getEnemyUnits();

		for (Unit u : ourUnits.getMages().values()) {
			playerPowerScore += MAGE_POWER;
		}
		for (Unit u : ourUnits.getKnights().values()) {
			playerPowerScore += KNIGHT_POWER;
		}
		for (Unit u : ourUnits.getRangers().values()) {
			playerPowerScore += RANGER_POWER;
		}

		for (Unit u : enemyUnits.getMages().values()) {
			enemyPowerScore += MAGE_POWER;
		}
		for (Unit u : enemyUnits.getKnights().values()) {
			enemyPowerScore += KNIGHT_POWER;
		}
		for (Unit u : enemyUnits.getRangers().values()) {
			enemyPowerScore += RANGER_POWER;
		}
		
		return new int[] {playerPowerScore, enemyPowerScore};
	}

	/**
	 * Method that returns the best target to attack within the attackable
	 * units.
	 * 
	 * @return Best possible unit to attack, false otherwise.
	 */
	public Unit targetSelection(GameController gc, Unit unit, Team enemyTeam) {

		EnemyLocations enemies = new EnemyLocations(gc, enemyTeam);

		ArrayList<Unit> possibleTargets = enemies.attackableList(gc, unit);
		ArrayList<Unit> withinRangeOf = enemies.isWithinAttackRange(unit);

		// Check that this Unit can attack another Unit
		if (possibleTargets.isEmpty() == false) {

			// First attack units that can attack the current Unit.
			for (Unit u : withinRangeOf) {
				if (possibleTargets.contains(u) && gc.canAttack(unit.id(), u.id())) {
					return u;
				}
			}

			// Then attack other combat units.
			for (Unit u : possibleTargets) {
				if (u.unitType() == UnitType.Knight || u.unitType() == UnitType.Mage || u.unitType() == UnitType.Ranger
						|| gc.canAttack(unit.id(), u.id())) {
					return u;
				}
			}

			// Attack any possible targets.
			for (Unit u : possibleTargets) {
				if (gc.canAttack(unit.id(), u.id())) {
					return u;
				}
			}

		}

		return null;
	}

	public void setDefensiveMode(Unit unit, GameController gc) {

	}

	public void setAgressiveMode(Unit unit, GameController gc) {

	}

}

import java.util.HashMap;

import bc.*;

public class PlayerUnits {

	private HashMap<Integer, Unit> allUnits, workers, knights, rangers, mages, healers, factories, rockets;
	
	public PlayerUnits(VecUnit units){
		allUnits = new HashMap<Integer, Unit>();
		workers = new HashMap<Integer, Unit>();
		knights = new HashMap<Integer, Unit>();
		rangers = new HashMap<Integer, Unit>();
		mages = new HashMap<Integer, Unit>();
		healers = new HashMap<Integer, Unit>();
		factories = new HashMap<Integer, Unit>();
		rockets = new HashMap<Integer, Unit>();
		
		for (int i = 0; i < units.size(); i++) {
		   Unit unit = units.get(i);
		   
		   if (unit.unitType() == UnitType.Worker){
			   workers.put(unit.id(), unit);
			   allUnits.put(unit.id(), unit);
		   } else if (unit.unitType() == UnitType.Knight){
			   knights.put(unit.id(), unit);
			   allUnits.put(unit.id(), unit);
		   }else if (unit.unitType() == UnitType.Ranger){
			   rangers.put(unit.id(), unit);
			   allUnits.put(unit.id(), unit);
		   }else if (unit.unitType() == UnitType.Mage){
			   mages.put(unit.id(), unit);
			   allUnits.put(unit.id(), unit);
		   }else if (unit.unitType() == UnitType.Healer){
			   healers.put(unit.id(), unit);
			   allUnits.put(unit.id(), unit);
		   }else if (unit.unitType() == UnitType.Factory){
			   factories.put(unit.id(), unit);
			   allUnits.put(unit.id(), unit);
		   }else if (unit.unitType() == UnitType.Rocket){
			   rockets.put(unit.id(), unit);
			   allUnits.put(unit.id(), unit);
		   }
		}
		
	}
	
	public void checkNewUnits(VecUnit units){
		for (int i = 0; i < units.size(); i++){
			Unit unit = units.get(i);
			if (!allUnits.containsKey(unit.id())){
				UnitType type = unit.unitType();
				
				switch(type.name()){
				case "Worker": this.addWorker(unit);
				case "Knight": this.addKnight(unit);
				case "Ranger": this.addRanger(unit);
				case "Mage": this.addMage(unit);
				case "Healer": this.addHealer(unit);
				case "Factory": this.addFactory(unit);
				case "Rocket": this.addRocket(unit);
				default: break;
					
				}
			}
		}
	}
	
	public HashMap<Integer, Unit> getWorkers() {
		return workers;
		
	}



	public void addWorker(Unit newWorker) {
		workers.put(newWorker.id(), newWorker);
		 allUnits.put(newWorker.id(), newWorker);
	}



	public HashMap<Integer, Unit> getKnights() {
		return knights;
	}



	public void addKnight(Unit newKnight) {
		knights.put(newKnight.id(), newKnight);
		 allUnits.put(newKnight.id(), newKnight);
	}



	public HashMap<Integer, Unit> getRangers() {
		return rangers;
	}



	public void addRanger(Unit newRanger) {
		rangers.put(newRanger.id(), newRanger);
		allUnits.put(newRanger.id(), newRanger);
	}



	public HashMap<Integer, Unit> getMages() {
		return mages;
	}



	public void addMage(Unit newMage) {
		mages.put(newMage.id(), newMage);
		allUnits.put(newMage.id(), newMage);
	}



	public HashMap<Integer, Unit> getHealers() {
		return healers;
	}



	public void addHealer(Unit newHealer) {
		healers.put(newHealer.id(), newHealer);
		allUnits.put(newHealer.id(), newHealer);
	}

	public HashMap<Integer, Unit> getFactories() {
		return factories;
	}

	public void addFactory(Unit newFactory) {
		factories.put(newFactory.id(), newFactory);
		allUnits.put(newFactory.id(), newFactory);
	}


	public HashMap<Integer, Unit> getRockets() {
		return rockets;
	}

	public void addRocket(Unit newRocket) {
		rockets.put(newRocket.id(), newRocket);
		allUnits.put(newRocket.id(), newRocket);
	}

	public HashMap<Integer, Unit> getAllUnits(){
		return this.allUnits;
	}
	
	
	public void makeFormation(GameController gc, Unit unit){
		
	}
	
	
}

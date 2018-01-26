import bc.*;

// Class that serves to make Macro Decisions on Player's Resource Management
public class ResearchManagement {

	private GameController gc;
	
	
	public ResearchManagement(GameController gc){
		this.gc = gc;

	}
	
	public void createDefaultQueue(GameController gc){
		gc.queueResearch(UnitType.Worker);
		gc.queueResearch(UnitType.Knight);
		gc.queueResearch(UnitType.Ranger);
		gc.queueResearch(UnitType.Mage);
		gc.queueResearch(UnitType.Knight);
		gc.queueResearch(UnitType.Mage);
		gc.queueResearch(UnitType.Knight);
		gc.queueResearch(UnitType.Mage);
		gc.queueResearch(UnitType.Rocket);
		gc.queueResearch(UnitType.Rocket);
		gc.queueResearch(UnitType.Mage);
		gc.queueResearch(UnitType.Ranger);
		gc.queueResearch(UnitType.Healer);
		gc.queueResearch(UnitType.Healer);
		gc.queueResearch(UnitType.Healer);
		
	}

	public void updateQueue(GameController gc, UnitType branch){
		ResearchInfo ri = new ResearchInfo();
		VecUnitType currentQueue = ri.queue();
		if (gc.resetResearch()==1){
			switch(branch){
				case Rocket: {
					raceToMarsProtocol(gc, ri);
				} case Mage: {
					blinkRushProtocol(gc, ri);
				} case Ranger: {
					snipeRushProtocol(gc, ri);
				} case Knight: {
					javelinRushProtocol(gc, ri);
				} case Worker: {
					workerBoostProtocol(gc, ri);
				} case Healer: {
					healerBoostProtocol(gc, ri);
				}
			}
			for (long i = 0; i<currentQueue.size(); i++){
				gc.queueResearch(currentQueue.get(i));
			}
		}
	}
	
	public void raceToMarsProtocol(GameController gc, ResearchInfo ri){
		for (long i=0; i< 3 - ri.getLevel(UnitType.Rocket); i++ ){
			gc.queueResearch(UnitType.Rocket);	
		}
	}

	public void blinkRushProtocol(GameController gc, ResearchInfo ri){
		for (long i=0; i< 4 - ri.getLevel(UnitType.Mage); i++ ){
			gc.queueResearch(UnitType.Rocket);	
		}

	}
	
	public void snipeRushProtocol(GameController gc, ResearchInfo ri){
		for (long i=0; i< 3 - ri.getLevel(UnitType.Ranger); i++ ){
			gc.queueResearch(UnitType.Rocket);	
		}
	}
	
	public void javelinRushProtocol(GameController gc, ResearchInfo ri){
		for (long i=0; i< 3 - ri.getLevel(UnitType.Knight); i++ ){
			gc.queueResearch(UnitType.Rocket);	
		}
	}

	public void workerBoostProtocol(GameController gc, ResearchInfo ri){
		for (long i=0; i< 4 - ri.getLevel(UnitType.Worker); i++ ){
			gc.queueResearch(UnitType.Rocket);	
		}
	}

	public void healerBoostProtocol(GameController gc, ResearchInfo ri){
		for (long i=0; i< 3 - ri.getLevel(UnitType.Healer); i++ ){
			gc.queueResearch(UnitType.Rocket);	
		}
	}
	
}

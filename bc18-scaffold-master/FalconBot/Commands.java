import bc.*;

public class Commands {

	
	
	/*
	 * General Command Chain Principles this class is built on:
	 * Worker Command Chain
	 * 1) If Under Enemy Unit Attack Range, run out of attack range.
	 * 2) When no factories are built and resource are available to build a factory, set a worker to build a factory.
	 * 3) If Turn >= CONSTANT NUMBER, and no rockets are built, build a rocket.
	 * 4) Otherwise, find nearest Karbonite and farm Karbonite
	 */
	
	public Commands(){
		
	}
	
	
	/**
	 * Method that returns 
	 * @param worker
	 * @return
	 */
	public int getWorkerAction(Unit worker){
		return 0;
	}
	
	
	
}

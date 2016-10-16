package es.deusto.ingenieria.ssdd.data;

import java.util.HashMap;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Swarm;

public class DataModelSwarm extends Observable{
	
	private HashMap<String, Swarm> swarmList;

	public HashMap<String, Swarm> getSwarmList() {
		return swarmList;
	}

	public void setSwarmList(HashMap<String, Swarm> swarmList) {
		this.swarmList = swarmList;
		setChanged();
	    notifyObservers();
	}
	
	public void notifySwarmChanged(Swarm s){
		setChanged();
	    notifyObservers(s); 
	}
	
	
	
	

}

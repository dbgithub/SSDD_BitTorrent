package es.deusto.ingenieria.ssdd.data;

import java.util.HashMap;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Swarm;

/**
 * This class deals with the business logic of everything related to the list of Swarms
 * @author aitor & kevin
 *
 */
public class DataModelSwarm extends Observable{
	
	private HashMap<String, Swarm> swarmList; // A list of Swarms, each one strictly related to a certain content shared

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

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
	
	private String displayedInfoHash; // This is the infohash of the corresponding Swarm that the user click on (with respect to the GUI)
	private HashMap<String, Swarm> swarmList; // A list of Swarms, each one strictly related to a certain content shared. Key represents the InfoHash.
	
	public DataModelSwarm() {
		this.displayedInfoHash = "";
		this.swarmList = new HashMap<String, Swarm>();
	}

	public HashMap<String, Swarm> getSwarmList() {
		return swarmList;
	}

	public void setSwarmList(HashMap<String, Swarm> swarmList) {
		this.swarmList = swarmList;
		setChanged();
	    notifyObservers();
	}
	
	public String getDisplayedInfoHash() {
		return displayedInfoHash;
	}

	public void setDisplayedInfoHash(String displayedInfoHash) {
		this.displayedInfoHash = displayedInfoHash;
	}

	public void notifySwarmChanged(Swarm s){
		setChanged();
	    notifyObservers(s); 
	}
	
	
	
	

}

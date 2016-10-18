package es.deusto.ingenieria.ssdd.data;

import java.util.HashMap;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Tracker;

/**
 * This class deals with the business logic of everything related to the list of Trackers
 * @author aitor & kevin
 *
 */
public class DataModelTracker extends Observable{
	
	private HashMap<String, Tracker> trackerList; // This is a list of key-value pairs for each and every tracker
	
	public HashMap<String, Tracker> getPeerlist() {
		return trackerList;
	}

	public void setPeerlist(HashMap<String, Tracker> trackerList) {
		this.trackerList = trackerList;
		setChanged();
	    notifyObservers();
	}
	
	public void notifyTrackerChanged(Tracker t){
		setChanged();
		notifyObservers(t);
	}

}

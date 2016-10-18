package es.deusto.ingenieria.ssdd.data;

import java.util.HashMap;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Tracker;

public class DataModelTracker extends Observable{
	
	private HashMap<String, Tracker> trackerList;
	
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

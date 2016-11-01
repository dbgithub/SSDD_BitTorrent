package es.deusto.ingenieria.ssdd.data;

import java.util.HashMap;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.tracker.EntranceAndKeepAlive;

/**
 * This class deals with the business logic of everything related to the list of Trackers
 * @author aitor & kevin
 *
 */
public class DataModelTracker extends Observable{
	
	public HashMap<Integer, Tracker> trackerList; // This is a list of key-value pairs for each and every tracker
	public String idRequestUniqueID = "";
	public Thread threadKeepaliveListener;
	public Thread threadKeepaliveSender;
	
	public DataModelTracker(){
		trackerList = new HashMap<Integer, Tracker>();
	}
	
	public HashMap<Integer, Tracker> getPeerlist() {
		return trackerList;
	}

	public void setPeerlist(HashMap<Integer, Tracker> trackerList) {
		this.trackerList = trackerList;
		setChanged();
	    notifyObservers();
	}
	
	public void notifyTrackerChanged(Tracker t){
		setChanged();
		notifyObservers(t);
	}

	public void startEntranceStep(DataModelConfiguration dmc) {
		EntranceAndKeepAlive keepalive = new EntranceAndKeepAlive(dmc, this);
		threadKeepaliveListener = new Thread(keepalive);
		threadKeepaliveListener.start();
		
	}

}

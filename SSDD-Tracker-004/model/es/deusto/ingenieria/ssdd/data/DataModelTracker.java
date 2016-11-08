package es.deusto.ingenieria.ssdd.data;

import java.util.HashMap;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.tracker.EntranceAndKeepAlive;
import es.deusto.ingenieria.ssdd.tracker.KeepALiveSender;
import es.deusto.ingenieria.ssdd.tracker.KeepALiveTimeChecker;

/**
 * This class deals with the business logic of everything related to the list of Trackers
 * @author aitor & kevin
 *
 */
public class DataModelTracker extends Observable{
	
	public HashMap<Integer, Tracker> trackerList; // This is a list of key-value pairs for each and every tracker
	public static String idRequestUniqueID = ""; // This is the ID of the JMS message to make sure that the message from the master goes just to the corresponding tracker.
	public boolean idCorrect; // this boolean indicates at the end, whether the master has rejected the message or not. That is, if the message the tracker was reading was actually issued to him or not.
	public Thread threadKeepaliveListener; // The current tracker launches a Thread to handle (listen) for incoming Keepalive messages.
	public KeepALiveSender keepaliveSender; // After the tracker has been assigned an ID and saved within the tracker list, now, the tracker 
										// launches a Thread to handle the process of sending Keepalive messages
	public KeepALiveTimeChecker keepaliveChecker;
	public Thread threadKeepaliveSender;
	public Thread threadKeepaliveChecker;
	
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
	
	public void stopEntranceStep() {
		System.out.println("Holaaaaaaaaa!");
		if (threadKeepaliveListener.isAlive()) {threadKeepaliveListener.interrupt();}
	}

}

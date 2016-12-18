package es.deusto.ingenieria.ssdd.data;

import java.util.HashMap;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.redundancy.RepositorySyncListener;
import es.deusto.ingenieria.ssdd.tracker.EntranceAndKeepAlive;
import es.deusto.ingenieria.ssdd.tracker.KeepALiveSender;
import es.deusto.ingenieria.ssdd.tracker.KeepALiveTimeChecker;
import es.deusto.ingenieria.ssdd.tracker.MulticastSocketTracker;

/**
 * This class deals with the business logic of everything related to the list of Trackers
 * @author aitor & kevin
 *
 */
public class DataModelTracker extends Observable{
	
	public HashMap<Integer, Tracker> trackerList; // A list of key-value pairs for each and every tracker
	public static String idRequestUniqueID = ""; // The ID of the JMS message to make sure that the message from the master goes just to the corresponding tracker.
	public boolean idCorrect; // this boolean indicates at the end, whether the master has rejected the message or not. That is, if the message the tracker was reading was actually issued to him or not.
	public Thread threadKeepaliveListener; // The current tracker launches a Thread to handle (listen) for incoming Keepalive messages.
	public Thread threadKeepaliveSender; // The current tracker launches a Thread that sends Keepalive messages periodically
	public Thread threadKeepaliveChecker; // The current tracker launches a Thread that makes sure that old or lost trackers are removed from the trackers' list.
	public Thread threadMulticastSocketTracker; // The current tracker will launch a Thread to join a Multicast group in order to be informed about incoming messages from peers.
	public KeepALiveSender keepaliveSender; // Runnable class that handles the process of sending Keepalive messages.
	public KeepALiveTimeChecker keepaliveChecker; // Runnable class that ensures no old or lost trackers remains in the trackers' list.
	public RepositorySyncListener repositorySyncListener; // JMS listener class that handles the process of sending Keepalive messages.
	public MulticastSocketTracker multicastSocketTracker; // Runnable class that waits and handles messages coming from peers to the multicast group.
	
	public DataModelTracker(){
		trackerList = new HashMap<Integer, Tracker>();
	}
	
	public HashMap<Integer, Tracker> getTrackerlist() {
		return trackerList;
	}

	public void setTrackerlist(HashMap<Integer, Tracker> trackerList) {
		this.trackerList = trackerList;
		setChanged();
	    notifyObservers();
	}
	
	public void notifyTrackerChanged(Tracker t){
		setChanged();
		notifyObservers(t);
	}

	public void startEntranceStep(DataModelConfiguration dmc, DataModelSwarm dms, DataModelPeer dmp) {
		EntranceAndKeepAlive keepalive = new EntranceAndKeepAlive(dmc, this, dms, dmp);
		threadKeepaliveListener = new Thread(keepalive);
		threadKeepaliveListener.start();	
	}
	
	public void stopEntranceStep() {
		if (threadKeepaliveListener.isAlive()) {threadKeepaliveListener.interrupt(); System.out.println("Entrance-and-keepalive process STOPPED! :)");}
	}
	
	public void sendRepositoryUpdateRequestMessage(String IP, int port, int peerID, String infohash) {
		this.repositorySyncListener.sendUpdateRequestMessage(IP,port,peerID,infohash);
	}
}

package es.deusto.ingenieria.ssdd.data;

import java.util.HashMap;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Peer;

/**
 * This class deals with the business logic of everything related to the list of Peers
 * @author aitor & kevin
 *
 */
public class DataModelPeer extends Observable {
	
	public HashMap<Integer, Peer> peerlist; // A list of key-value pairs containing all Peers
	
	public DataModelPeer() {
		this.peerlist = new HashMap<Integer, Peer>();
	}
	
	public HashMap<Integer, Peer> getPeerlist() {
		return peerlist;
	}

	public void setPeerlist(HashMap<Integer, Peer> peerlist) {
		this.peerlist = peerlist;
		setChanged();
	    notifyObservers();
	}
	
	public void notifyPeerChanged(Peer p){
		setChanged();
		notifyObservers(p);
	}
	
	

}

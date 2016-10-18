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
	
	private HashMap<String, Peer> peerlist; // A list of key-value pairs containing all Peers
	
	public HashMap<String, Peer> getPeerlist() {
		return peerlist;
	}

	public void setPeerlist(HashMap<String, Peer> peerlist) {
		this.peerlist = peerlist;
		setChanged();
	    notifyObservers();
	}
	
	public void notifyPeerChanged(Peer p){
		setChanged();
		notifyObservers(p);
	}
	
	

}

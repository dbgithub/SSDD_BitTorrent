package es.deusto.ingenieria.ssdd.data;

import java.util.HashMap;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Peer;

public class DataModelPeers extends Observable {
	
	private HashMap<String, Peer> peerlist;
	
	public HashMap<String, Peer> getPeerlist() {
		return peerlist;
	}

	public void setPeerlist(HashMap<String, Peer> peerlist) {
		this.peerlist = peerlist;
		setChanged();
	    notifyObservers();
	}
	
	

}

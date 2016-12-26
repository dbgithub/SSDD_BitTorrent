package es.deusto.ingenieria.ssdd.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.classes.Swarm;


/**
 * This class represents the business logic of what happens when the user configures the Tracker within the corresponding GUI
 * @author aitor & kevin
 *
 */
public class DataModelConfiguration extends Observable{
	private String ip;
	private int port;
	private String id;
	private boolean master;
	private boolean trackerSetUpFinished;
	private boolean availableToReceiveUpdates;
	
	public DataModelConfiguration(String ip, int port, String id) {
		super();
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.trackerSetUpFinished = false;
		this.availableToReceiveUpdates = true;
	}

	public DataModelConfiguration() {
		this.trackerSetUpFinished = false;
		this.availableToReceiveUpdates = true;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
		setChanged();
	    notifyObservers();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
		setChanged();
	    notifyObservers();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		setChanged();
	    notifyObservers();
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
		setChanged();
	    notifyObservers();
	}

	public boolean isTrackerSetUpFinished() {
		return trackerSetUpFinished;
	}

	public void setTrackerSetUpFinished(boolean bol) {
		this.trackerSetUpFinished = bol;
		setChanged();
	    notifyObservers();
	}

	public boolean isAvailableToReceiveUpdates() {
		return availableToReceiveUpdates;
	}

	public void setAvailabilityToReceiveUpdates(boolean bol) {
		this.availableToReceiveUpdates = bol;
		setChanged();
	    notifyObservers();
	}
	
	/**
	 * Destroys/Eliminates the database of the corresponding tracker given the fact that the tracker has shutdown.
	 */
	public void destroyDataRepository() {
		File f = new File("model/es/deusto/ingenieria/ssdd/redundancy/databases/TrackerDB_"+this.id+".db");
		try {
			Files.deleteIfExists(f.toPath());
			System.out.println("The database of tracker with ID='"+this.id+"' was removed successfuly! :)");
		} catch (IOException e) {
			System.err.println("ERROR deleting the database in 'destroyDataRepository' method!!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Dumps the information within the database into the memory (cache)
	 */
	public void dumpDBintoMemory(DataModelSwarm dms, DataModelPeer dmp) {
		System.out.println("Dumping database into memory...");
		DBManager db = new DBManager("model/es/deusto/ingenieria/ssdd/redundancy/databases/TrackerDB_"+this.id+".db");
		ArrayList<Peer> listOfPeers = db.retrievePeers();
		ArrayList<String> listOfInfohashes = db.retrieveTorrents();
		Iterator<Peer> it = listOfPeers.iterator();
		Iterator<String> it2 = listOfInfohashes.iterator();
		// Regarding DataModelPeer:
		while(it.hasNext()) {
			Peer temp = it.next();
			dmp.getPeerlist().put(temp.getId(), temp);
			System.out.println("Peer ID " + temp.getId());
			System.out.println("Peer IP " + temp.getIp());
			System.out.println("Peer Port " + temp.getPort());
			System.out.println("Peer connection ID " + temp.getConnection_id());
			System.out.println("Peer transaction ID " + temp.getTransaction_id());
			System.out.println("Peer announceRequest last update " + temp.getAnnounceRequest_lastupdate());
			System.out.println("Peer connectionID last update " + temp.getConnection_id_lastupdate());
		}
		// Regarding DataModelSwarm:
		while(it2.hasNext()) {
			String infohash = it2.next();
			Swarm temp = new Swarm(infohash);
			it = listOfPeers.iterator(); // renew the iterator
			// We iterate over all peers to figure out who is interested in this infohash. The one interested will be added to swarm's list of peers.
			while(it.hasNext()) {
				Peer peer_temp2 = it.next();
				if (peer_temp2.getSwarmList().containsKey(infohash)) {
					// This means that the current peer is interested (seeding or uploading) the mentioned infohash. So, we add this peer to swarm's list of peer:
					temp.getPeerListHashMap().put(peer_temp2.getId(), peer_temp2);
					if (temp.getSize() == 0) {temp.setSize(peer_temp2.getSwarmList().get(infohash).getLeft() + peer_temp2.getSwarmList().get(infohash).getDownloaded());}
					if (peer_temp2.getSwarmList().get(infohash).getLeft() == 0) {temp.setTotalSeeders(temp.getTotalSeeders()+1);} 
						else if (peer_temp2.getSwarmList().get(infohash).getDownloaded() != 0) {temp.setTotalSeeders(temp.getTotalSeeders()+1); temp.setTotalLeecher(temp.getTotalLeecher()+1);}
						else {temp.setTotalLeecher(temp.getTotalLeecher()+1);}
				}
			}
			dms.getSwarmList().put(infohash, temp);
			dms.notifySwarmChanged(temp);
		}
		db.closeDB();
		System.out.println("DB information dump into memory finished!");
	}

}

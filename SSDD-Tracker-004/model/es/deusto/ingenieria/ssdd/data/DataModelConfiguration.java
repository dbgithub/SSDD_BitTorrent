package es.deusto.ingenieria.ssdd.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Observable;

import es.deusto.ingenieria.ssdd.tracker.EntranceAndKeepAlive;

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
			System.out.println("Database from tracker with ID='"+this.id+"' removed successfuly! :)");
		} catch (IOException e) {
			System.err.println("ERROR deleting the database in 'destroyDataRepository' method!!");
			e.printStackTrace();
		}
	}

}

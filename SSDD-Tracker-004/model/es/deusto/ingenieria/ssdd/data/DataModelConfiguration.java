package es.deusto.ingenieria.ssdd.data;

import java.util.Observable;

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
	
	public DataModelConfiguration(String ip, int port, String id) {
		super();
		this.ip = ip;
		this.port = port;
		this.id = id;
	}

	public DataModelConfiguration() {
		
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
	
	
	
	

}

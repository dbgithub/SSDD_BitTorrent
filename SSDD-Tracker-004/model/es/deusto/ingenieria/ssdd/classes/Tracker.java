package es.deusto.ingenieria.ssdd.classes;

import java.util.Date;

/**
 * This class represents a Tracker
 * @author aitor & kevin
 *
 */
public class Tracker {
	
	private int id; // Tracker ID
	private String ip;
	private int port;
	private boolean master; // Identifies the tracker either as a master or not
	private Date lastKeepAlive; // Date representing when was the last time a KeepAlive was received
	
	public Tracker(int id, boolean master, Date lastKeepAlive) {
		super();
		this.id = id;
		this.master = master;
		this.lastKeepAlive = lastKeepAlive;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean getMaster() {
		return master;
	}
	public void setMaster(boolean master) {
		this.master = master;
	}
	public Date getLastKeepAlive() {
		return lastKeepAlive;
	}
	public void setLastKeepAlive(Date lastKeepAlive) {
		this.lastKeepAlive = lastKeepAlive;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
	

}

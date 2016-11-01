package es.deusto.ingenieria.ssdd.classes;

import java.util.Date;

/**
 * This class represents a Tracker
 * @author aitor & kevin
 *
 */
public class Tracker {
	
	private int id; // Tracker ID
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
	public boolean getWhoisMaster() {
		return master;
	}
	public void setWhoisMaster(boolean master) {
		this.master = master;
	}
	public Date getLastKeepAlive() {
		return lastKeepAlive;
	}
	public void setLastKeepAlive(Date lastKeepAlive) {
		this.lastKeepAlive = lastKeepAlive;
	}
	
	

}

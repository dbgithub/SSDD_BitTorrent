package es.deusto.ingenieria.ssdd.classes;

import java.util.Date;

/**
 * This class represents a Tracker
 * @author aitor & kevin
 *
 */
public class Tracker {
	
	private String id; // Tracker ID
	private String whoisMaster; // This is the ID of the Tracker Master
	private Date lastKeepAlive; // Date representing when was the last time a KeepAlive was received
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getWhoisMaster() {
		return whoisMaster;
	}
	public void setWhoisMaster(String whoisMaster) {
		this.whoisMaster = whoisMaster;
	}
	public Date getLastKeepAlive() {
		return lastKeepAlive;
	}
	public void setLastKeepAlive(Date lastKeepAlive) {
		this.lastKeepAlive = lastKeepAlive;
	}
	
	

}

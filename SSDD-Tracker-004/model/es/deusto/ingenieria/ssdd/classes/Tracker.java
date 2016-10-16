package es.deusto.ingenieria.ssdd.classes;

import java.util.Date;

public class Tracker {
	
	private String id;
	private String whoisMaster; //Master ID
	private Date lastKeepAlive;
	
	
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

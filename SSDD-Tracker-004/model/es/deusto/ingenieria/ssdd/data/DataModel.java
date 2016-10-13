package es.deusto.ingenieria.ssdd.data;

import java.util.Observable;

public class DataModel extends Observable{
	private String ip;
	private int port;
	private String id;
	
	public DataModel(String ip, int port, String id) {
		super();
		this.ip = ip;
		this.port = port;
		this.id = id;
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
	
	

}

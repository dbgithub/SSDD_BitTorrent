package es.deusto.ingenieria.ssdd.classes;

import java.util.HashMap;

/**
 * Class that represents a peer at the network
 * 
 * @author kevin & aitor
 *
 */
public class Peer {
	
	private int id;
	private String ip;
	private int port;
	private HashMap<String, PeerTorrent> swarmList;
	
	public Peer(int id, String ip, int port, HashMap<String, PeerTorrent> swarmlist) {
		super();
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.swarmList = swarmlist;
	}
	
	public Peer(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	/**
	 * @return the swarmList
	 */
	public HashMap<String, PeerTorrent> getSwarmList() {
		return swarmList;
	}

	/**
	 * @param swarmList the swarmList to set
	 */
	public void setSwarmList(HashMap<String, PeerTorrent> swarmList) {
		this.swarmList = swarmList;
	}
	
	/**
	 * equals is overrided to ensure we are just comparing IDs.
	 */
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Peer) {
			return ((Peer) o).id == this.id;
		}
		return false;
	}
	
	

}

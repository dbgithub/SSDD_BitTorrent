package es.deusto.ingenieria.ssdd.classes;


import java.util.HashMap;
import java.util.Date;

/**
 * Class that represents a peer at the network
 * 
 * @author kevin & aitor
 *
 */
public class Peer {
	
	private long connection_id;
	private Date connection_id_lastupdate;
	private Date announceRequest_lastupdate;
	private int transaction_id;
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

	public Peer(long connection_id, int transaction_id, int id, String ip, int port) {
		super();
		this.connection_id = connection_id;
		this.transaction_id = transaction_id;
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.swarmList = new HashMap<String, PeerTorrent>();
		this.connection_id_lastupdate = new Date();
	}

	public Peer() {
		this.swarmList = new HashMap<String, PeerTorrent>();
		this.connection_id_lastupdate = new Date();
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

	public HashMap<String, PeerTorrent> getSwarmList() {
		return swarmList;
	}

	public void setSwarmList(HashMap<String, PeerTorrent> swarmList) {
		this.swarmList = swarmList;
	}
	
	public void updatePeerTorrentInfo(String infohash, long downloaded, long uploaded, long left) {
		swarmList.get(infohash).setDownloaded(downloaded);
		swarmList.get(infohash).setUploaded(uploaded);
		swarmList.get(infohash).setLeft(left);
	}
	

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Peer) {
			return ((Peer) o).id == this.id;
		}
		return false;
	}

	public long getConnection_id() {
		return connection_id;
	}

	public void setConnection_id(long connection_id) {
		this.connection_id = connection_id;
	}

	public int getTransaction_id() {
		return transaction_id;
	}

	public void setTransaction_id(int transaction_id) {
		this.transaction_id = transaction_id;
	}

	public Date getConnection_id_lastupdate() {
		return connection_id_lastupdate;
	}

	public void setConnection_id_lastupdate(Date connection_id_lastupdate) {
		this.connection_id_lastupdate = connection_id_lastupdate;
	}

	public Date getAnnounceRequest_lastupdate() {
		return announceRequest_lastupdate;
	}

	public void setAnnounceRequest_lastupdate(Date announceRequest_lastupdate) {
		this.announceRequest_lastupdate = announceRequest_lastupdate;
	}
	
	/**
	 * Counts the amount of torrents that are finished by now (left = 0)
	 * @return the number of files that have left = 0
	 */
	public int countCompleted() {
		if (swarmList.size() != 0) {
			int counter = 0;
			for (PeerTorrent pt : swarmList.values()) {
				if (pt.getLeft() == 0) {counter++;}
			}
			return counter;
		} else {
			return -1;
		}
	}

}

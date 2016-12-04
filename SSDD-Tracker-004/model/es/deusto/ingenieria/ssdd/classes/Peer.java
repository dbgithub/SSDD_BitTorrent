package es.deusto.ingenieria.ssdd.classes;

import java.util.Date;

/**
 * Class that represents a peer at the network
 * 
 * @author kevin
 *
 */
public class Peer {
	
	private long connection_id;
	private Date connection_id_lastupdate;
	private Date announceRequest_lastupdate;
	private long transaction_id;
	private int id;
	private String ip;
	private int port;
	
	public Peer(int id, String ip, int port, int downloaded, int uploaded) {
		super();
		this.id = id;
		this.ip = ip;
		this.port = port;
	}

	public Peer(long connection_id, long transaction_id, int id, String ip, int port) {
		super();
		this.connection_id = connection_id;
		this.transaction_id = transaction_id;
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.connection_id_lastupdate = new Date();
	}



	public Peer() {
		// TODO Auto-generated constructor stub
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

	public long getConnection_id() {
		return connection_id;
	}

	public void setConnection_id(long connection_id) {
		this.connection_id = connection_id;
	}

	public long getTransaction_id() {
		return transaction_id;
	}

	public void setTransaction_id(long transaction_id) {
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
	
	

}

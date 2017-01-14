package es.deusto.ingenieria.ssdd.classes;

import java.net.InetAddress;

/**
 * A class that represents a Peer at Client's side
 * @author aitor & kevin
 *
 */
public class Peer {
	private int id;
	private InetAddress ip;
	private int port;
	
	public Peer() {}
	
	public Peer(int ID, InetAddress IP, int Port) {
		this.id = ID;
		this.ip = IP;
		this.port = Port;
	}
	
	public Peer(InetAddress IP, int Port) {
		this.ip = IP;
		this.port = Port;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Peer) {
			return ((Peer) o).ip.getHostAddress().equals(this.ip.getHostAddress()) && ((Peer) o).port == this.port;
		}
		return false;
	}
	
}

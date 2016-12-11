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
	
	private String ip;
	private int port;

	public Peer(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
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

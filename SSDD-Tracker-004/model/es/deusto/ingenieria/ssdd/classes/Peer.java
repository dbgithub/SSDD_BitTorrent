package es.deusto.ingenieria.ssdd.classes;

/**
 * Class that represents a peer at the network
 * 
 * @author kevin
 *
 */
public class Peer {
	
	private String id;
	private String ip;
	private int port;
	private int downloaded;
	private int uploaded;
	
	public Peer(String id, String ip, int port, int downloaded, int uploaded) {
		super();
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.downloaded = downloaded;
		this.uploaded = uploaded;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public int getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(int downloaded) {
		this.downloaded = downloaded;
	}

	public int getUploaded() {
		return uploaded;
	}

	public void setUploaded(int uploaded) {
		this.uploaded = uploaded;
	}
	
	
	
	

}

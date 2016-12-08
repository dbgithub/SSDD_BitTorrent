package es.deusto.ingenieria.ssdd.classes;

public class PeerTrackerTemplate {
	
	private int id;
	private String ip;
	private int port;
	private String infoHash;
	
	public PeerTrackerTemplate(int id, String ip, int port, String infoHash) {
		super();
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.infoHash = infoHash;
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
	public String getInfoHash() {
		return infoHash;
	}
	public void setInfoHash(String infoHash) {
		this.infoHash = infoHash;
	}
	
	

}
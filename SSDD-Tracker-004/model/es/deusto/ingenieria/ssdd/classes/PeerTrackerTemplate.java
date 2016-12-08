package es.deusto.ingenieria.ssdd.classes;

public class PeerTrackerTemplate {
	
	private int id;
	private String ip;
	private int port;
	private String infoHash;
	private long uploaded;
	private long downloaded;
	private long left;
	private boolean update; // specifies whether to do and UPDATE or not. If not, this means that a new INSERT has to be done in the database.
	
	public PeerTrackerTemplate(int id, String ip, int port, String infoHash, long uploaded, long downloaded, long left, boolean update) {
		super();
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.infoHash = infoHash;
		this.uploaded = uploaded;
		this.downloaded = downloaded;
		this.left = left;
		this.update = update;
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
	public long getUploaded() {
		return uploaded;
	}
	public void setUploaded(int uploaded) {
		this.uploaded = uploaded;
	}
	public long getDownloaded() {
		return downloaded;
	}
	public void setDownloaded(int downloaded) {
		this.downloaded = downloaded;
	}
	public long getLeft() {
		return left;
	}
	public void setLeft(int left) {
		this.left = left;
	}
	public boolean isUpdate() {
		return update;
	}
	public void setUpdate(boolean update) {
		this.update = update;
	}
	
	

}

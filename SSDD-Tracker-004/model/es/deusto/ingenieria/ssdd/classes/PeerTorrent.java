package es.deusto.ingenieria.ssdd.classes;

/**
 * Class that represents the relation between a peer and a torrent.
 * This class contains information regarding the details of the torrent current status for a given peer.
 * @author aitor & kevin
 *
 */
public class PeerTorrent implements Comparable<PeerTorrent>{
	
	private int id;
	private String ip;
	private int port;
	private String infoHash;
	private long downloaded;
	private long uploaded;
	private long left;
	
	public PeerTorrent(int id, String ip, int port, String infoHash) {
		super();
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.infoHash = infoHash;
	}
	
	public PeerTorrent(String infohash, long uploaded, long downloaded, long left) {
		this.infoHash = infohash;
		this.downloaded = downloaded;
		this.uploaded = uploaded;
		this.left = left;
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
	public long getDownloaded() {
		return downloaded;
	}
	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}
	public long getUploaded() {
		return uploaded;
	}
	public void setUploaded(long uploaded) {
		this.uploaded = uploaded;
	}
	public long getLeft() {
		return left;
	}
	public void setLeft(long left) {
		this.left = left;
	}

	@Override
	public int compareTo(PeerTorrent o) {
		Long thisone = new Long(this.left);
		Long theotherone = new Long(o.left);
		return thisone.compareTo(theotherone);
	}

}

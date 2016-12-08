package es.deusto.ingenieria.ssdd.classes;

public class PeerTorrent {
	private String infoHash;
	private int uploaded;
	private int downloaded;
	private int left;
	
	public PeerTorrent(String infohash, int uploaded, int downloaded, int left) {
		this.infoHash = infohash;
		this.uploaded = uploaded;
		this.downloaded = downloaded;
		this.left = left;
	}

	/**
	 * @return the infoHash
	 */
	public String getInfoHash() {
		return infoHash;
	}

	/**
	 * @param infoHash the infoHash to set
	 */
	public void setInfoHash(String infoHash) {
		this.infoHash = infoHash;
	}

	/**
	 * @return the uploaded
	 */
	public int getUploaded() {
		return uploaded;
	}

	/**
	 * @param uploaded the uploaded to set
	 */
	public void setUploaded(int uploaded) {
		this.uploaded = uploaded;
	}

	/**
	 * @return the downloaded
	 */
	public int getDownloaded() {
		return downloaded;
	}

	/**
	 * @param downloaded the downloaded to set
	 */
	public void setDownloaded(int downloaded) {
		this.downloaded = downloaded;
	}

	/**
	 * @return the left
	 */
	public int getLeft() {
		return left;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(int left) {
		this.left = left;
	}

}

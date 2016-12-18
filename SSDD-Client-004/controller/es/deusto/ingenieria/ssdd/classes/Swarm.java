package es.deusto.ingenieria.ssdd.classes;

import java.util.ArrayList;
import java.util.List;

import bitTorrent.tracker.protocol.udp.PeerInfo;

/**
 * Class that represents a combination of a simple Swarm, plus information regarding the state of the .torrent file with respect to the current peer
 * @author kevin & aitor
 *
 */
public class Swarm {
	
	private String infoHash; // This is the identification of the torrent this swarms is sharing (downloading and/or uploading)
	private String swarmFile; // The name of the file this swarm is sharing (downloading and/or uploading)
	private int size; // Size of the file
	private int totalSeeders; // Peers who are uploading the file
	private int totalLeecher; // Peers who are downloading the file
	private List<PeerInfo> peerList; //Peer list
	private long downloaded; // the amount of downloaded stuff regarding the current peer
	private long uploaded; // the amount of uploaded stuff regarding the current peer
	private long left; // the amount of stuff left regarding the current peer
	
	public Swarm(String infoHash, String file, int size) {
		this.infoHash = infoHash;
		this.swarmFile = file;
		this.size = size;
		this.totalLeecher = 0;
		this.totalSeeders = 0;
		this.downloaded = 0;
		this.uploaded = 0;
		this.left = 0;
		this.peerList = new ArrayList<>();
	}
	
	public Swarm(String infohash2) {
		this.infoHash = infohash2;
		this.swarmFile = "";
		this.totalLeecher = 0;
		this.totalSeeders = 0;
		this.size = 0;
		this.downloaded = 0;
		this.uploaded = 0;
		this.left = 0;
		this.peerList = new ArrayList<>();
	}

	public String getInfoHash() {
		return infoHash;
	}
	public void setInfoHash(String infoHash) {
		this.infoHash = infoHash;
	}
	public String getSwarmFile() {
		return swarmFile;
	}
	public void setSwarmFile(String swarmFile) {
		this.swarmFile = swarmFile;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getTotalSeeders() {
		return totalSeeders;
	}
	public void setTotalSeeders(int totalSeeders) {
		this.totalSeeders = totalSeeders;
	}
	public int getTotalLeecher() {
		return totalLeecher;
	}
	public void setTotalLeecher(int totalLeecher) {
		this.totalLeecher = totalLeecher;
	}

	public List<PeerInfo> getPeerList() {
		return peerList;
	}

	public void setPeerList(List<PeerInfo> peerList) {
		this.peerList = peerList;
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
}

package es.deusto.ingenieria.ssdd.classes;

import java.util.ArrayList;
import java.util.HashMap;

import bitTorrent.tracker.protocol.udp.PeerInfo;

/**
 * Class that represents a Swarm of the network
 * @author kevin & aitor
 *
 */
public class Swarm {
	
	private String infoHash; // This is the identification of the torrent this swarms is sharing (downloading and/or uploading)
	private String swarmFile; // The name of the file this swarm is sharing (downloading and/or uploading)
	private int size; // Size of the file
	private int totalSeeders; // Peers who are uploading the file
	private int totalLeecher; // Peers who are downloading the file
	private HashMap<Integer, Peer> peerList; //Peer list
	
	public Swarm(String infoHash, String file, int size) {
		this.infoHash = infoHash;
		this.swarmFile = file;
		this.size = size;
		this.totalLeecher = 0;
		this.totalSeeders = 0;
		this.peerList = new HashMap<Integer, Peer>();
	}
	
	public Swarm(String infohash2) {
		this.infoHash = infohash2;
		this.swarmFile = "";
		this.totalLeecher = 0;
		this.totalSeeders = 0;
		this.size = 0;
		this.peerList = new HashMap<Integer, Peer>();
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
	public HashMap<Integer, Peer> getPeerListHashMap() {
		return peerList;
	}
	public void setPeerListHashMap(HashMap<Integer, Peer> peerList) {
		this.peerList = peerList;
	}
	public void addPeerToList(int PeerID, Peer p) {
		this.peerList.put(PeerID, p);
	}
	public ArrayList<PeerInfo> getPeerInfoList() {
		ArrayList<PeerInfo> resul = new ArrayList<PeerInfo>();
		if (!peerList.isEmpty()) {
			for (Peer p : peerList.values()) {
				PeerInfo pf = new PeerInfo();
				pf.setIpAddress(Integer.parseInt(p.getIp()));
				pf.setPort(p.getPort());
				resul.add(pf);
			}		
			return resul;
		}
		return null;
	}
	

}

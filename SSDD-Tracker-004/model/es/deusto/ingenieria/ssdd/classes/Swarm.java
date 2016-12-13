package es.deusto.ingenieria.ssdd.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import bitTorrent.tracker.protocol.udp.PeerInfo;
import es.deusto.ingenieria.ssdd.tracker.MulticastSocketTracker;

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
	private HashMap<Integer, Peer> peerList; // This map contains a set of peer that are related somewhat to this swarm
	
	public Swarm(String infoHash, String file, int size) {
		this.infoHash = infoHash;
		this.swarmFile = file;
		this.size = size;
		this.totalLeecher = 0;
		this.totalSeeders = 0;
		this.peerList = new HashMap<Integer, Peer>();
	}
	
	public Swarm(String infohash) {
		this.infoHash = infohash;
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
	public ArrayList<PeerInfo> getPeerInfoList(long left, String infohash, int numberOfPeers) {
		//We order all the peers in a swarm taking into account the bytes of the file left
		ArrayList<PeerInfo> result = new ArrayList<PeerInfo>();
		if (!peerList.isEmpty()) {
			List<PeerTorrent> peerTorrent = new ArrayList<PeerTorrent>();
			for(Peer p : peerList.values()){
				peerTorrent.add(p.getSwarmList().get(infohash));
			}
			if(left == 0){
				//Then the peer is a complete seeder, don't need another seeders
				Collections.sort(peerTorrent);
			}
			else{
				Collections.sort(peerTorrent);
				Collections.reverse(peerTorrent);
			}
			int index = 0;
			while(index < numberOfPeers && index < peerTorrent.size())
			{
				PeerInfo pf = new PeerInfo();
				pf.setIpAddress(MulticastSocketTracker.convertIpAddressToInt(peerTorrent.get(index).getIp()));
				pf.setPort(peerTorrent.get(index).getPort());
				result.add(pf);
				index++;
			}	
			return result;
		}
		return null;
	}
	

}

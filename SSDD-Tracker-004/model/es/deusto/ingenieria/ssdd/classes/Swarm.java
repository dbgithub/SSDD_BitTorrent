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
	private long size; // Size of the file
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
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
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
	
	/**
	 * Counts the amount of torrents that are finished by now (left = 0) for each Peer
	 * @return the number of files that have left = 0
	 */
	public int countCompleted() {
		if (peerList.size() != 0) {
			int counter = 0;
			for (Peer p : peerList.values()) {
				if (p.getSwarmList().get(infoHash).getLeft() == 0) {counter++;}
			}
			return counter;
		} else {
			return -1;
		}
	}

	public int getAppropiateInterval(HashMap<String, Swarm> hashMap) {
		//we will determine the interval considering the number of peers / seeders
		//we first normalize data taking into account max/min
		long maxSize = Long.MIN_VALUE;
		long minSize = Long.MAX_VALUE;
		int maxLeechers = Integer.MIN_VALUE;
		int minLeechers = Integer.MAX_VALUE;
		int maxSeeders = Integer.MIN_VALUE;
		int minSeeders = Integer.MAX_VALUE;
		if(hashMap.size() > 1){
			for(Swarm temp : hashMap.values()){
				if(temp.getTotalLeecher() > maxLeechers){
					maxLeechers = temp.getTotalLeecher();
				}
				if(temp.getTotalLeecher() < minLeechers){
					minLeechers = temp.getTotalLeecher();
				}
				if(temp.getTotalSeeders() > maxSeeders){
					maxSeeders = temp.getTotalSeeders();
				}
				if(temp.getTotalSeeders() < minSeeders){
					minSeeders = temp.getTotalSeeders();
				}
				if(temp.getSize() > maxSize){
					maxSize = temp.getSize();
				}
				if(temp.getSize() < minSize){
					minSize = temp.getSize();
				}
			}
			//normalized values
			double thisSize = this.size - minSize / (maxSize - minSize);
			double thisLeechers = this.totalLeecher - minLeechers /(maxLeechers - minLeechers);
			double thisSeeders = this.totalSeeders - minSeeders /(maxSeeders - minSeeders);
			//If the file is big and the number of leechers high, we get a relative low interval
			return (int) Math.round(70 - thisSize * 30 - thisLeechers * 30 + 60 * thisSeeders);
		}
		else{
			//This means that this swarm is the only one
			//Just check number of peers (seeders/leechers)
			double seederLeechersRatio = totalSeeders/totalLeecher;
			if(seederLeechersRatio >= 1){
				return (int) Math.round(60 + 60 * seederLeechersRatio);
			}
			else{
				return (int) Math.round(60 * seederLeechersRatio);
			}
		}
	}
	

}

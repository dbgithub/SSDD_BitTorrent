package es.deusto.ingenieria.ssdd.classes;

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

}

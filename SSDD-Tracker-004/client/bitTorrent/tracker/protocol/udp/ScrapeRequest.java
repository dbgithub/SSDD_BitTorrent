package bitTorrent.tracker.protocol.udp;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Offset          Size            	Name            	Value
 * 0               64-bit integer  	connection_id
 * 8               32-bit integer  	action          	2 // scrape
 * 12              32-bit integer  	transaction_id
 * 16 + 20 * n     20-byte string  	info_hash
 * 16 + 20 * N
 *
 */

public class ScrapeRequest extends BitTorrentUDPRequestMessage {

	private List<String> infoHashes;
	
	public ScrapeRequest() {
		super(Action.SCRAPE);
		this.infoHashes = new ArrayList<>();
	}
	
	@Override
	public byte[] getBytes() {
		//TODO: Complete this method
			
		return null;
	}
	
	public static ScrapeRequest parse(byte[] byteArray) {
		//TODO: Complete this method
		
		return null;
	}
	
	public List<String> getInfoHashes() {
		return infoHashes;
	}

	public void addInfoHash(String infoHash) {
		if (infoHash != null && !infoHash.trim().isEmpty() && !this.infoHashes.contains(infoHash)) {
			this.infoHashes.add(infoHash);
		}
	}
}

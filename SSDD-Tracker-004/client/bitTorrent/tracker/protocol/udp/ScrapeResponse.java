package bitTorrent.tracker.protocol.udp;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Offset      	Size            	Name            Value
 * 0           	32-bit integer  	action          2 // scrape
 * 4           	32-bit integer  	transaction_id
 * 8 + 12 * n  	32-bit integer  	seeders
 * 12 + 12 * n 	32-bit integer  	completed
 * 16 + 12 * n 	32-bit integer  	leechers
 * 8 + 12 * N
 * 
 */

public class ScrapeResponse extends BitTorrentUDPMessage {
	
	private List<ScrapeInfo> scrapeInfos;

	public ScrapeResponse() {
		super(Action.SCRAPE);		
		this.scrapeInfos = new ArrayList<>();
	}
	
	@Override
	public byte[] getBytes() {
		//TODO: Complete this method
			
		return null;
	}
	
	public static ScrapeResponse parse(byte[] byteArray) {
		//TODO: Complete this method
		
		return null;
	}
	
	public List<ScrapeInfo> getScrapeInfos() {
		return scrapeInfos;
	}

	public void addScrapeInfo(ScrapeInfo scrapeInfo) {
		if (scrapeInfo != null && !this.scrapeInfos.contains(scrapeInfo)) {
			this.scrapeInfos.add(scrapeInfo);
		}
	}
}
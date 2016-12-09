package bitTorrent.tracker.protocol.udp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage.Action;

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
		ByteBuffer buffer = ByteBuffer.allocate(8+12*scrapeInfos.size());
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.putLong(0, super.getAction().value());
		buffer.putLong(4, super.getTransactionId());
		
		int index = 8;
		for(ScrapeInfo t: scrapeInfos){
			buffer.putLong(index, t.getSeeders());
			buffer.putLong(index+4, t.getCompleted());
			buffer.putLong(index+8, t.getLeechers());
			index = index + 12;
		}
		buffer.flip();
			
		return buffer.array();
	}
	
	public static ScrapeResponse parse(byte[] byteArray) {
		try {
	    	ByteBuffer buffer = ByteBuffer.wrap(byteArray);
		    buffer.order(ByteOrder.BIG_ENDIAN);
		    
		    ScrapeResponse msg = new ScrapeResponse();
		    
		    msg.setAction(Action.valueOf(buffer.getInt(0)));	    
		    msg.setTransactionId(buffer.getInt(4));
		    
		    int index = 8;
		    ScrapeInfo scrapeInfo = null;
		    
		    while ((index + 12) < byteArray.length ) {
		    	scrapeInfo = new ScrapeInfo();
		    	scrapeInfo.setSeeders(buffer.getInt(index));
		    	scrapeInfo.setCompleted(buffer.getInt(index+4));
		    	scrapeInfo.setLeechers(buffer.getInt(index+8));
		    	index += 12;
		    }		    
			
			return msg;
		} catch (Exception ex) {
			System.out.println("# Error parsing AnnounceResponse message: " + ex.getMessage());
		}
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
package bitTorrent.tracker.protocol.udp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper;

import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage.Action;

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
		ByteBuffer buffer = ByteBuffer.allocate(16+(40*infoHashes.size()));
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.putLong(0, super.getConnectionId());
		buffer.putInt(8, super.getAction().value());
		buffer.putInt(12, super.getTransactionId());
		buffer.position(16);
		int index = 16;
		System.out.println("Remaining: " + buffer.remaining());
		for(String t: infoHashes){
			System.out.println("Length of infohash: "+ t.getBytes().length);
			buffer.put(t.getBytes(), 0, 40);
			buffer.position(index+40);
			index = index + 40;
		}
		
		buffer.flip();
			
		return buffer.array();
	}
	
	public static ScrapeRequest parse(byte[] byteArray) {
		try {
			System.out.println("Length: "+ byteArray.length);
	    	ByteBuffer buffer = ByteBuffer.wrap(byteArray);
		    buffer.order(ByteOrder.BIG_ENDIAN);
		    
		    ScrapeRequest msg = new ScrapeRequest();
		    
		    msg.setConnectionId(buffer.getLong(0));
		    msg.setAction(Action.valueOf(buffer.getInt(8)));	    
		    msg.setTransactionId(buffer.getInt(12));
		    buffer.position(16);
		    int index = 16;
		    
		    while ((index + 40) <= byteArray.length ) {
		    	byte[] msgB = new byte[40];
			    buffer.get(msgB, 0, msgB.length);
			    msg.getInfoHashes().add(new String(msgB));
		    	index += 40;
		    	System.out.println(index);
		    	buffer.position(index);
		    }		    
			
			return msg;
		} catch (Exception ex) {
			System.out.println("# Error parsing AnnounceResponse message: " + ex.getMessage());
		}
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
	
	public static void main(String[]args){
		ScrapeRequest temp = new ScrapeRequest();
		temp.setAction(Action.ANNOUNCE);
		temp.setConnectionId(0020011212);
		temp.setTransactionId(999999999);
		String infohash1 = "ABF02E0F6F6F352DAC22F8706799C147F40AF701";
		String infohash2 = "ABF02E0F6F6F352DAC22F87067221C147F40AF21";
		temp.infoHashes.add(infohash1);
		temp.infoHashes.add(infohash2);
		byte[] array = temp.getBytes();
		ScrapeRequest otro = ScrapeRequest.parse(array);
		System.out.println("Transaction ID: "+otro.getTransactionId() + " infohash: "+ otro.getInfoHashes().get(1));
		
	}
}

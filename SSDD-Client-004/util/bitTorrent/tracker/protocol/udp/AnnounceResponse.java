package bitTorrent.tracker.protocol.udp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Offset      Size            	Name            Value
 * 0           32-bit integer  	action          1 // announce
 * 4           32-bit integer  	transaction_id
 * 8           32-bit integer  	interval
 * 12          32-bit integer  	leechers
 * 16          32-bit integer  	seeders
 * 20 + 6 * n  32-bit integer  	IP address
 * 24 + 6 * n  16-bit integer  	TCP port
 * 20 + 6 * N
 * 
 */

public class AnnounceResponse extends BitTorrentUDPMessage {
	
	private int interval;
	private int leechers;
	private int seeders;
	
	private List<PeerInfo> peers;
	
	public AnnounceResponse() {
		super(Action.ANNOUNCE);
		
		this.peers = new ArrayList<>();
	}
	
	@Override
	public byte[] getBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(20+6*peers.size());
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.putInt(0, super.getAction().value());
		buffer.putInt(4, super.getTransactionId());
		buffer.putInt(8, this.getInterval());
		buffer.putInt(12, this.getLeechers());
		buffer.putInt(16, this.getSeeders());
		
		int index = 20;
		for(PeerInfo t: peers){
			buffer.putInt(index, t.getIpAddress());
			buffer.putShort(index+4, (short) t.getPort());
			index = index + 6;
		}
		buffer.flip();
			
		return buffer.array();
	}
	
	public static AnnounceResponse parse(byte[] byteArray) {
	    try {
	    	ByteBuffer buffer = ByteBuffer.wrap(byteArray);
		    buffer.order(ByteOrder.BIG_ENDIAN);
		    
		    AnnounceResponse msg = new AnnounceResponse();
		    
		    msg.setAction(Action.valueOf(buffer.getInt(0)));	    
		    msg.setTransactionId(buffer.getInt(4));
		    msg.setInterval(buffer.getInt(8));
		    msg.setLeechers(buffer.getInt(12));
		    msg.setSeeders(buffer.getInt(16));
		    
		    int index = 20;
		    PeerInfo peerInfo = null;
		    
		    while ((index + 6) < byteArray.length ) {
		    	peerInfo = new PeerInfo();
		    	peerInfo.setIpAddress(buffer.getInt(index));
		    	peerInfo.setPort(buffer.getShort(index+4));		    	
		    	msg.getPeers().add(peerInfo);
		    	index += 6;
		    }		    
			
			return msg;
		} catch (Exception ex) {
			System.out.println("# Error parsing AnnounceResponse message: " + ex.getMessage());
		}
	    
	    return null;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getLeechers() {
		return leechers;
	}

	public void setLeechers(int leechers) {
		this.leechers = leechers;
	}

	public int getSeeders() {
		return seeders;
	}

	public void setSeeders(int seeders) {
		this.seeders = seeders;
	}

	public List<PeerInfo> getPeers() {
		return peers;
	}

	public void setPeers(List<PeerInfo> peers) {
		this.peers = peers;
	}
}

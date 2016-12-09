package bitTorrent.tracker.protocol.udp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage.Action;
import bitTorrent.util.ByteUtils;

/**
 *
 * Offset  Size    			Name    			Value
 * 0       64-bit integer  	connection_id
 * 8       32-bit integer  	action          	1 // announce
 * 12      32-bit integer  	transaction_id
 * 16      20-byte string  	info_hash
 * 36      20-byte string  	peer_id
 * 56      64-bit integer  	downloaded
 * 64      64-bit integer  	left
 * 72      64-bit integer  	uploaded
 * 80      32-bit integer  	event           	0 // 0: none; 1: completed; 2: started; 3: stopped
 * 84      32-bit integer  	IP address      	0 // default
 * 88      32-bit integer  	key
 * 92      32-bit integer  	num_want        	-1 // default
 * 96      16-bit integer  	port
 * 98
 * 
 */

public class AnnounceRequest extends BitTorrentUDPRequestMessage {

	public enum Event {		
		NONE(0),
		COMPLETED(1),
		STARTED(2),
		STOPPED(3);
		
		private int value;
		
		private Event(int value) {
			this.value = value;
		}
		
		public int value() {
			return this.value;
		}
		
		public static Event valueOf(int value) {
			for (Event a : Event.values()) {
				if (a.value == value) {
					return a;
				}
			}
			return null;
		}
	}
	
	private byte[] infoHash;
	private String peerId;
	private long downloaded;
	private long left;
	private long uploaded;
	private Event event;
	private int key;
	private int numWant = -1;
	
	private PeerInfo peerInfo;
	
	public AnnounceRequest() {
		super(Action.ANNOUNCE);
		
		this.peerInfo = new PeerInfo();
		this.peerInfo.setIpAddress(0);
	}
	
	@Override
	public byte[] getBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(98);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.putLong(0, super.getConnectionId());
		buffer.putInt(8, super.getAction().value());
		buffer.putInt(12, super.getTransactionId());
		buffer.position(16);
		buffer.put(this.infoHash);
		buffer.position(36);
		buffer.put(this.peerId.getBytes());
		buffer.putLong(56, this.downloaded);
		buffer.putLong(64, this.left);
		buffer.putLong(72, this.uploaded);
		buffer.putInt(80, event.value);
		buffer.putInt(84, this.peerInfo.getIpAddress());
		buffer.putInt(88, this.key);
		buffer.putInt(92, this.numWant);
		buffer.putShort(96, (short)this.peerInfo.getPort());
		
		buffer.flip();
			
		return buffer.array();
	}
	
	public static AnnounceRequest parse(byte[] byteArray) {
		//TODO: Complete this method
		try {
	    	ByteBuffer buffer = ByteBuffer.wrap(byteArray);
		    buffer.order(ByteOrder.BIG_ENDIAN);
		    
		    AnnounceRequest msg = new AnnounceRequest();
		    
		    msg.setConnectionId(buffer.getLong(0));
		    msg.setAction(Action.valueOf(buffer.getInt(8)));
		    msg.setTransactionId(buffer.getInt(12));
		    buffer.position(16);
		    byte[] infohash = new byte[20];
		    buffer.get(infohash);
		    msg.setInfoHash(infohash);
		    buffer.position(36);
		    byte[] peerid = new byte[20];
		    buffer.get(peerid);
		    msg.setPeerId(new String(peerid));
		    msg.setDownloaded(buffer.getLong(56));
		    msg.setLeft(buffer.getLong(64));
		    msg.setUploaded(buffer.getLong(72));
		    msg.setEvent(Event.valueOf(buffer.getInt(80)));
		    PeerInfo pI = new PeerInfo();
		    pI.setIpAddress(buffer.getInt(84));
		    msg.setKey(buffer.getInt(88));
		    msg.setNumWant(buffer.getInt(92));
		    pI.setPort(buffer.getShort(96));
		    msg.setPeerInfo(pI);    
			
			return msg;
		} catch (Exception ex) {
			System.out.println("# Error parsing AnnounceResponse message: " + ex.getMessage());
		}
		return null;
	}

	public byte[] getInfoHash() {
		return infoHash;
	}
	
	public String getHexInfoHash() {
		return ByteUtils.toHexString(this.infoHash);
	}
	
	public void setInfoHash(byte[] infoHash) {
		this.infoHash = infoHash;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public long getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}

	public long getLeft() {
		return left;
	}

	public void setLeft(long left) {
		this.left = left;
	}

	public long getUploaded() {
		return uploaded;
	}

	public void setUploaded(long uploaded) {
		this.uploaded = uploaded;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public int getNumWant() {
		return numWant;
	}

	public void setNumWant(int numWant) {
		this.numWant = numWant;
	}

	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	public void setPeerInfo(PeerInfo peerInfo) {
		this.peerInfo = peerInfo;
	}
}
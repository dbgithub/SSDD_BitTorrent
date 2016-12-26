package bitTorrent.peer.protocol;

import bitTorrent.util.ByteUtils;

/**
 * not interested: <len=0001><id=3>
 * 
 * The not interested message is fixed-length and has no payload.
 */

public class NotInterestedMsg extends PeerProtocolMessage {
	
	public NotInterestedMsg() {
		super(Type.NOT_INTERESTED);
		super.setLength(ByteUtils.intToBigEndianBytes(1, new byte[4], 0));		
	}
}

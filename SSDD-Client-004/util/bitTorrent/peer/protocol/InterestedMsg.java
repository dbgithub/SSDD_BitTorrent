package bitTorrent.peer.protocol;

import bitTorrent.util.ByteUtils;

/**
 * interested: <len=0001><id=2>
 * 
 * The interested message is fixed-length and has no payload
 */

public class InterestedMsg extends PeerProtocolMessage {
	
	public InterestedMsg() {
		super(Type.INTERESTED);
		super.setLength(ByteUtils.intToBigEndianBytes(1, new byte[4], 0));		
	}
}

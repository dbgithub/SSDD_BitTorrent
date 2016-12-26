package bitTorrent.peer.protocol;

import bitTorrent.util.ByteUtils;

/**
 * choke: <len=0001><id=0>
 * 
 * The choke message is fixed-length and has no payload.
 */

public class ChokeMsg extends PeerProtocolMessage {
	
	public ChokeMsg() {
		super(Type.CHOKE);
		super.setLength(ByteUtils.intToBigEndianBytes(1, new byte[4], 0));		
	}
}
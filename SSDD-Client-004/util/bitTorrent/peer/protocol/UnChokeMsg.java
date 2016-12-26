package bitTorrent.peer.protocol;

import bitTorrent.util.ByteUtils;

/**
 * unchoke: <len=0001><id=1>
 * 
 * The unchoke message is fixed-length and has no payload.
 */

public class UnChokeMsg extends PeerProtocolMessage {
	
	public UnChokeMsg() {
		super(Type.UNCHOKE);
		super.setLength(ByteUtils.intToBigEndianBytes(1, new byte[4], 0));		
	}
}

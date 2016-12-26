package bitTorrent.peer.protocol;

/**
 * port: <len=0003><id=9><listen-port>
 * 
 * The port message is sent by newer versions of the Mainline that implements a DHT tracker.
 * The listen port is the port this peer's DHT node is listening on.
 * This peer should be inserted in the local routing table (if DHT tracker is supported).
 */
import java.io.ByteArrayOutputStream;

import bitTorrent.util.ByteUtils;

public class PortMsg extends PeerProtocolMessage {
	
	private int port;
	
	public PortMsg(int port) {
		super(Type.PORT);
		super.setLength(ByteUtils.intToBigEndianBytes(3, new byte[4], 0));
		this.updatePayload(port);
		
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private void updatePayload(int port) {
		try {
			ByteArrayOutputStream payload = new ByteArrayOutputStream();

			payload.write(ByteUtils.intToBigEndianBytes(port, new byte[4], 0));
			
			super.setPayload(payload.toByteArray());
		} catch (Exception ex) {
			System.out.println("# Error updating PortMsg payload: " + ex.getMessage());
		}
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer(super.toString());
		buffer.append(" - PORT: ");
		buffer.append(this.port);
		
		return buffer.toString();
	}
}
package bitTorrent.tracker.protocol.udp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

/**
 *
 * Offset  Size            	Name            	Value
 * 0       64-bit integer  	connection_id   	0x41727101980
 * 8       32-bit integer  	action          	0 // connect
 * 12      32-bit integer  	transaction_id
 * 16
 * 
 */

public class ConnectRequest extends BitTorrentUDPRequestMessage {
	
	public ConnectRequest() {
		super(Action.CONNECT);
		super.setConnectionId(Long.decode("0x41727101980"));
	}
	
	@Override
	public byte[] getBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.putLong(0, super.getConnectionId());
		buffer.putInt(8, super.getAction().value());
		buffer.putInt(12, super.getTransactionId());
		
		buffer.flip();
			
		return buffer.array();
	}
	
	public static ConnectRequest parse(byte[] byteArray) {
		try {
		    ByteBuffer buffer = ByteBuffer.wrap(byteArray);
		    buffer.order(ByteOrder.BIG_ENDIAN);
		    
		    ConnectRequest msg = new ConnectRequest();
		    
		    msg.setConnectionId(buffer.getLong(0));
		    msg.setAction(Action.valueOf(buffer.getInt(8)));
		    msg.setTransactionId(buffer.getInt(12));
		    
		    return msg;
		} catch (Exception ex) {
			System.out.println("# Error parsing ConnectRequest message: " + ex.getMessage());
		}
	    
		return null;
	}
	
	public static void main(String[] args) {
		 Random random = new Random();
		 int transactionID = random.nextInt(Integer.MAX_VALUE);
		 
		 ConnectRequest connect = new ConnectRequest();
		 connect.setTransactionId(transactionID);
		 
		 System.out.println(connect.getAction() + " " + connect.getConnectionId() + " " + connect.getTransactionId());
		 
		 byte[] bytes = connect.getBytes();
		 
		 ConnectRequest connect2 = ConnectRequest.parse(bytes);
		 
		 System.out.println(connect2.getAction() + " " + connect2.getConnectionId() + " " + connect2.getTransactionId());
	}
}
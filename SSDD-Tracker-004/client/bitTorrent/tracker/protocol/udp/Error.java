package bitTorrent.tracker.protocol.udp;

/**
 * 
 * Offset  Size            	Name            	Value
 * 0       32-bit integer  	action          	3 // error
 * 4       32-bit integer  	transaction_id
 * 8       string  message
 * 
 */

public class Error extends BitTorrentUDPMessage {

	private String message;

	public Error() {
		super(Action.ERROR);
	}
	
	@Override
	public byte[] getBytes() {
		//TODO: Complete this method
			
		return null;
	}
	
	public static Error parse(byte[] byteArray) {
		//TODO: Complete this method
		
		return null;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

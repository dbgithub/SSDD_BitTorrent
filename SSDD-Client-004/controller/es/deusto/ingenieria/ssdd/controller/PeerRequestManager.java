package es.deusto.ingenieria.ssdd.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.Formatter;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import bitTorrent.peer.protocol.BitfieldMsg;
import bitTorrent.peer.protocol.Handsake;
import bitTorrent.peer.protocol.HaveMsg;
import bitTorrent.peer.protocol.PeerProtocolMessage;
import bitTorrent.peer.protocol.PieceMsg;
import bitTorrent.peer.protocol.PortMsg;
import bitTorrent.peer.protocol.RequestMsg;
import bitTorrent.util.ByteUtils;

/**
 * This class handles all messages that are received from another peer. All messages are taken into consideration: 'Hansake', BitField, ...
 * @author aitor & kevin
 *
 */
public class PeerRequestManager extends Thread{

	private MetainfoHandlerSingleFile torrent;
	public Date lastTimeReceivedMessage = null;
	private DataInputStream in;
	private DataOutputStream out;
	private Socket tcpSocket;
	private String peerID;
	volatile boolean cancel = false;
	
	//About this client
	private BitSet downloadedChunks;
	private ArrayList<BitSet> myBlockInfoByPiece;
	private RandomAccessFile downloadingFile;
	private byte[] currentPiece;
	
	//About the other peer
	private BitSet otherPeerChunks;
	private int interestedPiece = 0;
	private int pieceLength;
	private boolean sentBitfield = false;
	
	//State flags
	private boolean choked = true;
	private boolean interested = false;
	private int port;
	//Determines if we have received a Bitfield message, just after Handshake
	private boolean firstTime = true;

	public PeerRequestManager(Socket socket, MetainfoHandlerSingleFile torrent, RandomAccessFile downloadingFile, int piece, String peerid, BitSet donwloadedChunks) {
		try {
			//set socket timeout to two minutes if no message is received
			this.tcpSocket = socket;
		    this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
			this.downloadedChunks = donwloadedChunks;
			this.downloadingFile = downloadingFile;
			this.pieceLength = piece;
			this.peerID = peerid;
			this.torrent = torrent;
			this.start();
		} catch (IOException e) {
			System.err.println("# TCPConnection IO error:" + e.getMessage());
		}
	}

	public void run() {
		try {
			
			byte[] buffer = new byte[1024];
			int numberOfBytesReaded = this.in.read(buffer); // This will return -1 when the other end of the stream closes the TCP connection
			System.out.println("[PeerRequestManager] - Received data from '" + tcpSocket.getInetAddress().getHostAddress() + ":" + tcpSocket.getPort());		
			System.out.println("[PeerRequestManager] - Bytes read: "+ numberOfBytesReaded+ " bytes");
			//We record the time when received message
			lastTimeReceivedMessage = new Date();
			Handsake hansake = null;
			if (numberOfBytesReaded >= 0) {hansake = Handsake.parseHandsake(ByteUtils.subArray(buffer, 0, numberOfBytesReaded));} // If the peer at the other side of the TCP connection does not close the connection, the length of bytes read will be greater than 0
			if(hansake != null){
				System.out.println("[PeerRequestManager] - Handshake received!");
				// Obtaining information just in case
				InetAddress ip = tcpSocket.getInetAddress();
				int port = tcpSocket.getPort();
				String peerid = hansake.getPeerId();
				byte[] infohash = hansake.getInfoHash();
				System.out.println("		· HANDSHAKE RECEIVED PEERID : "+peerid);
				System.out.println("		· HANDSHAKE RECEIVED INFOHASH : "+ByteUtils.toHexString(infohash));
				System.out.println("		· HANDSHAKE RECEIVED FROM PORT: "+port);
				
				// Sending the response back to the peer:
				// We check whether this current peer received a Handshake message before. We do this by checking whether we have saved the port number of the peer's incoming message.
				if(ClientController.handsakeAlreadySent.containsKey(port)){
					// Now, we know that the peer of the incoming message was already sent a Handshake message before.
					// We have to check if the connection between these two peers was already established or it is in process.
					if(!ClientController.handsakeAlreadySent.get(port)){
						// Sending BitField message since the Handshake process ended successfully:
						ClientController.handsakeAlreadySent.put(port, true);
						ClientController.alreadyConnected.put(peerid, port);
						sentBitfield = true;
						System.out.println("[PeerRequestManager] - Sending BitField message...");
						BitfieldMsg bit = new BitfieldMsg(ByteUtils.bitSetToBytes(downloadedChunks));
						this.out.write(bit.getBytes());
					}
				}
				else if (!ClientController.alreadyConnected.containsKey(peerid)){
					// This means that current peer did not answer yet the Handshake message that was received from another peer at this end-point
					System.out.println("[PeerRequestManager] - Sending Handshake message back to the sender to eventually establish the connection...");
					ClientController.handsakeAlreadySent.put(port, true);
					ClientController.alreadyConnected.put(peerid, port);
					hansake.setPeerId(peerID+"          "); // I add spaces to fit the 20 size of the string int the Hansake message
					this.out.write(hansake.getBytes());
				} else {
					System.out.println("[PeerRequestManager] - Handshake skipped, no need to establish a connection, TCP connection already set between peers :)");
					cancel();
				}
				
				
				// Waits for other messages in this socket
				while(!cancel && !(tcpSocket.isClosed())){
					buffer = new byte[1024];
					numberOfBytesReaded = this.in.read(buffer);
					PeerProtocolMessage message = PeerProtocolMessage.parseMessage(buffer);
					switch (message.getType()) {
					case KEEP_ALIVE:
						// <len=0000>
						//Message to maintain the connection (due to Timeout)
						//Do nothing, just renew last message time
						if(!firstTime)
						{
							lastTimeReceivedMessage = new Date();
						}
						break;
					case CHOKE:
						// <len=0001><id=0>
						//This peers is choked, isn't allowed to request data
						if(!firstTime)
						{
							choked = true;
						}
						break;
					case UNCHOKE:
						// <len=0001><id=1>	
						//This peer is unchoked, is allowed again to request data
						if(!firstTime)
						{
							choked = false;
						}
						break;
					case INTERESTED:
						// <len=0001><id=2>
						//The other peer is interested, so will request blocks soon
						//(If it is unchoked)
						if(!firstTime)
						{
							interested = true;
						}
						break;
					case NOT_INTERESTED:
						// <len=0001><id=3>
						//The other peer isn't interested
						if(!firstTime)
						{
							interested = false;
						}
						break;
					case HAVE:
						// <len=0005><id=4><piece index>
						//Message when a peer download a piece and is validated 
						//TODO: Validate Index and drop the connection if it's out of bounds
						// Send request (INTERESTED) if this peer hasn't that piece
						if(!firstTime)
						{
							HaveMsg havemessage = (HaveMsg) message;
							int newpiece = havemessage.getIndex();
							//The downloaded piece must be between the range
							if(newpiece < otherPeerChunks.size() && newpiece >= 0){
								otherPeerChunks.set(newpiece);
							}
							else{
								//Drop connection
								System.out.println("Dropped connection: Piece Outofbounds");
								cancel();
							}
						}
						break;
					case BITFIELD:
						// <len=0001+X><id=5><bitfield>
						//Sent after the handshake (first message)
						//If we need some, send appropriate requests
						if(firstTime)
						{
							firstTime = false;
							System.out.println("[PeerRequestManager] - BitField received!");
							BitfieldMsg bitmessage = (BitfieldMsg) message;
							byte [] bitfield = bitmessage.getPayload();
							otherPeerChunks = ByteUtils.bytesToBitSet(bitfield);
							
							if(!sentBitfield){
								//Send message if I didn't send the Bitfield initial request
								BitfieldMsg newone = new BitfieldMsg(ByteUtils.bitSetToBytes(downloadedChunks));
								out.write(newone.getBytes());
							}
							
							//TODO: we should ask for the pieces that we don't have sending requests
							//Create thread to start sending requests
							System.out.println("[PeerRequestManager] - Creating PeerRequestCreator...");
							PeerRequestCreator prc = new PeerRequestCreator(downloadedChunks, otherPeerChunks, pieceLength, out, torrent);
							prc.start();
						}
						break;
					case REQUEST:
						// <len=0013><id=6><index><begin><length>
						//It is requesting one block, so we have to serve it 
						if(!firstTime)
						{
							RequestMsg requestmessage = (RequestMsg) message;
							int pieceIndex = requestmessage.getIndex();
							interestedPiece = pieceIndex;
							int begin = requestmessage.getBegin();
							int pieceL = requestmessage.getRLength();
							
							System.out.println("[PeerRequestManager] -  Asked for piece number "+pieceIndex+ " and block offset "+ begin);
							
							//Just to be sure, check if we have it
							if(downloadedChunks.get(pieceIndex)){
								System.out.println("[PeerRequestManager] -  I have that piece number: "+ pieceIndex);
								byte[] bufferFile = new byte[pieceL];
								downloadingFile.read(bufferFile, begin, pieceL);
								PieceMsg piece = new PieceMsg(pieceIndex, begin, bufferFile);
								this.out.write(piece.getBytes());
							}
							
						}
						break;
					case PIECE:
						// <len=0009+X><id=7><index><begin><block>
						if(!firstTime)
						{
							PieceMsg piecemessage = (PieceMsg) message;
							byte[] block = piecemessage.getBlock();
							int newpieceIndex = piecemessage.getIndex();
							
							//Add block to current piece download
							if(currentPiece == null){
								currentPiece = new byte[pieceLength/16384];
							}
							ByteUtils.concatenateByteArrays(currentPiece, block);
							//currentPiece.add(block);
							int numberOfBlock = piecemessage.getBegin() / 16384;
							
							//Setting block to downloaded (true)
							myBlockInfoByPiece.get(newpieceIndex).set(numberOfBlock);
							
							//Check if all the piece is downloaded
							if(myBlockInfoByPiece.get(newpieceIndex).cardinality() == myBlockInfoByPiece.get(newpieceIndex).size()){
								//Downloaded
								
								//TODO: CHECK COMPLETE PIECE HASH IF IT'S CORRECT (NOT SURE IF IT IS OK)
								byte [] hashPiece = ByteUtils.generateSHA1Hash(currentPiece);
								String correctHash = torrent.getMetainfo().getInfo().getHexStringSHA1().get(newpieceIndex);
								if(correctHash.equals(hashPiece)){
									//Write to the file
									downloadingFile.write(currentPiece, newpieceIndex*pieceLength, currentPiece.length);
									
									//Update downloaded
									downloadedChunks.set(newpieceIndex);
									int dimension = pieceLength/16384;
									currentPiece = new byte[dimension];
									interestedPiece = 0;
								}
							}
						}
						break;
					case CANCEL:
						// <len=0013><id=8><index><begin><length>
						//Cancel download of the block
						interestedPiece = 0;
						break;
					case PORT:
						// <len=0003><id=9><listen-port>
						//Sets listening port
						PortMsg portMsg = (PortMsg) message;
						port = portMsg.getPort();
						break;
					default:
						break;
					}
				}
			}
		
		} catch (EOFException e) {
			System.err.println("# TCPConnection EOF error" + e.getMessage());
		}catch(SocketTimeoutException e){
			System.out.println("# SocketTimeoutException error: No message received in 2 minutes.");
		} 
		catch (IOException e) {
			System.err.println("# TCPConnection IO error:" + e.getMessage());
		} finally {
			try {
				tcpSocket.close();
			} catch (IOException e) {
				System.err.println("# TCPConnection IO error:" + e.getMessage());
			}
		}
	}
	
	/**
	 * Cancel the thread
	 */
	public void cancel() {
        cancel = true;
        Thread.currentThread().interrupt(); // Since 'socket.receive(...)' is a blocking call, it might be useful to just directly call ".interrupt()" instead of waiting the while loop to realize that 'cancel' is not false anymore. 
    }
	
	public boolean isSet(byte[] arr, int bit) {
	    int index = bit / 8;  
	    int bitPosition = bit % 8;

	    return (arr[index] >> bitPosition & 1) == 1;
	}
	
	public static String SHAsum(byte[] convertme) throws NoSuchAlgorithmException{
	    MessageDigest md = MessageDigest.getInstance("SHA-1"); 
	    return byteArray2Hex(md.digest(convertme));
	}
	
	private static String byteArray2Hex(final byte[] hash) {
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    return formatter.toString();
	}
	
}

package es.deusto.ingenieria.ssdd.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.Formatter;

import javax.net.ssl.HandshakeCompletedEvent;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import bitTorrent.peer.protocol.BitfieldMsg;
import bitTorrent.peer.protocol.Handsake;
import bitTorrent.peer.protocol.HaveMsg;
import bitTorrent.peer.protocol.PeerProtocolMessage;
import bitTorrent.peer.protocol.PieceMsg;
import bitTorrent.peer.protocol.PortMsg;
import bitTorrent.peer.protocol.RequestMsg;
import bitTorrent.util.ByteUtils;

public class PeerRequestManager extends Thread{

	private MetainfoHandlerSingleFile torrent;
	public Date lastTimeReceivedMessage = null;
	private DataInputStream in;
	private DataOutputStream out;
	private Socket tcpSocket;
	private String peerID;
	
	//About this client
	private BitSet donwloadedChunks;
	private ArrayList<BitSet> myBlockInfoByPiece;
	private RandomAccessFile downloadingFile;
	private byte[] currentPiece;
	
	//About the other peer
	private BitSet otherPeerChunks;
	private int interestedPiece = 0;
	
	private int pieceLength;
	volatile boolean cancel = false;
	
	//State flags
	private boolean choked = true;
	private boolean interested = false;
	private int port;
	//Determines if we have received an Bitfield message yet, just after Handshake
	private boolean firstTime = true;

	public PeerRequestManager(Socket socket, MetainfoHandlerSingleFile torrent, RandomAccessFile downloadingFile, int piece, String peerid, BitSet donwloadedChunks) {
		try {
			//set socket timeout to two minutes if no message is received
			socket.setSoTimeout(120000);
			this.tcpSocket = socket;
		    this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
			this.donwloadedChunks = donwloadedChunks;
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
		//Echo server
		try {
			byte[] buffer = new byte[1024];
			int numberOfBytesReaded = this.in.read(buffer);
			System.out.println(" - Received data from '" + tcpSocket.getInetAddress().getHostAddress() + ":" + tcpSocket.getPort());		
			
			//We record the time when received message
			lastTimeReceivedMessage = new Date();
			
			Handsake hansake = Handsake.parseHandsake(buffer);
			if(hansake != null){
				System.out.println("- PeerRequestManager: Handshake received...");
				//Obtaining information
				InetAddress ip = tcpSocket.getInetAddress();
				int port = tcpSocket.getPort();
				String peerid = hansake.getPeerId();
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>"+port);
				byte[] infohash = hansake.getInfoHash();
				
				
				//sending response to the peer
				if(ClientController.handsakeAlreadySent.containsKey(port)){
					if(!(ClientController.handsakeAlreadySent.get(port))){
						//send bitfield if this sent the handshake
						ClientController.handsakeAlreadySent.put(port, true);
						System.out.println("- PeerRequestManager: Sending BitField");
						BitfieldMsg bit = new BitfieldMsg(ByteUtils.bitSetToBytes(donwloadedChunks));
						this.out.write(bit.getBytes());
					}
				}
				else{
					//this means that the peer hasn't sent the hanshake, so must respond to the received one
					ClientController.handsakeAlreadySent.put(port, true);
					hansake.setPeerId(peerID);
					this.out.write(hansake.getBytes());
				}
				
				
				//Start waiting for other messages in this socket
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
							System.out.println("PeerRequestManager: BitField received...");
							BitfieldMsg bitmessage = (BitfieldMsg) message;
							byte[] bytes = bitmessage.getBytes();
							otherPeerChunks = new BitSet(bytes.length);
							int index = 0;
							//Checking if bits of the bytes retrieved
							//Each bit represents a piece at the file
							while (index < bytes.length){
								if(isSet(bytes, index)){
									otherPeerChunks.set(index);
								}
							}
							//TODO: we should ask for the pieces that we don't have sending requests
							//Create thread to start sending requests
							
						}
						break;
					case REQUEST:
						// <len=0013><id=6><index><begin><length>
						//It is requesting one block, so we have to serve it 
						//TODO: Look for the piece that wants and send it 
						if(!firstTime)
						{
							RequestMsg requestmessage = (RequestMsg) message;
							int pieceIndex = requestmessage.getIndex();
							interestedPiece = pieceIndex;
							int begin = requestmessage.getBegin();
							int pieceL = requestmessage.getRLength();
							//Just to be sure, check if we have it
							if(donwloadedChunks.get(pieceIndex)){
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
									donwloadedChunks.set(newpieceIndex);
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

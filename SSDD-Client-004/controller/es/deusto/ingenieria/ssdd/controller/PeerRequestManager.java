package es.deusto.ingenieria.ssdd.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage;

import bitTorrent.peer.protocol.BitfieldMsg;
import bitTorrent.peer.protocol.Handsake;
import bitTorrent.peer.protocol.HaveMsg;
import bitTorrent.peer.protocol.PeerProtocolMessage;
import bitTorrent.peer.protocol.PieceMsg;
import bitTorrent.peer.protocol.PortMsg;
import bitTorrent.peer.protocol.RequestMsg;

public class PeerRequestManager extends Thread{

	public Date lastTimeReceivedMessage = null;
	private DataInputStream in;
	private DataOutputStream out;
	private Socket tcpSocket;
	
	//About this client
	private BitSet donwloadedChunks;
	private ArrayList<BitSet> myBlockInfoByPiece;
	private RandomAccessFile downloadingFile;
	private ArrayList<byte[]> currentPiece;
	
	//About the other peer
	private BitSet otherPeerChunks;
	private int interestedPiece = 0;
	
	private int pieceLength;
	volatile boolean cancel = false;
	
	//State flags
	private boolean choked = true;
	private boolean interested = false;
	private int port;

	public PeerRequestManager(Socket socket, RandomAccessFile downloadingFile, int piece) {
		try {
			//set socket timeout to two minutes if no message is received
			socket.setSoTimeout(120000);
			this.tcpSocket = socket;
		    this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
			this.downloadingFile = downloadingFile;
			this.pieceLength = piece;
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
				//Obtaining information
				InetAddress ip = tcpSocket.getInetAddress();
				int port = tcpSocket.getPort();
				String peerid = hansake.getPeerId();
				byte[] infohash = hansake.getInfoHash();
				
				//sending response to the peer
				this.out.write(hansake.getBytes());
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
						lastTimeReceivedMessage = new Date();
						break;
					case CHOKE:
						// <len=0001><id=0>
						//This peers is choked, isn't allowed to request data
						choked = true;
						break;
					case UNCHOKE:
						// <len=0001><id=1>	
						//This peer is unchoked, is allowed again to request data
						choked = false;
						break;
					case INTERESTED:
						// <len=0001><id=2>
						//The other peer is interested, so will request blocks soon
						//(If it is unchoked)
						interested = true;
						break;
					case NOT_INTERESTED:
						// <len=0001><id=3>
						//The other peer isn't interested
						interested = false;
						break;
					case HAVE:
						// <len=0005><id=4><piece index>
						//Message when a peer download a piece and is validated 
						//TODO: Validate Index and drop the connection if it's out of bounds
						// Send request (INTERESTED) if this peer hasn't that piece
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
						break;
					case BITFIELD:
						// <len=0001+X><id=5><bitfield>
						//Sent after the handshake (first message)
						//If we need some, send appropriate requests
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
						break;
					case REQUEST:
						// <len=0013><id=6><index><begin><length>
						//It is requesting one block, so we have to serve it 
						//TODO: Look for the piece that wants and send it 
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
						break;
					case PIECE:
						// <len=0009+X><id=7><index><begin><block>
						PieceMsg piecemessage = (PieceMsg) message;
						byte[] block = piecemessage.getBlock();
						int newpieceIndex = piecemessage.getIndex();
						
						//Add block to current piece download
						currentPiece.add(block);
						int numberOfBlock = piecemessage.getBegin() / 16384;
						
						//Setting block to downloaded (true)
						myBlockInfoByPiece.get(newpieceIndex).set(numberOfBlock);
						
						//Check if all the piece is downloaded
						if(myBlockInfoByPiece.get(newpieceIndex).cardinality() == myBlockInfoByPiece.get(newpieceIndex).size()){
							//Downloaded
							
							//Take all the blocks and put them in a single byte array
							int lengthtotal = 0;
							for(byte[] bytes2 : currentPiece){
								lengthtotal = lengthtotal + bytes2.length;
							}
							byte[] piecetotal = new byte[lengthtotal];
							ByteBuffer target = ByteBuffer.wrap(piecetotal);
							for(byte[] bytes2 : currentPiece){
								target.put(bytes2);
							}
							
							//Write to the file
							downloadingFile.write(target.array(), newpieceIndex*pieceLength, lengthtotal);
							
							//Update downloaded
							donwloadedChunks.set(newpieceIndex);
							currentPiece = new ArrayList<>();
							interestedPiece = 0;
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
	
}

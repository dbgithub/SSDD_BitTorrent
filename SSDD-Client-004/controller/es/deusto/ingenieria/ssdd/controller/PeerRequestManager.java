package es.deusto.ingenieria.ssdd.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

import bitTorrent.peer.protocol.Handsake;
import bitTorrent.peer.protocol.PeerProtocolMessage;
import bitTorrent.peer.protocol.PortMsg;

public class PeerRequestManager extends Thread{

	public Date lastTimeReceivedMessage = null;
	private DataInputStream in;
	private DataOutputStream out;
	private Socket tcpSocket;
	volatile boolean cancel = false;
	
	//State flags
	private boolean choked = true;
	private boolean interested = false;
	private int port;

	public PeerRequestManager(Socket socket) {
		try {
			//set socket timeout to two minutes if no message is received
			socket.setSoTimeout(120000);
			this.tcpSocket = socket;
		    this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
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
						// len=0001><id=2>
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
						//Message when a peer download an piece and is validated 
						//TODO: Validate Index and drop the connection if it's out of bounds
						// Send request (INTERESTED) if this peer hasn't that piece
						break;
					case BITFIELD:
						// <len=0001+X><id=5><bitfield>
						//Sent after the handshake (first message)
						//TODO: register the pieces that it has and check if we need those
						//If we need some, send appropriate requests
						break;
					case REQUEST:
						// <len=0013><id=6><index><begin><length>
						//It is requesting one block, so we have to serve it 
						//TODO: Look for the piece that wants and send it 
						break;
					case PIECE:
						// <len=0009+X><id=7><index><begin><block>
						break;
					case CANCEL:
						// <len=0013><id=8><index><begin><length>
						//Cancel download of the block
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
	
}

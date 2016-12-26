package es.deusto.ingenieria.ssdd.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;

import bitTorrent.peer.protocol.Handsake;
import bitTorrent.peer.protocol.PeerProtocolMessage;

public class PeerRequestManager extends Thread{

	private DataInputStream in;
	private DataOutputStream out;
	private Socket tcpSocket;
	volatile boolean cancel = false;

	public PeerRequestManager(Socket socket) {
		try {
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
			
			Handsake message = Handsake.parseHandsake(buffer);
			if(message != null){
				InetAddress ip = tcpSocket.getInetAddress();
				int port = tcpSocket.getPort();
				String peerid = message.getPeerId();
				byte[] infohash = message.getInfoHash();
				while(!cancel && !(tcpSocket.isClosed())){
					buffer = new byte[1024];
					
				}
			}
		
		} catch (EOFException e) {
			System.err.println("# TCPConnection EOF error" + e.getMessage());
		} catch (IOException e) {
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

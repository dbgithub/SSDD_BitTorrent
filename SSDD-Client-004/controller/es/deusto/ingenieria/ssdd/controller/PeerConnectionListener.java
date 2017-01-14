package es.deusto.ingenieria.ssdd.controller;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import bitTorrent.tracker.protocol.udp.AnnounceRequest;
import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage.Action;
import bitTorrent.tracker.protocol.udp.ConnectRequest;
import bitTorrent.tracker.protocol.udp.ConnectResponse;
import bitTorrent.tracker.protocol.udp.Error;
import bitTorrent.tracker.protocol.udp.PeerInfo;
import bitTorrent.tracker.protocol.udp.ScrapeInfo;
import bitTorrent.tracker.protocol.udp.ScrapeRequest;
import bitTorrent.tracker.protocol.udp.ScrapeResponse;
import bitTorrent.tracker.protocol.udp.AnnounceResponse;
import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage;

/**
 * This runnable class represents a thread that will be executed, taking care of the following:
 * · Declaring and using the ServerSocket that is passed as an argument
 * · For each incoming request from other peers, a new RequestManager (which is a listener) is created.
 * @author aitor & kevin
 *
 */
public class PeerConnectionListener implements Runnable {

	private ServerSocket peerListenerSocket;
	private RandomAccessFile downloadFile;
	private int piece;
	volatile boolean cancel = false;
	
//	THREAD
//	private Thread connectionCheckerThread; // Esto es necesario???
	
	
	public PeerConnectionListener(ServerSocket peerListenerSocket, RandomAccessFile downloadFile, int pieceLength) {
		this.peerListenerSocket = peerListenerSocket;
		this.downloadFile = downloadFile;
		this.piece = pieceLength;
	}
	
	@Override
	public void run() {
		while(!cancel) {				
			try {
				new PeerRequestManager(peerListenerSocket.accept(), downloadFile, piece);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} // END while
	}
	
	/**
	 * Cancel the thread
	 */
	public void cancel() {
        cancel = true;
        Thread.currentThread().interrupt(); // Since 'socket.receive(...)' is a blocking call, it might be useful to just directly call ".interrupt()" instead of waiting the while loop to realize that 'cancel' is not false anymore. 
    }
	
}
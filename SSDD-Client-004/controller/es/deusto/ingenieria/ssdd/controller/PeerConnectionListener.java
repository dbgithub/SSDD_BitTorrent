package es.deusto.ingenieria.ssdd.controller;

import java.io.IOException;
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
 * This runnable class represents a thread that will be executed, taking care of the folllowing:
 * · Create a multicast group
 * · Join the corresponding tracker (the one who is launching this thread) to the multicast group (if the tracker is the master, a new UDP socket will be opened to communicate with the peer).
 * · Handle incoming messages from the multicast group (send & receive):
 * 		· Create a ConnectResponse message
 * 		· Create an AnnounceResponse message
 * 		· Create a ScrapeRequest message
 * · Error handling
 * @author aitor & kevin
 *
 */
public class PeerConnectionListener implements Runnable {

	private ServerSocket peerListenerSocket;
	volatile boolean cancel = false;
	
	//THREAD
	private Thread connectionCheckerThread;
	
	
	public PeerConnectionListener(ServerSocket peerListenerSocket) {
		this.peerListenerSocket = peerListenerSocket;
	}
	
	@Override
	public void run() {
		while(!cancel) {				
			try {
				new PeerRequestManager(peerListenerSocket.accept());
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
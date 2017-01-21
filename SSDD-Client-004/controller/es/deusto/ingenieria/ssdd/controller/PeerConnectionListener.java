package es.deusto.ingenieria.ssdd.controller;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;

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
	private String peerID;
	private int piece;
	private MetainfoHandlerSingleFile torrent;
	volatile boolean cancel = false;
	
	
	public PeerConnectionListener(ServerSocket peerListenerSocket, MetainfoHandlerSingleFile single, RandomAccessFile downloadFile, int pieceLength, int peerid) {
		this.peerListenerSocket = peerListenerSocket;
		this.downloadFile = downloadFile;
		this.piece = pieceLength;
		this.peerID = peerid+"";
		this.torrent = single;
	}
	
	@Override
	public void run() {
		while(!cancel) {				
			try {
				new PeerRequestManager(peerListenerSocket.accept(), torrent, downloadFile, piece, peerID);
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
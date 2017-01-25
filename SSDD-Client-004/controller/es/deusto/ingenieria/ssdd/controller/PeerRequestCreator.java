package es.deusto.ingenieria.ssdd.controller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import bitTorrent.peer.protocol.RequestMsg;

public class PeerRequestCreator extends Thread{

	
	volatile boolean cancel = false;
	//Torrent
	private MetainfoHandlerSingleFile torrent;
	//Represents the pieces that I have
	private BitSet mine;
	//Represents the piece that the other peer has
	private BitSet otherPeer;
	private int pieceLength;
	//Output
	private DataOutputStream output;

	
	public PeerRequestCreator(BitSet mine, BitSet other, int pieceLength, DataOutputStream out, MetainfoHandlerSingleFile single) {
		this.mine = mine;
		this.otherPeer = other;
		this.pieceLength = pieceLength;
		this.output = out;
		this.torrent = single;
	}

	public void run() {
		System.out.println("[PeerRequestCreator] - Intialized!");
		System.out.println("[PeerRequestCreator] - Actual state - Mine: "+ mine.cardinality()+ " Other: "+ otherPeer.cardinality());
		while(!cancel){
			if(mine.cardinality() == mine.size()){
				//All pieces downloaded
				System.out.println("[PeerRequestCreator] - All downloaded");
				cancel = true;
			}
			else{
				//Ask for pieces to the peer
				int otherPeerCardinality = otherPeer.cardinality();
				for(int i =0; i < mine.size(); i++){
					if(!(mine.get(i))){
						//I don't have that piece, check if the other peer has it
						System.out.println("[PeerRequestCreator] - Piece number "+ i);
						if(otherPeer.get(i)){
							//It has it, so ask for it, block by block
							//Each block 16384 bytes
							int numberOfBlocks = pieceLength / 16384;
							for(int x=0; x < numberOfBlocks; x++){
								RequestMsg message = new RequestMsg(i, x*16384, 16384);
								try {
									output.write(message.getBytes());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								//Wait a bit for request the next one
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				}
				
				//Check if the number of pieces has changed. Stay unless
				int actualCardinality = otherPeerCardinality;
				do{
					try {
						System.out.println("[PeerRequestCreator] - Sleep and check then");
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					actualCardinality = otherPeer.cardinality();
				}while(otherPeerCardinality == actualCardinality);
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


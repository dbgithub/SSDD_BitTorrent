package controller;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import bitTorrent.metainfo.handler.MetainfoHandler;
import bitTorrent.metainfo.handler.MetainfoHandlerMultipleFile;
import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import bitTorrent.tracker.protocol.udp.AnnounceRequest;
import bitTorrent.tracker.protocol.udp.AnnounceRequest.Event;
import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage.Action;
import bitTorrent.tracker.protocol.udp.PeerInfo;
import main.TorrentInfoExtractor;

public class ClientController {
	
	public static boolean responseReceived = false;
	private static final int DEFAULT_PORT = 9000;
	
	public void startConnection(File torrentFile){
		//Create Object to extract the information related with the torrent file
		TorrentInfoExtractor tie = new TorrentInfoExtractor();
		//Extract information
		MetainfoHandler metaInfoFromTorrent = tie.extractInformationFromFile(torrentFile);
		
		//Check if is multiple or single file information
		//JUST DEVELOPED IF THE TORRENT CONTAINS ONE FILE (TODO: DEVELOP THE OTHER OPTION)
		if(metaInfoFromTorrent instanceof MetainfoHandlerMultipleFile){
			MetainfoHandlerMultipleFile multiple = (MetainfoHandlerMultipleFile) metaInfoFromTorrent;
			System.out.println("Obtained \n"+multiple.getMetainfo());
		}
		else if (metaInfoFromTorrent instanceof MetainfoHandlerSingleFile){
			MetainfoHandlerSingleFile single = (MetainfoHandlerSingleFile) metaInfoFromTorrent;
			System.out.println("Obtained \n"+single.getMetainfo());
			// Start with the connection to the tracker
			// First of all, send request to the multicast group. If the request doesn't have a response in 3 secs
			// we will send the request again until we receive it.
			do{
				sendRequestToMulticastGroup(single);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}while(responseReceived);
		}
	}

	private void sendRequestToMulticastGroup(MetainfoHandlerSingleFile single) {
		
		//I PUT THE PORT HARDCODED HERE AS STATIC VARIABLE
		try (DatagramSocket socket = new DatagramSocket(DEFAULT_PORT)) {
			InetAddress group = InetAddress.getByName(single.getMetainfo().getAnnounce());
			//socket.joinGroup(group);			
			
			AnnounceRequest ar = new AnnounceRequest();
			PeerInfo pI = ar.getPeerInfo();
			
			//I hardcode this values to prove the functionality. Will be removed in deeper development
			pI.setIpAddress(127001);
			pI.setPort(8000);
			ar.setConnectionId(11011);
			ar.setInfoHash(single.getMetainfo().getInfo().getInfoHash());
			ar.setPeerId("11011");
			ar.setEvent(Event.STARTED);
			ar.setAction(Action.ANNOUNCE);
			
			byte[] requestBytes = ar.getBytes();	
			DatagramPacket messageOut = new DatagramPacket(requestBytes, requestBytes.length, group, DEFAULT_PORT);
			socket.send(messageOut);
			
			System.out.println(" - Sent a message to '" + messageOut.getAddress().getHostAddress() + ":" + messageOut.getPort() + 
			                   "' -> " + new String(messageOut.getData()) + " [" + messageOut.getLength() + " byte(s)]");
		} catch (SocketException e) {
			System.err.println("# Socket Error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# IO Error: " + e.getMessage());
		}
	}

}

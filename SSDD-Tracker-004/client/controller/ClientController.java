package controller;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ThreadLocalRandom;

import bitTorrent.metainfo.handler.MetainfoHandler;
import bitTorrent.metainfo.handler.MetainfoHandlerMultipleFile;
import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import bitTorrent.tracker.protocol.udp.AnnounceRequest;
import bitTorrent.tracker.protocol.udp.ConnectRequest;
import bitTorrent.tracker.protocol.udp.ConnectResponse;
import bitTorrent.tracker.protocol.udp.AnnounceRequest.Event;
import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage.Action;
import es.deusto.ingenieria.ssdd.tracker.MulticastSocketTracker;
import bitTorrent.tracker.protocol.udp.PeerInfo;
import main.TorrentInfoExtractor;

public class ClientController {
	
	public static boolean ConnectResponseReceived = false;
	private ConnectResponse connectResponse; //Response for the first ConnectRequest
	private DatagramSocket multicastsocketSend; //Represents the socket for sending messages
	private DatagramSocket socketReceive; //Represents the socket for receiving messages
	private static long connectionId = 41727101980L;
	private static final int DESTINATION_PORT = 9000;
	
	//THREADS
	private ConnectionIdRenewer connectionRenewer;
	private Thread connectionRenewerThread;
	
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
			try {
				multicastsocketSend = new DatagramSocket();
				socketReceive = multicastsocketSend;
			}catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Let's send the ConnectionRequest and wait for the response (we try again if timeout reached)
			sendAndWaitUntilConnectResponseReceivedLoop(single, multicastsocketSend, socketReceive);
			
			System.out.println("Connection id: "+ connectionId);
			
			//Start a thread to renew connectionID after each minute of use
			connectionIdRenewer(multicastsocketSend, socketReceive, single);
			
			
		}
	}

	private void sendAnnounceRequest(MetainfoHandlerSingleFile single) {
		
		//I PUT THE PORT HARDCODED HERE AS STATIC VARIABLE
		try (DatagramSocket socket = new DatagramSocket()) {
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
			DatagramPacket messageOut = new DatagramPacket(requestBytes, requestBytes.length, group, DESTINATION_PORT);
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
	
	private void sendConnectRequest(MetainfoHandlerSingleFile single, DatagramSocket socket){
		try{
			InetAddress group = InetAddress.getByName(single.getMetainfo().getAnnounce());
			//socket.joinGroup(group);			
			
			ConnectRequest request = new ConnectRequest();
			request.setConnectionId(connectionId);
			request.setAction(Action.CONNECT);
			request.setTransactionId(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));
			
			byte[] requestBytes = request.getBytes();	
			DatagramPacket messageOut = new DatagramPacket(requestBytes, requestBytes.length, group, DESTINATION_PORT);
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
	
	public void sendAndWaitUntilConnectResponseReceivedLoop(MetainfoHandlerSingleFile single, DatagramSocket socketSend, DatagramSocket socketListen){
		try{
			//Let's set a timeout if the tracker doesn't response
			socketListen.setSoTimeout(3000);
			byte[] buffer = new byte[1024];
			boolean responseReceived = false;
			while(!responseReceived){     // recieve data until timeout
	            try {
	            	sendConnectRequest(single, socketSend);
	            	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
					//Stay blocked until the message is received or the timeout reached
	            	socketListen.receive(response);
	            	if(response.getLength() >= 16){
	            		connectResponse = ConnectResponse.parse(response.getData());
		            	connectionId = connectResponse.getConnectionId();
		            	responseReceived = true;
	            	}
	            }
	            catch (SocketTimeoutException e) {
	                // timeout exception.
	                System.out.println("Timeout reached!!! " + e);
	            } catch (IOException e) {
					e.printStackTrace();
				}
	        }
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
	}
	
	private void connectionIdRenewer(DatagramSocket send, DatagramSocket receive, MetainfoHandlerSingleFile single) {
		ConnectionIdRenewer ms = new ConnectionIdRenewer(send, receive, single, this);
		connectionRenewer = ms;
		connectionRenewerThread = new Thread(ms); 
		connectionRenewerThread.start();
	}
	

}

package es.deusto.ingenieria.ssdd.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import bitTorrent.metainfo.InfoDictionarySingleFile;
import bitTorrent.metainfo.handler.MetainfoHandler;
import bitTorrent.metainfo.handler.MetainfoHandlerMultipleFile;
import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import bitTorrent.tracker.protocol.udp.AnnounceRequest;
import bitTorrent.tracker.protocol.udp.ConnectRequest;
import bitTorrent.tracker.protocol.udp.ConnectResponse;
import bitTorrent.tracker.protocol.udp.AnnounceRequest.Event;
import bitTorrent.tracker.protocol.udp.AnnounceResponse;
import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage.Action;
import es.deusto.ingenieria.ssdd.classes.Swarm;
import bitTorrent.tracker.protocol.udp.PeerInfo;
import bitTorrent.tracker.protocol.udp.ScrapeInfo;
import bitTorrent.tracker.protocol.udp.ScrapeRequest;
import bitTorrent.tracker.protocol.udp.ScrapeResponse;

/**
 * This class handles most of the business logic of client side (peer side).
 * The communication with the cluster of trackers begins when a ConnectionRequest is sent to the tracker's IP.
 * To do so, it is necessary to read/load/submit a certain .torrent file from local file system. For that purpose, there exists
 * a .torrent file under the "torrent" folder in the project hierarchy.
 * This is a summary of the majority of methods/functions that this class controls:
 * 	· Create ConnectRequest + send ConnectRequest + wait for ConnectResponse
 * 	· Create AnnounceRequest + send AnnounceRequest + wait for AnnounceResponse
 * 	· Create ScrapeRequest + send ScrapeRequest + wait for ScrapeResponse
 * 	· 'startConnection' starts the whole process after the user choose a .torrent file 
 * @author aitor & kevin
 *
 */
public class ClientController {
	
	private ConnectResponse connectResponse; // Response for the first ConnectRequest
	private AnnounceResponse announceResponse; // Response of the AnnounceRequests
	private ScrapeResponse scrapeResponse; // Response of the ScrapeRequest
	private DatagramSocket multicastsocketSend; //Represents the socket for sending messages
	private DatagramSocket socketReceive; //Represents the socket for receiving messages
	private ServerSocket peerListenerSocket; //Socket for listening other peer connections
	
	private int idPeer = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE); // Random ID for the peer
	private InetAddress multicastIP;
	private static long connectionId = 41727101980L;
	private static int transactionID = -1;
	private static final int DESTINATION_PORT = 9000;
	private static int peerListenerPort;
	public static int interval;
	public static boolean ConnectResponseReceived = false;
	
	// List of swarms
	public HashMap<String, Swarm> torrents = new HashMap<>();
	// Information about each and every swarm (leechers, seeders and completed), ScrapeInfos:
	public ArrayList<ScrapeInfo> listScrapeInfo = new ArrayList<ScrapeInfo>();
	
	//THREADS
	private Thread connectionRenewerThread;
	private Thread downloadNotifierThread;
	
	public void startConnection(File torrentFile){
		//Create Object to extract the information related with the torrent file
		TorrentInfoExtractor tie = new TorrentInfoExtractor();
		//Extract information
		@SuppressWarnings("rawtypes")
		MetainfoHandler metaInfoFromTorrent = tie.extractInformationFromFile(torrentFile);
		
		//Check if is multiple or single file information
		//JUST DEVELOPED IF THE TORRENT CONTAINS ONE FILE (TODO: DEVELOP THE OTHER OPTION)
		if(metaInfoFromTorrent instanceof MetainfoHandlerMultipleFile){
			MetainfoHandlerMultipleFile multiple = (MetainfoHandlerMultipleFile) metaInfoFromTorrent;
			System.out.println("Obtained \n"+multiple.getMetainfo());
		}
		else if (metaInfoFromTorrent instanceof MetainfoHandlerSingleFile){
			MetainfoHandlerSingleFile single = (MetainfoHandlerSingleFile) metaInfoFromTorrent;
			System.out.println("Single file obtained: \n"+single.getMetainfo());
			System.out.println("-----------------------------------------------------------------");
			// Start with the connection to the tracker
			// First of all, send request to the multicast group. If the request doesn't have a response in 3 secs
			// we will send the request again until we receive it.
			if(multicastsocketSend == null){
				try {
					multicastsocketSend = new DatagramSocket();
					socketReceive = multicastsocketSend;
					peerListenerSocket = new ServerSocket();
					peerListenerPort = peerListenerSocket.getLocalPort();
				}catch (SocketException e) {
					System.out.println("ERROR: Error opening UDP sender/listener Socket.");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("ERROR: Error opening TCP listener Socket.");
					e.printStackTrace();
				}
			}
			//Let's send the ConnectionRequest and wait for the response (we try again if timeout reached)
			sendAndWaitUntilConnectResponseReceivedLoop(single, multicastsocketSend, socketReceive, true);

			System.out.println("Connection id: "+ connectionId);
			System.out.println("Transaction id: "+ connectResponse.getTransactionId());
			
			//Start a thread to renew connectionID after each minute of use
			createConnectionIdRenewer(multicastsocketSend, socketReceive, single);
			
			//Send the first AnnounceRequest related with the torrent
			System.out.println("PeerID: " +idPeer);
			sendAndWaitUntilAnnounceResponseReceivedLoop(single, multicastsocketSend, socketReceive, 0, 0, 0);
			
			System.out.println("AnnounceResponse: "+ announceResponse.getTransactionId());
			
			//Adding information about the swarm
			String urlInfohash = single.getMetainfo().getInfo().getHexInfoHash();
			String file = single.getMetainfo().getInfo().getName();
			int fileLength = single.getMetainfo().getInfo().getLength();
			Swarm s = new Swarm(urlInfohash, file, fileLength);
			s.setPeerList(announceResponse.getPeers());
			s.setTotalLeecher(announceResponse.getLeechers());
			s.setTotalSeeders(announceResponse.getSeeders());
			torrents.put(single.getMetainfo().getInfo().getHexInfoHash(), s);
			
			//Start a thread to notify the state of the download periodically
			createDownloadStateNotifier(single, multicastsocketSend, socketReceive);
			System.out.println("Received Peers...");
			for(Swarm s2 : torrents.values()){
				for(PeerInfo temporal: s2.getPeerList()){
					try {
						if(temporal.getIpAddress()!=0){
							System.out.println(convertIntToIP(temporal.getIpAddress()) + " "+ temporal.getPort());
						}
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Sends a ConnectRequest through the specified DatagramSocket
	 * @param single
	 * @param socket
	 * @param firstTime
	 */
	private void sendConnectRequest(MetainfoHandlerSingleFile single, DatagramSocket socket, boolean firstTime){
		try{
			multicastIP = InetAddress.getByName(single.getMetainfo().getAnnounce());
						
			//Create Message
			ConnectRequest request = createConnectRequest(firstTime);
			
			byte[] requestBytes = request.getBytes();	
			DatagramPacket messageOut = new DatagramPacket(requestBytes, requestBytes.length, multicastIP, DESTINATION_PORT);
			socket.send(messageOut);
			
			System.out.println(" - Sending ConnectRequest to '" + messageOut.getAddress().getHostAddress() + ":" + messageOut.getPort() + " [" + messageOut.getLength() + " byte(s)]...");			                   
		} catch (SocketException e) {
			System.err.println("# Socket Error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# IO Error: " + e.getMessage());
		}
	}
	
	/**
	 * Sends an AnnounceRequest through the specified DatagramSocket
	 * @param single
	 * @param socket
	 * @param downloaded
	 * @param left
	 * @param uploaded
	 */
	private void sendAnnounceRequest(MetainfoHandlerSingleFile single, DatagramSocket socket, long downloaded, long left, long uploaded){
		try{
			InfoDictionarySingleFile info = single.getMetainfo().getInfo();
			System.out.println("	· InfoHash: "+ info.getHexInfoHash());
			AnnounceRequest request = createAnnounceRequest(info.getInfoHash(), 0, info.getLength(), 0, Event.NONE, convertIpAddressToInt(peerListenerSocket.getInetAddress().getAddress()), peerListenerPort);
			
			byte[] requestBytes = request.getBytes();	
			DatagramPacket messageOut = new DatagramPacket(requestBytes, requestBytes.length, multicastIP, DESTINATION_PORT);
			socket.send(messageOut);
			
			System.out.println(" - Sending a AnnounceRequest to '" + messageOut.getAddress().getHostAddress() + ":" + messageOut.getPort() + " [" + messageOut.getLength() + " byte(s)]...");
		} catch (SocketException e) {
			System.err.println("# Socket Error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# IO Error: " + e.getMessage());
		}
	}
	
	/**
	 * Sends a ScrapeRequest through the specified DatagramSocket
	 */
	private void sendScrapeRequest(DatagramSocket socketSend) {
		ScrapeRequest request = createScrapeRequest();
		byte[] requestBytes = request.getBytes();
		DatagramPacket messageOut = new DatagramPacket(requestBytes, requestBytes.length,multicastIP, DESTINATION_PORT);
		System.out.println(" - UDP ScrapeRequest message sent to the multicast group. From " + socketSend.getLocalAddress().getHostAddress() +":"+
				socketSend.getLocalPort() + " to " + multicastIP.getHostAddress()+":"+DESTINATION_PORT +" (Bytes="+messageOut.getLength()+ ")");
		try {
			socketSend.send(messageOut);
		} catch (IOException e) {
			System.err.println("# Socket Error ('sendScrapeRequest'): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Waits for a ConnectResponse after creating and sending a ConnectRequest
	 * @param single
	 * @param socketSend
	 * @param socketListen
	 * @param firstime
	 */
	public void sendAndWaitUntilConnectResponseReceivedLoop(MetainfoHandlerSingleFile single, DatagramSocket socketSend, DatagramSocket socketListen, boolean firstime){
		try{
			//Let's set a timeout if the tracker doesn't response
			socketListen.setSoTimeout(3000);
			byte[] buffer = new byte[1024];
			boolean responseReceived = false;
			while(!responseReceived){     // Receive data until timeout
	            try {
	            	sendConnectRequest(single, socketSend, firstime);
	            	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
					//Stay blocked until the message is received or the timeout reached
	            	socketListen.receive(response);
	            	//Size validation
	            	if(response.getLength() >= 16){
	            		connectResponse = ConnectResponse.parse(response.getData());
	            		//Parse and Action Validation
	            		if(connectResponse != null){
	            			if(connectResponse.getAction().equals(Action.CONNECT)){
	            				//TransactionId validation
	            				if(connectResponse.getTransactionId() == transactionID){
	            					connectionId = connectResponse.getConnectionId();
					            	responseReceived = true;
	            				}
		            		}
	            		}
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
	
	/**
	 * Waits for a AnnounceResponse after creating and sending an AnnounceRequest
	 * @param single
	 * @param socketSend
	 * @param socketListen
	 * @param downloaded
	 * @param left
	 * @param uploaded
	 */
	public void sendAndWaitUntilAnnounceResponseReceivedLoop(MetainfoHandlerSingleFile single,
			DatagramSocket socketSend, DatagramSocket socketListen, long downloaded, long left, long uploaded) {
		try{
			//Let's set a timeout if the tracker doesn't response
			socketListen.setSoTimeout(3000);
			byte[] buffer = new byte[1024];
			boolean responseReceived = false;
			while(!responseReceived){     // recieve data until timeout
	            try {
	            	sendAnnounceRequest(single, socketSend, 0, 0, 0);
	            	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
					//Stay blocked until the message is received or the timeout reached
	            	socketListen.receive(response);
	            	//Size validation
	            	if(response.getLength() >= 16){
	            		announceResponse = AnnounceResponse.parse(response.getData());
	            		//Parse and Action Validation
	            		if(announceResponse != null){
	            			//TransactionId validation
	            			if(announceResponse.getTransactionId() == transactionID){
		            			responseReceived = true;
		            			interval = announceResponse.getInterval();
		            		}
	            		}
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
	
	/**
	 * Waits for a ScrapeResponse after creating and sending a ScrapeRequest
	 */
	public void sendAndWaitUntilScrapeResponseReceivedLoop() {
		try {
			socketReceive.setSoTimeout(3000); //Let's set a timeout if the tracker doesn't response
			byte[] buffer = new byte[1024];
			boolean responseReceived = false;
			while(!responseReceived) {
				sendScrapeRequest(multicastsocketSend);
				DatagramPacket response = new DatagramPacket(buffer, buffer.length);
				try {
					socketReceive.receive(response); // We wait until a response is received
					if (response.getLength() >= 4) { 
						scrapeResponse = ScrapeResponse.parse(response.getData());
						if (scrapeResponse != null) {
							if (scrapeResponse.getAction().equals(Action.SCRAPE)) {
								if (scrapeResponse.getTransactionId() == transactionID) {
									responseReceived = true;
									listScrapeInfo = (ArrayList<ScrapeInfo>) scrapeResponse.getScrapeInfos();
									System.out.println("ScrapeResponse received! The list of ScrapeInfo has been saved in memory!");
								}
							}
						}
					}
				} catch (SocketTimeoutException e1) {
					System.out.println("ERROR: Timeout exception in 'ScrapeResponseReceivedLoop'");
				} 
			}
		} catch (IOException e) {
			System.out.println("ERROR occurred receiving from the listening socket in 'ScrapeResponseReceivedLoop'");
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates an AnnounceRequest given the values in the parameters
	 * @param infoHash
	 * @param downloaded
	 * @param left
	 * @param uploaded
	 * @param event
	 * @param ipaddress
	 * @param port
	 * @return
	 */
	private AnnounceRequest createAnnounceRequest(byte[]infoHash, long downloaded, long left, int uploaded, Event event, int ipaddress, int port){
		AnnounceRequest temp = new AnnounceRequest();
		temp.setConnectionId(connectionId);
		temp.setAction(Action.ANNOUNCE);
		temp.setTransactionId(transactionID);
		temp.setPeerId(idPeer+"");
		temp.setInfoHash(infoHash);
		temp.setDownloaded(downloaded);
		temp.setLeft(left);
		temp.setUploaded(uploaded);
		temp.setEvent(event);
		PeerInfo pi = new PeerInfo();
		pi.setIpAddress(ipaddress);
		pi.setPort(port);
		temp.setPeerInfo(pi);
		temp.setKey(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));
		temp.setNumWant(-1);
		return temp;
		
	}
	
	/**
	 * Creates a ConnectRequest given the values in the parameters
	 * @param firstTime
	 * @return
	 */
	private ConnectRequest createConnectRequest(boolean firstTime){
		ConnectRequest request = new ConnectRequest();
		request.setAction(Action.CONNECT);
		//If it's the first time, first connection, we should generate transaction ID.
		//Unless it is, we use the generated one
		if(firstTime){
			transactionID = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
		}
		request.setConnectionId(41727101980L);
		request.setTransactionId(transactionID);
		return request;
		
	}
	
	/**
	 * Creates a ScrapeRequest
	 * @return
	 */
	private ScrapeRequest createScrapeRequest() {
		ScrapeRequest scrape = new ScrapeRequest();
		scrape.setConnectionId(connectionId);
		scrape.setTransactionId(transactionID);
		scrape.setAction(Action.SCRAPE);
		int j = 0;
		for (String s : this.torrents.keySet()) {
			if (j < 74) {
				scrape.addInfoHash(s);
				System.out.println("Adding infohash to the ScrapeRequest message's list!");
				j++;				
			} else {
				break; // We cannot request information about more than 74 swarms, that's the limit! :)
			}
		}
		return scrape;
	}
	
	/**
	 * Launches a thread to renew the connection ID of the peer every minute (60seconds)
	 * @param send
	 * @param receive
	 * @param single
	 */
	private void createConnectionIdRenewer(DatagramSocket send, DatagramSocket receive, MetainfoHandlerSingleFile single) {
		connectionRenewerThread = new Thread(new ConnectionIdRenewer(send, receive, single, this)); 
		connectionRenewerThread.start();
	}
	
	/**
	 * Launches a thread to notify the cluster of trackers the state of the downloaded content of the .torrent file.
	 * Apart from sending a periodically notification of the state, the peer is ALSO interested in obtaining a list of peers 
	 * who are also seeding or downloading the .torrent file
	 * @param single
	 * @param send
	 * @param receive
	 */
	private void createDownloadStateNotifier(MetainfoHandlerSingleFile single, DatagramSocket send,
			DatagramSocket receive) {
		downloadNotifierThread = new Thread(new DownloadStateNotifier(send, receive, single, this)); 
		downloadNotifierThread.start();
	}
	
	// THIS METHOD IS FOR THE NEAR FUTURE. WE ARE NOT USING IT IN THIS PART OF THE PROJECT
	private InetAddress convertIntToIP(int ip) throws UnknownHostException{
		byte[] bytes = BigInteger.valueOf(ip).toByteArray();
		InetAddress address = InetAddress.getByAddress(bytes);
		return address;
	}
	
	public static int convertIpAddressToInt(byte[] ip){
		
		int result = 0;
		if(ip != null){
			for (byte b: ip)  
			{  
			    result = result << 8 | (b & 0xFF);  
			}
		}
		return result;
		
	}


	/**
	 * If Client side doesn't have a transaction ID set yet, then we cannot proceed with the ScrapeRequest message
	 * The GUI calls this method to ensure that transaction ID is different from -1 (initial value)
	 * @return returns transaction ID
	 */
	public static int getTransactionID() {
		return transactionID;
	}

}

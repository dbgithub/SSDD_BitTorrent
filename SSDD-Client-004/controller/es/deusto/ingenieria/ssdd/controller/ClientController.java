package es.deusto.ingenieria.ssdd.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import bitTorrent.metainfo.InfoDictionarySingleFile;
import bitTorrent.metainfo.handler.MetainfoHandler;
import bitTorrent.metainfo.handler.MetainfoHandlerMultipleFile;
import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import bitTorrent.peer.protocol.Handsake;
import bitTorrent.tracker.protocol.udp.AnnounceRequest;
import bitTorrent.tracker.protocol.udp.ConnectRequest;
import bitTorrent.tracker.protocol.udp.ConnectResponse;
import bitTorrent.tracker.protocol.udp.AnnounceRequest.Event;
import bitTorrent.tracker.protocol.udp.AnnounceResponse;
import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage.Action;
import es.deusto.ingenieria.ssdd.classes.Peer;
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
	private MetainfoHandlerSingleFile torrent;
	private RandomAccessFile file;
	public static boolean ConnectResponseReceived = false;
	private FileAllocateUtil downloaded;
	
	// List of swarms
	public HashMap<String, Swarm> torrents = new HashMap<>();
	// Information about each and every swarm (leechers, seeders and completed), ScrapeInfos:
	public ArrayList<ScrapeInfo> listScrapeInfo = new ArrayList<ScrapeInfo>();
	// List of peers for each swarm
	public HashMap<String, ArrayList<Peer>> listPeers = new HashMap<>();
	// An auxiliary list that maintains a control of the number of peers  to whom the Handsake has been sent:
	private HashMap<String, Integer> auxListPeers = new HashMap<>();
	// This map determines to whom the handsake message has been sent. KEY: port, VALUE: boolean (either true or false)  
	// If the VALUE is FALSE, then the first Hansake message was sent but an answer was not received yet!
	// If the VALUE is TRUE, then the Handsake message was received from that peer to whom the Handsake was sent.
	public static HashMap<Integer, Boolean> handsakeAlreadySent = new HashMap<>();
	public static HashMap<Integer, Boolean> alreadyConnected = new HashMap<>();
	
	//THREADS
	private Thread connectionRenewerThread;
	private Thread downloadNotifierThread;
	private Thread peerConnectionThread;
	
	public void startConnection(File torrentFile){
		//Create Object to extract the information related with the torrent file
		TorrentInfoExtractor tie = new TorrentInfoExtractor();
		//Extract information
		@SuppressWarnings("rawtypes")
		MetainfoHandler metaInfoFromTorrent = tie.extractInformationFromFile(torrentFile);
		
		//Check if is multiple or single file information
		if(metaInfoFromTorrent instanceof MetainfoHandlerMultipleFile){
			MetainfoHandlerMultipleFile multiple = (MetainfoHandlerMultipleFile) metaInfoFromTorrent;
			System.out.println("Obtained \n"+multiple.getMetainfo());
		}
		else if (metaInfoFromTorrent instanceof MetainfoHandlerSingleFile){
			MetainfoHandlerSingleFile single = (MetainfoHandlerSingleFile) metaInfoFromTorrent;
			torrent = single;
			System.out.println("Single file obtained: \n"+single.getMetainfo());
			System.out.println("-----------------------------------------------------------------");
			// Start with the connection to the tracker
			// First of all, send request to the multicast group. If the request doesn't have a response in 3 secs
			// we will send the request again until we receive it.
			if(multicastsocketSend == null){
				try {
					multicastsocketSend = new DatagramSocket();
					socketReceive = multicastsocketSend;
					//This is mandatory because in Announce Messages the port
					//Is represented with a Short, so max. value is 32767.
					//We can't choose randomly or leave S.O to choose
					Random rn = new Random();
					int random = 0 + rn.nextInt(32766 - 0 + 1);
					boolean itsfreeport = false;
					while(!itsfreeport){
						try{
							peerListenerSocket = new ServerSocket(random);
				            itsfreeport = true;
				        }
						catch(Exception ex){
							itsfreeport = false;
						}
					}
					peerListenerPort = peerListenerSocket.getLocalPort();
				}catch (SocketException e) {
					System.out.println("ERROR: Error opening UDP sender/listener Socket.");
					e.printStackTrace();
				}
			}
			//Allocate space
			FileAllocateUtil fd = new FileAllocateUtil(single);
			downloaded = fd;
			RandomAccessFile fileAllocated = null;
			try {
				fileAllocated = fd.getFileAllocated();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			file = fileAllocated;
			
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
			String infohash = single.getMetainfo().getInfo().getHexInfoHash();
			String file = single.getMetainfo().getInfo().getName();
			int fileLength = single.getMetainfo().getInfo().getLength();
			Swarm s = new Swarm(infohash, file, fileLength);
			s.setPeerList(announceResponse.getPeers());
			s.setTotalLeecher(announceResponse.getLeechers());
			s.setTotalSeeders(announceResponse.getSeeders());
			torrents.put(infohash, s);
			processReceivedPeerList(announceResponse.getPeers()); // This just displays (shows) a list of peers in the console.
					
			// Start connecting to peers...
			// Let's launch a thread using the ServerSocket initialized before, to wait for incoming Peer's connections and requests:
			peerConnectionThread = new Thread(new PeerConnectionListener(peerListenerSocket, single, fileAllocated, single.getMetainfo().getInfo().getPieceLength(),idPeer, downloaded.getDonwloadedChunks()));
			peerConnectionThread.start();

			//Start a thread to notify the state of the download periodically
			createDownloadStateNotifier(single, multicastsocketSend, socketReceive);	

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
			AnnounceRequest request = createAnnounceRequest(info.getInfoHash(), 0, info.getLength(), 0, Event.NONE, convertIpAddressToInt(InetAddress.getLocalHost().getAddress()), peerListenerPort);
			
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
		            			updateSwarmInformation(single.getMetainfo().getInfo().getHexInfoHash(), announceResponse); // We can make use of 'MetainfoHandlerSingleFile' because at this point we have confirmed the TransactionID
		            			processReceivedPeerList(announceResponse.getPeers());
		            			connectToPeers();
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
	
	private void updateSwarmInformation(String infohash, AnnounceResponse res) {
		if (torrents.get(infohash) != null) {
			torrents.get(infohash).setPeerList(res.getPeers());
			torrents.get(infohash).setTotalLeecher(res.getLeechers());
			torrents.get(infohash).setTotalSeeders(res.getSeeders());
		}
	}
	private void processReceivedPeerList(List<PeerInfo> peerlist) {
		System.out.println("Displaying the received list of Peers from tracker...");
		ArrayList<Peer> tempListPeers;
		for(Swarm s2 : torrents.values()){
			// Checking whether we already knew about this list of peers:
			if (listPeers.containsKey(s2.getInfoHash())) {
				tempListPeers = listPeers.get(s2.getInfoHash());
			} else {
				tempListPeers = new ArrayList<Peer>();
			}
			for(PeerInfo temporal: s2.getPeerList()){
				try {
					if(temporal.getIpAddress()!=0){
						Peer temp_peer = new Peer(convertIntToIP(temporal.getIpAddress()), temporal.getPort());
						// We add the Peer to the list of peers corresponding the current Swarm (InfoHash)
						if (!tempListPeers.contains(temp_peer)) {tempListPeers.add(temp_peer);}
						// Print the peer in the console:
						if(temporal.getIpAddress() == convertIpAddressToInt(InetAddress.getLocalHost().getAddress()) && temporal.getPort() == peerListenerPort){
							System.out.println("		· (YOU, current Peer) "+convertIntToIP(temporal.getIpAddress()) + ":"+ temporal.getPort());
							continue;
						}
						System.out.println("		· "+convertIntToIP(temporal.getIpAddress()) + ":"+ temporal.getPort());
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			} // END PeerInfo loop
			// Now, it's time to relate the list of peers that we have captured with the Swarm (InfoHash):
			listPeers.put(s2.getInfoHash(), tempListPeers); // If the old key already existed, then it's replaced.
		} // END Swarm loop
		
		
		// Just checking if the peers are saved correctly into the list:
//		System.out.println("SIZE of listPeers:" + listPeers.size());
//		for(String infohash : listPeers.keySet()){
//			for(Peer peer: listPeers.get(infohash)){
//				System.out.println("INFOHASH -> " + infohash);
//				System.out.println("PEER.IP -> " + peer.getIp().getHostAddress());
//				System.out.println("PEER.port -> " + peer.getPort());
//			} // END PeerInfo loop
//		} // END
		
		
	}
	
	/**
	 * For every swarm and for every list of peers within that swarm, a TCP connection is established.
	 */
	private void connectToPeers() {
		for (String infohash : torrents.keySet()) {
			// With the following IF we are trying to identify whether exists or not a new peer for current Swarm (apart from the ones)
			// who were already in the list of peers. If yes, then it means that we need to establish a connection with him/her, otherwise,
			// it means that all peers of the list for that Swarm were already sent the Handsake messages.
			if (auxListPeers.containsKey(infohash)) {
				if (listPeers.get(infohash).size() == auxListPeers.get(infohash)) {System.out.println("CONTINUE!!!!!!!!"); continue; }
			}
			for (Peer peer : listPeers.get(infohash)) {
				if (listPeers.get(infohash).size() == 1) {System.out.println("BREAAAAKKKKKK!!!!!!!!");break;} // Here we skip sending intentionally a Handsake to ourselves
				try {
					if(peer.getIp().getHostAddress().equals(InetAddress.getLocalHost().getHostAddress())  && peer.getPort() == peerListenerPort){
						System.out.println("Skipped sending the Handsake to myself! :)");
						continue;
					}
					if(!(ClientController.handsakeAlreadySent.containsKey(peer.getPort()))){
						Socket socket = new Socket(peer.getIp().getHostAddress(), peer.getPort());
						PeerRequestManager prm = new PeerRequestManager(socket, torrent, file, torrent.getMetainfo().getInfo().getPieceLength(),idPeer+"", downloaded.getDonwloadedChunks());
						//prm.start();
						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						// The first message that has to be sent to the peer it's Handsake type:
						System.out.println(" - Sending a Handsake to '" + peer.getIp().getHostAddress() + ":" + peer.getPort() + " (InfoHash:" + infohash + ")...");
						Handsake outgoing_message = new Handsake();
						outgoing_message.setInfoHash(infohash.getBytes());
						outgoing_message.setPeerId(String.valueOf(idPeer));
						out.write(outgoing_message.getBytes());
						System.out.println("********************"+ peer.getPort());
						handsakeAlreadySent.put(peer.getPort(), false); // Here, we are indicating that we have affirmatively sent the Handsake mesage the mentioned Peer, but, an answer was not received back yet!
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
			auxListPeers.put(infohash, listPeers.get(infohash).size());
		}
	}

}

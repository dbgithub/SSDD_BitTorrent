package es.deusto.ingenieria.ssdd.tracker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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
import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.classes.PeerTorrent;
import es.deusto.ingenieria.ssdd.classes.Swarm;
import es.deusto.ingenieria.ssdd.data.DataModelPeer;
import es.deusto.ingenieria.ssdd.data.DataModelSwarm;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;

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
public class MulticastSocketTracker implements Runnable {

	private MulticastSocket socketMulticast; // Socket for the multicast group. This will send messages to the members from within the multicast group, plus receive messages from peers.
	private DatagramSocket socketUDP; // Socket for the UDP socket, NOT multicast. This will send message to the peers (the node/party) in the otehr side of the communication socket.
	private InetAddress group;
	private DatagramPacket incomingMessage;
	volatile boolean cancel = false;
	private boolean ismaster;
	private DataModelPeer dmp;
	private DataModelTracker dmt;
	private DataModelSwarm dms;
	private int interval = 15; // number of seconds to wait before receiving another AnnounceRequest from a peer.
	
	//THREAD
	private Thread connectionCheckerThread;
	
	
	public MulticastSocketTracker(int port, String IP, boolean ismaster, DataModelTracker dmt, DataModelSwarm dms, DataModelPeer dmp) {
		this.dmt = dmt;
		this.dms = dms;
		this.dmp = dmp;
		try {
			this.socketMulticast = new MulticastSocket(port);
			this.group = InetAddress.getByName(IP);
			this.socketMulticast.joinGroup(group);
		} catch (IOException e) {
			System.out.println("ERROR creating and/or joining a multicast group in 'MulticastSocketTracker'!");
			e.printStackTrace();
		};
		this.incomingMessage = null;
		this.ismaster = ismaster;
		if (ismaster) {
			// Only when the tracker who is launching this thread is the master, this code will be executed.
			// Since the tracker master needs a direct communication between him and the peer, it is necessary to create a datagram socket
			// in order to send UDP messages to the peer.
			try {
				socketUDP = new DatagramSocket();
				System.out.println("UDP (DatagramSocket) vanilla socket created successfully!");
			} catch (SocketException e) {
				System.out.println("ERROR creating a datagram socket in 'MulticastSocketTracker'!");
				e.printStackTrace();
			}
		}
		startConnectionIdChecker();
	}
	
	@Override
	public void run() {
		while(!cancel) {
			System.out.println("-----------------------------------------------------------------");
			System.out.println("MulticastSocket: waiting for incoming messages...");
			byte[] buffer = new byte[1024]; 
			this.incomingMessage = new DatagramPacket(buffer, buffer.length);
			try {
				this.socketMulticast.receive(incomingMessage);
				System.out.println("UDP message arrived at Multicast group! :)");
				System.out.println("	Sender's IP: " + incomingMessage.getAddress().getHostAddress()); // This might be either an IP from the multicast group or peer's IP.
				System.out.println("	Sender's Port: " + incomingMessage.getPort()); // This might be either an port from the multicast group or peer's port.
				System.out.println("	Sender's message length: " + incomingMessage.getLength());				
				
				if (!incomingMessage.getAddress().equals(group)) {
					// In this case, the incoming message comes from an IP different from any tracker's IP, then, it is NOT a message from within the multicast group.
					// Since we are interested in the incoming peers' IPs, we will save any interesting data:		
					String ip = incomingMessage.getAddress().getHostAddress();
					int originPort = incomingMessage.getPort();
					int destinationPort = originPort;
					switch(incomingMessage.getLength()){
						case 16: //If length is 16 bytes, then it's a ConnectRequest
							ConnectRequest request = ConnectRequest.parse(incomingMessage.getData());
							if(request != null){
								long connectionId = request.getConnectionId();
								int transacctionId = request.getTransactionId();
								Action action = request.getAction();
								
								if(connectionId == 41727101980L){
									if(action.compareTo(Action.CONNECT) == 0){
										System.out.println("The UDP message received was a ConnectRequest!");
										//Then, this means the first connection between the peer and the tracker
										//So, we have to add the peer to a list and response back to the peer if we are the master
										if(!(dmp.peerlist.containsKey(transacctionId))){
											//First, create a peer.
											Peer p = new Peer();
											p.setIp(ip);
											p.setAnnounceRequest_lastupdate(null);
											p.setPort(destinationPort);
											p.setTransaction_id(transacctionId);
											p.setConnection_id_lastupdate(new Date());
											dmp.peerlist.put(p.getTransaction_id(), p);
											System.out.println("		·The transaction_id of the peer is: "+ p.getTransaction_id());
											// If the current tracker is the master. Then, it has to response to the peer with a connection_id
											if(ismaster){
												//Create Response
												ConnectResponse response = prepareConnectResponse(transacctionId);
												p.setConnection_id(response.getConnectionId());
												System.out.println("		·ConnectionID "+ p.getConnection_id());
												
												//Once the message is created, we send it	
												sendUDPMessage(response, ip, destinationPort);
												System.out.println("		·Sending ConnectResponse UDP message back to the peer...");
											}	
										}
										else{
											//Then is trying to renew its connectionId
											if (ismaster) {
												//We have to response to the peer with a new connection_id
												Peer selected = dmp.peerlist.get(transacctionId);
												ConnectResponse response = prepareConnectResponse(transacctionId);
												selected.setConnection_id(response.getConnectionId());
												selected.setConnection_id_lastupdate(new Date());
												//Once is created the message, we send it	
												sendUDPMessage(response, ip, destinationPort);	
											}
										}
									}
									else{
										if(ismaster){sendError("[ActionIncorrectError]: Action incorrect, should be ANNOUNCE.", transacctionId, ip, destinationPort);} // Send error regarding ACTION incorrect (connect)
									}
								}
								else{
									if (ismaster) {sendError("[ConnectionIdIncorrectError]: The ConnectionId doesn't match", transacctionId, ip, destinationPort);} //Send error regarding connection ID incorrect
								}
							}
							else{
								if (ismaster) {sendError("[ParsingError]: Error parsing the ConnectRequest message. Try to send it again.", 0, ip, destinationPort);} //Send error regarding transactionId incorrect
							}
							break;
						case 98: //If length is 98 bytes, then it's an AnnounceRequest
							AnnounceRequest announcerequest = AnnounceRequest.parse(incomingMessage.getData());
							if(announcerequest != null){
								System.out.println("The UDP message received was a AnnounceRequests!");
								System.out.println("		·InfoHash: "+announcerequest.getHexInfoHash());
								String infoHash = announcerequest.getHexInfoHash();
								int transacctionIdA = announcerequest.getTransactionId();
								long connectionIdA = announcerequest.getConnectionId();
								long downloaded = announcerequest.getDownloaded();
								long uploaded = announcerequest.getUploaded();
								long left = announcerequest.getLeft();
								int port = announcerequest.getPeerInfo().getPort();
								int ipPeer = announcerequest.getPeerInfo().getIpAddress();
								Peer temp = dmp.peerlist.get(transacctionIdA);
								System.out.println("		·Received PEER ID: " + Integer.parseInt(announcerequest.getPeerId().trim()));
								if(temp != null){
									// This means that the communication is going on the right path.
									// The tracker knew about the transaction ID, so we continue with the process.
									// Check if the information is coherent:
									boolean connectionIDcorrect = true; // by default is true
									
									//Set the correct IP and port for connections of peers
									temp.setIp(InetAddress.getByAddress(convertIntToBytes(ipPeer)).getHostAddress());
									temp.setPort(port);
									
									if (ismaster) {
										if (temp.getConnection_id() != connectionIdA) {connectionIDcorrect = false;}
									}
									if (!connectionIDcorrect) {
										if (ismaster) {sendError("[ConnectionIdIncorrectError]: The ConnectionId doesn't match", transacctionIdA, ip, destinationPort);} // Send error regarding connection ID incorrect
									} else {
										System.out.println("		·ConnectionID CORRECT");
										if(announcerequest.getAction().compareTo(Action.ANNOUNCE) == 0){
											System.out.println("		· >>>>> Action = ANNOUNCE");
											if(temp.getAnnounceRequest_lastupdate() == null){
												// This means that it is the first time in receiving an AnnounceRequest.
												temp.setId(Integer.parseInt(announcerequest.getPeerId().trim())); // first we assign the ID
												temp.setAnnounceRequest_lastupdate(new Date());
												// Then, we send an AnnounceResponse (checking the swarm and saving data in memory is done within the following method):
												AnnounceResponse ann_response = prepareAnnounceResponse(connectionIdA, transacctionIdA, infoHash, downloaded, uploaded, left, temp);
												if (ismaster) {
													//Once the message is created, we send it	
													sendUDPMessage(ann_response, ip, destinationPort);
													System.out.println("		·Sending AnnounceResponse UDP message back to the peer with transactionID "+ann_response.getTransactionId()+" ... ");	
												}
												dmp.notifyPeerChanged(temp);
											}
											else{
												//Check if the required amount of time has elapsed so as to accept or not the message from the peer:
												Date last = temp.getAnnounceRequest_lastupdate();
												long diffInMillies = new Date().getTime() - last.getTime();
												long secondsPassed = TimeUnit.SECONDS.convert(diffInMillies,TimeUnit.MILLISECONDS);
												if(secondsPassed >= interval){
													// This means that one minute or more has elapsed 
													temp.setId(Integer.parseInt(announcerequest.getPeerId().trim())); // first we assign the ID
													// Then, we send an AnnounceResponse (checking the swarm and saving data in memory is done within the following method):
													AnnounceResponse ann_response = prepareAnnounceResponse(connectionIdA, transacctionIdA, infoHash, downloaded, uploaded, left, temp);
													
													if (ismaster) {
														//Once the message is created, we send it	
														sendUDPMessage(ann_response, ip, destinationPort);
														System.out.println("		·Sending AnnounceResponse UDP message back to the peer with transactionID "+ann_response.getTransactionId()+" ... ");	
													}
													dmp.notifyPeerChanged(temp);
												}
												else{
													// Less than one minute has elapsed. Such a short time to receive another request from the peer so soon. Send error!
													if (ismaster) {sendError("[IntervalIncorrectError]: AnnounceRequest too early.", transacctionIdA, ip, destinationPort);} //Send error regarding request to early	
												}
											}
										} else {
											if(ismaster){sendError("[ActionIncorrectError]: Action incorrect, should be ANNOUNCE.", transacctionIdA, ip, destinationPort);} // Send error regarding ACTION incorrect (announce)	
										}		
									}
								}
								else{
									if(ismaster){sendError("[TransactionIdIncorrectError]: Incorrect transactionID. No registered previously.", transacctionIdA, ip, destinationPort);} //Send error regarding transactionId incorrect			
								}
							}
							else{
								if (ismaster) {sendError("[ParsingError]: Error parsing AnnounceRequest message. Try to send it again.", 0, ip, destinationPort);} //Send error regarding transactionId incorrect
							}
							break;
						default: // We are supposing that the received message corresponds to a ScrapeRequest.
							ScrapeRequest scraperequest = ScrapeRequest.parse(Arrays.copyOfRange(incomingMessage.getData(), 0, incomingMessage.getLength()));
							if (scraperequest != null) {
								System.out.println("The UDP message received was a ScrapeRequest!");
								System.out.println("		·Number of infoHashes asked about: " +scraperequest.getInfoHashes().size());
								System.out.println("		·Other attributes: " + scraperequest.getConnectionId() + " | " + scraperequest.getTransactionId());
								// We get the typical data:
								int transacctionId_scrape = scraperequest.getTransactionId();
								List<String> infohashes = scraperequest.getInfoHashes();
								// Now, the idea is to get information related to the info hashes we are asked about.
								// Information required: num. of leechers, num. of seeders and num. of completed
								List<ScrapeInfo> scrapeInfoList = collectScrapeInformation(infohashes);
								ScrapeResponse scrape_response = prepareScrapeResponse(transacctionId_scrape, scrapeInfoList);
								
								if (ismaster) {
									//Once the message is created, we send it	
									sendUDPMessage(scrape_response, ip, destinationPort);
									System.out.println("		·Sending ScrapeResponse UDP message back to the peer with transactionID "+scrape_response.getTransactionId()+" ... ");	
								}
							} else {
								if (ismaster) {sendError("[SizeIncorrectError]: Size incorrect, not matching any of the messages.", 0, ip, destinationPort);} //Send error regarding size of message incorrect					
							}
							break;
					}
				}
			} catch (IOException e) {
				System.out.println("ERROR reciving an incoming message in 'MulticastSocketTracker'!");
				e.printStackTrace();
			}
		} // END while
	}
	
	/**
	 * Sends a message to the members of the MULTICAST group
	 * @param msg
	 * @param port
	 */
	public void sendMulticastMessage(String msg, int port) {
		DatagramPacket outgoingMessage = new DatagramPacket(msg.getBytes(), msg.length(), group, port);
		try {
			socketMulticast.send(outgoingMessage);
			System.out.println("UDP multicast message sent from multicast group!! :)");
		} catch (IOException e) {
			System.out.println("ERROR sending UDP multicast messsage from multicast group in 'MulticastSocketTracker'!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a message to the node/party in the other side of the UDP socket. This message will NOT go to the multicast group
	 * @param msg
	 * @param IP
	 * @param port
	 */
	public void sendUDPMessage(BitTorrentUDPMessage msg, String IP, int port) {
		InetAddress serverHost;
		try {
			serverHost = InetAddress.getByName(IP);
			byte[] byteMsg = msg.getBytes();
			DatagramPacket outgoingMessage = new DatagramPacket(byteMsg, byteMsg.length, serverHost, port);
			this.socketUDP.send(outgoingMessage);
			System.out.println("UDP message sent from UDP socket!! :)");
		} catch (IOException e) {
			System.out.println("ERROR sending UDP messsage from UDP socket in 'MulticastSocketTracker'!");
			e.printStackTrace();
		}			
	}
	
	/**
	 * Cancel the thread
	 */
	public void cancel() {
        cancel = true;
        Thread.currentThread().interrupt(); // Since 'socket.receive(...)' is a blocking call, it might be useful to just directly call ".interrupt()" instead of waiting the while loop to realize that 'cancel' is not false anymore. 
        try {
			this.socketMulticast.leaveGroup(this.group);
		} catch (IOException e) {
			System.out.println("ERROR leaving multicast group in 'MulticastSocketTracker'!");
			e.printStackTrace();
		}
    }
	
	/**
	 * Creates a ConnectResponse message given the values in the parameters
	 * @param transactionId
	 * @return
	 */
	public ConnectResponse prepareConnectResponse(int transactionId){
		ConnectResponse response = new ConnectResponse();
		response.setAction(Action.CONNECT);
		response.setTransactionId(transactionId); // Transaction ID!
		response.setConnectionId(ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE)); // Connection ID!
		return response;
	}
	
	/**
	 * Creates an AnnounceResponse message given the values in the parameters
	 * @param connectionId
	 * @param transactionId
	 * @param stringinfohash
	 * @param downloaded
	 * @param uploaded
	 * @param left
	 * @param peer
	 * @return
	 */
	public AnnounceResponse prepareAnnounceResponse(long connectionId, int transactionId, String stringinfohash, long downloaded, long uploaded, long left, Peer peer){
		AnnounceResponse response = new AnnounceResponse();
		response.setAction(Action.ANNOUNCE);
		response.setTransactionId(transactionId);
		//response.setInterval(interval);
		
		// Now, it is compulsory to check whether the peer we are communicating with has already been interested in the swarm.
		// In other words, check if the swarm already exists or not.
		if (dms.getSwarmList().containsKey(stringinfohash)) {
			// IF YES, then we already knew about that swarm.
			// First, we prepare the response to the peer and we will handle the update process (concerning all trackers) afterwards:
			Swarm temp = dms.getSwarmList().get(stringinfohash);
			
			//Set new value of peers/seeders
			response.setLeechers(temp.getTotalLeecher());
			response.setSeeders(temp.getTotalSeeders());
			response.setPeers(temp.getPeerInfoList(left, stringinfohash, 50));
			//We don't know nothing about the swarm, so we establish a default interval
			//We determinate an interval
			int period = temp.getAppropiateInterval(dms.getSwarmList());
			System.out.println("		·Interval selected: " + period);
			response.setInterval(period);
			interval = period;
			// No matter whether the tracker is the MASTER or SLAVE, it is necessary to save in memory the information regarding the SWARM.
			// The corresponding update to the database will occur later on.
			// Now we extract/capture the peer from the memory and update is properties
			
			//Check if the peer exists in the swarm
			HashMap<String, Swarm> temp_SwarmMap = dms.getSwarmList();
			Peer tempPeer = null;
			if(temp.getPeerListHashMap().containsKey(peer.getId())){
				//The peer is already in the peer, just is necessary to update it
				tempPeer = temp.getPeerListHashMap().get(peer.getId());
				tempPeer.updatePeerTorrentInfo(stringinfohash, downloaded, uploaded, left);
			}
			else{
				//The peer isn't at the swarm
				tempPeer = dmp.peerlist.get(transactionId);
				if(left > 0){
					//leecher
					temp.setTotalLeecher(temp.getTotalSeeders()+1);
				}
				if(downloaded > 0){
					//seeder
					temp.setTotalSeeders(temp.getTotalLeecher()+1);
				}
				
				//Then we insert it
				tempPeer.getSwarmList().put(stringinfohash, new PeerTorrent(stringinfohash, uploaded, downloaded, left));
				
			}
			

			// Now we put the peer back to its place in the memory:
			temp.addPeerToList(tempPeer.getId(), tempPeer);
			temp_SwarmMap.put(temp.getInfoHash(), temp);
			dms.setSwarmList(temp_SwarmMap);
			dms.notifySwarmChanged(temp);
			// So now, it is necessary to tell the rest of the trackers (IF WE ARE THE MASTER) to update the information repository:
			if (ismaster) {dmt.sendRepositoryUpdateRequestMessage(peer.getIp(),peer.getPort(),peer.getId(),stringinfohash);}
		} else {
			// IF NO, then we did NOT know about that swarm before.
			response.setLeechers(0);
			response.setSeeders(0);
			ArrayList<PeerInfo> temp = new ArrayList<PeerInfo>();
			PeerInfo pf = new PeerInfo();
			pf.setIpAddress(convertIpAddressToInt(peer.getIp()));
			pf.setPort(peer.getPort());
			temp.add(pf);
			response.setPeers(temp);
			response.setInterval(interval);
			// Now, no matter whether the tracker is the MASTER or SLAVE, it is necessary to save in memory the information regarding the SWARM.
			// The corresponding update to the database will occur later on.
			HashMap<String, Swarm> temp_SwarmMap= dms.getSwarmList();
			Swarm s = new Swarm(stringinfohash);
			if(left > 0){
				//leecher
				s.setTotalLeecher(1);
			}
			if(downloaded > 0){
				//seeder
				s.setTotalSeeders(1);
			}
			s.setSize(downloaded + left);
			peer.getSwarmList().put(stringinfohash, new PeerTorrent(stringinfohash, uploaded, downloaded, left));
			s.addPeerToList(peer.getId(), peer);
			temp_SwarmMap.put(stringinfohash, s);
			dms.setSwarmList(temp_SwarmMap);
			dms.notifySwarmChanged(s);
			// So, this means that it is the first time a peer requests the mentioned swarm.
			// We need to include/save the new swarm and inform the rest of the trackers:
			if (ismaster) {dmt.sendRepositoryUpdateRequestMessage(peer.getIp(),peer.getPort(),peer.getId(),stringinfohash);}
		}
		return response;
	}
	
	/**
	 * Creates a ScrapeResponse message given the values in the parameters
	 * @param transactionid
	 * @param scrapeInfoList
	 * @return
	 */
	public ScrapeResponse prepareScrapeResponse(int transactionid, List<ScrapeInfo> scrapeInfoList) {
		ScrapeResponse response = new ScrapeResponse();
		response.setAction(Action.SCRAPE);
		response.setTransactionId(transactionid);
		return response;
	}
	
	private List<ScrapeInfo> collectScrapeInformation(List<String> infohashes) {
		List<ScrapeInfo> resul = new ArrayList<ScrapeInfo>();
		for (String infohash : infohashes) {
			ScrapeInfo sf = new ScrapeInfo();
			sf.setLeechers(dms.getSwarmList().get(infohash).getTotalLeecher());
			sf.setSeeders(dms.getSwarmList().get(infohash).getTotalSeeders());
			sf.setCompleted(dms.getSwarmList().get(infohash).countCompleted());
			resul.add(sf);
		}
		return resul;
	}
	
	/**
	 * Creates an Error message
	 * @param errormessage
	 * @param transactionId
	 * @return
	 */
	public Error prepareError(String errormessage, int transactionId){
		Error response = new Error();
		response.setAction(Action.ERROR);
		response.setTransactionId(transactionId);
		response.setMessage(errormessage);
		return response;
	}
	
	/**
	 * Send an error message through a UDP socket (first, it creates an ErrorMessage)
	 * @param msg
	 * @param transactionID
	 * @param ip
	 * @param destinationport
	 */
	private void sendError(String msg, int transactionID, String ip, int destinationport) {
		Error error = prepareError(msg, transactionID);
		sendUDPMessage(error, ip, destinationport);
	}
	
	/**
	 * Launches a thread that checks every 10 seconds if the connection ID of every peer of all existing swarms have expired or not.
	 */
	private void startConnectionIdChecker() {
		connectionCheckerThread = new Thread(new ConnectionIdChecker(dmp)); 
		connectionCheckerThread.start();
	}
	
	public static int convertIpAddressToInt(String ip){
		
		int result = 0;
		InetAddress temp = null;
		try {
			temp = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if(temp != null){
			for (byte b: temp.getAddress())  
			{  
			    result = result << 8 | (b & 0xFF);  
			}
		}
		return result;
		
	}
	
	byte[] convertIntToBytes(int bytes) {
		  return new byte[] {
		    (byte)((bytes >>> 24) & 0xff),
		    (byte)((bytes >>> 16) & 0xff),
		    (byte)((bytes >>>  8) & 0xff),
		    (byte)((bytes       ) & 0xff)
		  };
		}
}

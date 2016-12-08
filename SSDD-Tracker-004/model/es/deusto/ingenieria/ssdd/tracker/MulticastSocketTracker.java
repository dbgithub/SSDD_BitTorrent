package es.deusto.ingenieria.ssdd.tracker;

import java.awt.List;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import bitTorrent.tracker.protocol.udp.AnnounceRequest;
import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage.Action;
import controller.ConnectionIdRenewer;
import bitTorrent.tracker.protocol.udp.BitTorrentUDPRequestMessage;
import bitTorrent.tracker.protocol.udp.ConnectRequest;
import bitTorrent.tracker.protocol.udp.ConnectResponse;
import bitTorrent.tracker.protocol.udp.Error;
import bitTorrent.tracker.protocol.udp.PeerInfo;
import bitTorrent.tracker.protocol.udp.AnnounceRequest.Event;
import bitTorrent.tracker.protocol.udp.AnnounceResponse;
import bitTorrent.tracker.protocol.udp.BitTorrentUDPMessage;
import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.classes.Swarm;
import es.deusto.ingenieria.ssdd.data.DataModelSwarm;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;

/**
 * This runnable class represents a thread that will be executed, taking care of the folllowing:
 * · Creating a multicast group
 * · Join the corresponding tracker (the one who is launching this thread) to the multicast group.
 * · Handle incoming messages from the multicast group (send & receive)
 * Apart from that, this class will make sure that in case the tracker is the master, then, a new UDP socket will be opened
 * to communicate with the peer.
 * In summary, this class manages the multicast group socket + UDP vanilla socket.
 * @author aitor & kevin
 *
 */
public class MulticastSocketTracker implements Runnable {

	private MulticastSocket socketMulticast; // Socket for the multicast group. This will send messages to the members from within the multicast group, plus receive messages from peers.
	private DatagramSocket socketUDP; // Socket for the UDP socket, NOT multicast. This will send message to the peers (the node/party) in the otehr side of the communication socket.
	private InetAddress group;
	private byte[] buffer;
	private DatagramPacket incomingMessage;
	volatile boolean cancel = false;
	private boolean ismaster;
	public HashMap<Long, Peer> peerList = new HashMap<>(); //Saves the peers that 
	private DataModelTracker trackersInfo;
	private DataModelSwarm swarmsInfo;
	private int interval = 60; // number of seconds to wait before receiving another AnnounceRequest from a peer.
	
	//THREAD
	private ConnectionIdChecker connectionChecker;
	private Thread connectionCheckerThread;
	
	
	public MulticastSocketTracker(int port, String IP, boolean ismaster, DataModelTracker dmt, DataModelSwarm dms) {
		this.trackersInfo = dmt;
		this.swarmsInfo = dms;
		try {
			this.socketMulticast = new MulticastSocket(port);
			this.group = InetAddress.getByName(IP);
			this.socketMulticast.joinGroup(group);
		} catch (IOException e) {
			System.out.println("ERROR creating and/or joining a multicast group in 'MulticastSocketTracker'!");
			e.printStackTrace();
		};
		this.buffer = new byte[1024];	
		this.incomingMessage = null;
		this.ismaster = ismaster;
		if (ismaster) {
			// Only when the tracker who is launching this thread is the master, this code will be executed.
			// Since the tracker master needs a direct communication between him and the peer, it is necessary to create a datagram socket
			// in order to send UPD messages to the peer.
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
			System.out.println("MulticastSocket: waiting for incoming messages...");
			this.incomingMessage = new DatagramPacket(buffer, buffer.length);
			try {
				this.socketMulticast.receive(incomingMessage);
				System.out.println("UDP message arrived at Multicast group! :)");
				System.out.println("Sender's IP: " + incomingMessage.getAddress().getHostAddress()); // This might be either an IP from the multicast group or peer's IP.
				System.out.println("Sender's Port: " + incomingMessage.getPort()); // This might be either an port from the multicast group or peer's port.
				System.out.println("Sender's message length: " + incomingMessage.getLength());
				System.out.println("Sender's data: " + new String(incomingMessage.getData()));
				
				
				if (!incomingMessage.getAddress().equals(group)) {
					// In this case, the incoming message comes from  an IP different from any tracker's IP, then, it is NOT a message from within the multicast group.
					// Since we are interested in the incoming peers' IPs, we will save any interesting data:
					
					// TODO Enviar un mensaje JMS, UpdateRequest, a todos los SLAVES que estan escuchando para valorar si el master envia o no actualizacion de datos.
					String ip = incomingMessage.getAddress().getHostAddress();
					int originPort = incomingMessage.getPort();
					int destinationPort = originPort;
					switch(incomingMessage.getLength()){
						case 16:
							//If length is 16 bytes, then it's a ConnectRequest
							System.out.println("The UDP message received was a ConnectionRequest");
							ConnectRequest request = ConnectRequest.parse(incomingMessage.getData());
							long connectionId = request.getConnectionId();
							int transacctionId = request.getTransactionId();
							Action action = request.getAction();
							if(action.compareTo(Action.CONNECT) == 0 && connectionId == 41727101980L){
								//Then, this means the first connection between the peer and the tracker
								//So, we have to add the peer to a list and response back to the peer if we are the master
								//First, create a peer
								Peer p = new Peer();
								p.setIp(ip);
								p.setAnnounceRequest_lastupdate(null);
								p.setPort(destinationPort);
								p.setTransaction_id(transacctionId);
								p.setConnection_id_lastupdate(new Date());
								peerList.put(p.getTransaction_id(), p);
								
								// If the current tracker is the master. Then, it has to response to the peer with a connection_id
								if(ismaster){
									//Create Response
									ConnectResponse response = prepareConnectResponse(transacctionId);
									p.setConnection_id(response.getConnectionId());
									
									//Once the message is created, we send it	
									sendUDPMessage(response, ip, destinationPort);
									System.out.println("Sending ConnectResponse UPD message back to the peer...");
								}	
							}
							else{
								//Then is trying to renew its connectionId
								Peer selected = peerList.get(transacctionId);
								//We have to response to the peer with a new connection_id
								ConnectResponse response = prepareConnectResponse(transacctionId);
								selected.setConnection_id(response.getConnectionId());
								selected.setConnection_id_lastupdate(new Date());
								
								//Once is created the message, we send it	
								sendUDPMessage(response, ip, destinationPort);
							}
							break;
						case 98:
							//If length is 98 bytes, then it's an AnnounceRequest
							AnnounceRequest announcerequest = AnnounceRequest.parse(incomingMessage.getData());
							System.out.println("The UDP message received was a AnnounceRequests (InfoHash: "+announcerequest.getHexInfoHash()+")");
							String infoHash = announcerequest.getHexInfoHash();
							int transacctionIdA = announcerequest.getTransactionId();
							long connectionIdA = announcerequest.getConnectionId();
							long downloaded = announcerequest.getDownloaded();
							long uploaded = announcerequest.getUploaded();
							long left = announcerequest.getLeft();
							Peer temp = peerList.get(transacctionIdA);
							if(temp != null){
								// This means that the communication is going on the right path.
								// The tracker knew about the transaction ID, so we continue with the process.
								// Check if the information is coherent:
								if(temp.getConnection_id() == transacctionIdA && announcerequest.getAction().compareTo(Action.ANNOUNCE) == 0){
									if(temp.getAnnounceRequest_lastupdate() == null){
										// This means that it is the first time in receiving an AnnounceRequest.
										// Then, we send an AnnounceResponse (checking the swarm and saving data in memory is done within the following method):
										temp.setId(Integer.parseInt(announcerequest.getPeerId())); // first we assign the ID
										AnnounceResponse ann_response = prepareAnnounceResponse(connectionIdA, transacctionIdA, infoHash, downloaded, uploaded, left, temp);

										if (ismaster) {
											//Once the message is created, we send it	
											sendUDPMessage(ann_response, ip, destinationPort);
											System.out.println("Sending AnnounceResponse UPD message back to the peer...");	
										}
									}
									else{
										//Check if the required amount of time has elapsed so as to accept or not the message from the peer:
									    Date last = temp.getAnnounceRequest_lastupdate();
										long diffInMillies = new Date().getTime() - last.getTime();
									    long secondsPassed = TimeUnit.SECONDS.convert(diffInMillies,TimeUnit.MILLISECONDS);
									    if(secondsPassed >= 60){
									    	// This means that one minute or more has elapsed 
									    	// Then, we send an AnnounceResponse (checking the swarm and saving data in memory is done within the following method):
									    	temp.setId(Integer.parseInt(announcerequest.getPeerId())); // first we assign the ID
											AnnounceResponse ann_response = prepareAnnounceResponse(connectionIdA, transacctionIdA, infoHash, downloaded, uploaded, left, temp);
											
											if (ismaster) {
												//Once the message is created, we send it	
												sendUDPMessage(ann_response, ip, destinationPort);
												System.out.println("Sending AnnounceResponse UPD message back to the peer...");	
											}
									    }
									    else{
									    	// Less than one minute has elapsed. Such a short time to receive another request from the peer so soon. Send error!
									    	Error error = prepareError("Error: need to wait the time specified", transacctionIdA);
									    	sendUDPMessage(error, ip, destinationPort);
									    }
									}
								}
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
	
	// Sends a message to the members of the MULTICAST group
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
	
	// Sends a message to the node/party in the other side of the UDP socket.
	// This message will NOT go to the multicast group
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
	
	public ConnectResponse prepareConnectResponse(int transactionId){
		ConnectResponse response = new ConnectResponse();
		response.setAction(Action.CONNECT);
		response.setTransactionId(transactionId);
		response.setConnectionId(ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE));
		return response;
	}
	
	public AnnounceResponse prepareAnnounceResponse(long connectionId, int transactionId, String stringinfohash, long downloaded, long uploaded, long left, Peer peer){
		AnnounceResponse response = new AnnounceResponse();
		response.setAction(Action.ANNOUNCE);
		response.setTransactionId(transactionId);
		response.setInterval(interval);
		
		// Now, it is compulsory to check whether the peer we are communicating with has already been interested in the swarm.
		// In other words, check if the swarm already exists or not.
		if (swarmsInfo.getSwarmList().containsKey(stringinfohash)) {
			// IF YES, then we already knew about that swarm.
			// First, we send back the response to the peer and we will handle the update process (concerning all trackers) afterwards:
			Swarm temp = swarmsInfo.getSwarmList().get(stringinfohash);
			response.setLeechers(temp.getTotalLeecher());
			response.setSeeders(temp.getTotalSeeders());
			response.setPeers(temp.getPeerInfoList());
			// No matter whether the tracker is the MASTER or SLAVE, it is necessary to save in memory information regarding the SWARM.
			// The corresponding update to the database will occur later on.
			temp.addPeerToList(peer.getId(), peer);
			HashMap<String, Swarm> temp_map= swarmsInfo.getSwarmList();
			temp_map.put(stringinfohash, temp);
			swarmsInfo.setSwarmList(temp_map);
			// So now, it is necessary to tell the rest of the trackers to update the information repository:
			trackersInfo.sendRepositoryUpdateRequestMessage(peer.getIp(),peer.getPort(),peer.getId(),stringinfohash);
		} else {
			// IF NO, then we did NOT know about that swarm before.
			response.setLeechers(0);
			response.setSeeders(0);
			ArrayList<PeerInfo> temp = new ArrayList<PeerInfo>();
			PeerInfo pf = new PeerInfo();
			pf.setIpAddress(Integer.parseInt(peer.getIp()));
			pf.setPort(peer.getPort());
			temp.add(pf);
			response.setPeers(temp);
			// No matter whether the tracker is the MASTER or SLAVE, it is necessary to save in memory information regarding the SWARM.
			// The corresponding update to the database will occur later on.
			HashMap<String, Swarm> temp_map= swarmsInfo.getSwarmList();
			Swarm s = new Swarm(stringinfohash);
			s.addPeerToList(peer.getId(), peer);
			temp_map.put(stringinfohash, s);
			swarmsInfo.setSwarmList(temp_map);
			// So, this means that it is the first time a peer requests the mentioned swarm.
			// We need to include/save the new swarm and inform the rest of the trackers:
			trackersInfo.sendRepositoryUpdateRequestMessage(peer.getIp(),peer.getPort(),peer.getId(),stringinfohash);
		}
		return response;
	}
	
	public Error prepareError(String errormessage, int transactionId){
		Error response = new Error();
		response.setAction(Action.ERROR);
		response.setTransactionId(transactionId);
		response.setMessage(errormessage);
		return response;
	}
	
	private void startConnectionIdChecker() {
		ConnectionIdChecker checker = new ConnectionIdChecker(this);
		connectionChecker = checker;
		connectionCheckerThread = new Thread(checker); 
		connectionCheckerThread.start();
	}

}

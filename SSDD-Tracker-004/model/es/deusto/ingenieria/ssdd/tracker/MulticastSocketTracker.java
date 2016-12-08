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
 * This runnable class represents a thread that will be executed which its main purpose will be handling:
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
	
	private int interval = 60; //number of seconds to wait
	
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
					// In this case, the incoming message comes from  an IP different from any tracker's IP, then, it is NOT a message from withing the multicast group.
					// Since we are interested in the incoming peers' IPs, we will save any interesting data:
					
					// TODO Acceder al SWARM y comprobar que el infoHash que nos manda el peer esta o no metido en el hashmap del SWARM.
					// 		Si no lo está entonces, metemos el infoHash nuevo y guardamos la direccion IP y puerto del peer en cuestion.
					// TODO Enviar un mensaje JMS, UpdateRequest, a todos los SLAVES que estan escuchando para valorar si el master envia o no actualizacion de datos.
					// TODO Independientemente de la respuesta de los trackers SLAVES en cuanto al UpdateRequest, el master ha de devolver la lista de PEERS en relacion
					//		al SWARM en cuestion, de vuelta al peer solicitante. Para eso, enviar los bytes mediante el UDP socket, no el multicast.
					String ip = incomingMessage.getAddress().getHostAddress();
					int originPort = incomingMessage.getPort();
					int destinationPort = originPort;
					switch(incomingMessage.getLength()){
						case 16:
							//If length is 16 bytes, then it's a ConnectRequest
							
							ConnectRequest request = ConnectRequest.parse(incomingMessage.getData());
							long connectionId = request.getConnectionId();
							int transacctionId = request.getTransactionId();
							Action action = request.getAction();
							if(action.compareTo(Action.CONNECT) == 0 && connectionId == 41727101980L){
								//Then is the first connection to the tracker
								//So we have to add to a list and response if we are the master
								//First create a peer
								Peer p = new Peer();
								p.setIp(ip);
								p.setAnnounceRequest_lastupdate(null);
								p.setPort(destinationPort);
								p.setTransaction_id(transacctionId);
								p.setConnection_id_lastupdate(new Date());
								peerList.put(p.getTransaction_id(), p);
								
								//We have to response to the peer with a connection_id
								if(ismaster){
									//Create Response
									ConnectResponse response = prepareConnectResponse(connectionId, transacctionId);
									p.setConnection_id(response.getConnectionId());
									
									//Once is created the message, we send it	
									sendUDPMessage(response, ip, destinationPort);
								}
								
							}
							else{
								//Then is trying to renew its connectionId
								Peer selected = peerList.get(transacctionId);
								//We have to response to the peer with a new connection_id
								ConnectResponse response = prepareConnectResponse(connectionId, transacctionId);
								selected.setConnection_id(response.getConnectionId());
								selected.setConnection_id_lastupdate(new Date());
								
								//Once is created the message, we send it	
								sendUDPMessage(response, ip, destinationPort);
							}
							break;
						case 98:
							//If length is 98 bytes, then it's an AnnounceResponse
							AnnounceRequest announcerequest = AnnounceRequest.parse(incomingMessage.getData());
							System.out.println("Announce Request: "+ announcerequest.getHexInfoHash());
							Peer temp = peerList.get(announcerequest.getTransactionId());
							int transacctionIdA = announcerequest.getTransactionId();
							long connectionIdA = announcerequest.getConnectionId();
							if(temp != null){
								//Check if all is correct
								if(temp.getConnection_id() == announcerequest.getConnectionId() && announcerequest.getAction().compareTo(Action.ANNOUNCE) == 0){
									if(temp.getAnnounceRequest_lastupdate() == null){
										//This means that it is the first time in sending the request
										//Then we send an AnnounceResponse
										//TODO: add the peer to the swarm, manage the information to de DATABASE and response
										// to the client
									}
									else{
										//Compare if have passed the time required 
									    Date last = temp.getAnnounceRequest_lastupdate();
										long diffInMillies = new Date().getTime() - last.getTime();
									    long secondsPassed = TimeUnit.SECONDS.convert(diffInMillies,TimeUnit.MILLISECONDS);
									    if(secondsPassed >= 60){
									    	//This means that has passed one minute or more
									    	//Then we send an AnnounceResponse
									    	Peer p = peerList.get(transacctionIdA);
									    	p.setId(Integer.parseInt(announcerequest.getPeerId()));
									    	if(p != null){
									    		//TODO: add the peer to the swarm, manage the information to de DATABASE and response
												// to the client
									    		//AnnounceResponse aresponse = prepareAnnounceResponse(connectionIdA, transacctionIdA, announcerequest.getInfoHash(), announcerequest.getHexInfoHash(), p);
									    	}
									    }
									    else{
									    	//if not, send error
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
	
	public ConnectResponse prepareConnectResponse(long connectionId, int transactionId){
		ConnectResponse response = new ConnectResponse();
		response.setAction(Action.CONNECT);
		response.setTransactionId(transactionId);
		connectionId = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
		response.setConnectionId(connectionId);
		return response;
	}
	
	public AnnounceResponse prepareAnnounceResponse(long connectionId, int transactionId, byte[] infohash, String stringinfohash, Peer peer){
		AnnounceResponse response = new AnnounceResponse();
		response.setAction(Action.ANNOUNCE);
		response.setTransactionId(transactionId);
		response.setInterval(interval);
		if(swarmsInfo.getSwarmList().containsKey(infohash)){
			//The swarm exists, so there is at least another peer
			response.setPeers(new ArrayList(swarmsInfo.getSwarmList().get(infohash).getPeerList().values()));
		}
		else{
			//The first peer asking for that swarm
			Swarm newone = new Swarm(stringinfohash);
			newone.addPeerToList(peer.getId(), peer);
			
			response.setLeechers(0);
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

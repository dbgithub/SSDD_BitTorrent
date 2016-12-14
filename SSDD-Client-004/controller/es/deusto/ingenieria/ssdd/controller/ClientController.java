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

public class ClientController {
	
	public static boolean ConnectResponseReceived = false;
	private ConnectResponse connectResponse; //Response for the first ConnectRequest
	private AnnounceResponse announceResponse; //Response for the first AnnounceResponse
	private ScrapeResponse scrapeResponse; // Response of the Scrape request
	
	private DatagramSocket multicastsocketSend; //Represents the socket for sending messages
	private DatagramSocket socketReceive; //Represents the socket for receiving messages
	private ServerSocket peerListenerSocket; //Socket for listening other peer connections
	
	private int idPeer = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
	private InetAddress multicastIP;
	private static long connectionId = 41727101980L;
	private static int transactionID;
	private static final int DESTINATION_PORT = 9000;
	private static int peerListenerPort;
	
	// List of swarms
	public HashMap<String, Swarm> torrents = new HashMap<>();
	// Information about each and every swarm (leechers, seeders and completed), ScrapeInfos:
	public ArrayList<ScrapeInfo> listScrapeInfo = new ArrayList<ScrapeInfo>();
	
	//THREADS
	private ConnectionIdRenewer connectionRenewer;
	private Thread connectionRenewerThread;
	private DownloadStateNotifier downloadNotifier;
	private Thread downloadNotifierThread;
	
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
			if(multicastsocketSend == null){
				try {
					multicastsocketSend = new DatagramSocket();
					socketReceive = multicastsocketSend;
					peerListenerSocket = new ServerSocket();
					peerListenerPort = peerListenerSocket.getLocalPort();
				}catch (SocketException e) {
					// TODO Auto-generated catch block
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
			createDownloadStateNotifier(single, multicastsocketSend, socketReceive, announceResponse.getInterval());
		}
	}

	private void sendConnectRequest(MetainfoHandlerSingleFile single, DatagramSocket socket, boolean firstTime){
		try{
			multicastIP = InetAddress.getByName(single.getMetainfo().getAnnounce());
						
			//Create Message
			ConnectRequest request = createConnectRequest(firstTime);
			
			byte[] requestBytes = request.getBytes();	
			DatagramPacket messageOut = new DatagramPacket(requestBytes, requestBytes.length, multicastIP, DESTINATION_PORT);
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
	
	private void sendAnnounceRequest(MetainfoHandlerSingleFile single, DatagramSocket socket, long downloaded, long left, long uploaded){
		try{
			InfoDictionarySingleFile info = single.getMetainfo().getInfo();
			System.out.println("InfoHash: "+ info.getHexInfoHash());
			AnnounceRequest request = createAnnounceRequest(info.getInfoHash(), 0, info.getLength(), 0, Event.NONE, 0, peerListenerPort);
			
			byte[] requestBytes = request.getBytes();	
			DatagramPacket messageOut = new DatagramPacket(requestBytes, requestBytes.length, multicastIP, DESTINATION_PORT);
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
	
	private void sendScrapeRequest(DatagramSocket socketSend) {
		ScrapeRequest request = createScrapeRequest();
		byte[] requestBytes = request.getBytes();
		DatagramPacket messageOut = new DatagramPacket(requestBytes, requestBytes.length,multicastIP, DESTINATION_PORT);
		System.out.println(" - UDP ScrapeRequest message sent to the multicast group. From " + messageOut.getAddress().getHostAddress() +":"+
							messageOut.getPort() + " to " + multicastIP.getHostAddress()+":"+DESTINATION_PORT +" (Bytes="+messageOut.getLength());
		try {
			socketSend.send(messageOut);
		} catch (IOException e) {
			System.err.println("# Socket Error ('sendScrapeRequest'): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendAndWaitUntilConnectResponseReceivedLoop(MetainfoHandlerSingleFile single, DatagramSocket socketSend, DatagramSocket socketListen, boolean firstime){
		try{
			//Let's set a timeout if the tracker doesn't response
			socketListen.setSoTimeout(3000);
			byte[] buffer = new byte[1024];
			boolean responseReceived = false;
			while(!responseReceived){     // recieve data until timeout
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
					if (response.getLength() >= 48) { 
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
				} catch (IOException e) {
					System.out.println("ERROR occurred receiving from the listening socket in 'ScrapeResponseReceivedLoop'");
					e.printStackTrace();
				} 
			}
		} catch (SocketException e1) {
			System.out.println("ERROR: Timeout exception in 'ScrapeResponseReceivedLoop'");
			e1.printStackTrace();
		}
	}
	
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
	
	private ConnectRequest createConnectRequest(boolean firstTime){
		ConnectRequest request = new ConnectRequest();
		request.setConnectionId(connectionId);
		request.setAction(Action.CONNECT);
		//If it's the first time, first connection, we should generate transaction ID.
		//Unless it is, we use the generated one
		if(firstTime){
			transactionID = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
		}
		request.setTransactionId(transactionID);
		return request;
		
	}
	
	private ScrapeRequest createScrapeRequest() {
		ScrapeRequest scrape = new ScrapeRequest();
		scrape.setConnectionId(connectionId);
		scrape.setTransactionId(transactionID);
		scrape.setAction(Action.SCRAPE);
		int j = 0;
		for (String s : this.torrents.keySet()) {
			if (j < 74) {
				scrape.addInfoHash(s);
				j++;				
			} else {
				break; // We cannot request information about more than 74 swarms, that's the limit! :)
			}
		}
		return scrape;
	}
	
	private void createConnectionIdRenewer(DatagramSocket send, DatagramSocket receive, MetainfoHandlerSingleFile single) {
		ConnectionIdRenewer ms = new ConnectionIdRenewer(send, receive, single, this);
		connectionRenewer = ms;
		connectionRenewerThread = new Thread(ms); 
		connectionRenewerThread.start();
	}
	
	private void createDownloadStateNotifier(MetainfoHandlerSingleFile single, DatagramSocket send,
			DatagramSocket receive, int interval) {
		DownloadStateNotifier dsn = new DownloadStateNotifier(send, receive, single, interval, this);
		downloadNotifier = dsn;
		downloadNotifierThread = new Thread(dsn); 
		downloadNotifierThread.start();
	}
	
	private InetAddress convertIntToIP(int ip) throws UnknownHostException{
		byte[] bytes = BigInteger.valueOf(ip).toByteArray();
		InetAddress address = InetAddress.getByAddress(bytes);
		return address;
	}

}

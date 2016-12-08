package es.deusto.ingenieria.ssdd.tracker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import bitTorrent.tracker.protocol.udp.AnnounceRequest;

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
	
	public MulticastSocketTracker(int port, String IP, boolean ismaster) {
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
				// TODO: En base al tamaño del mensaje que recibes (los bytes), sabremos si es Announce o Connect request
				AnnounceRequest ar = new AnnounceRequest();
				ar.parse(incomingMessage.getData());
				System.out.println("---Announce Request---");
				System.out.println("Peer ID: "+ar.getConnectionId());
				if (ismaster && !incomingMessage.getAddress().equals(group)) {
					// In this case, the incoming message comes from  an IP different from any tracker's IP, then, it is NOT a message from withing the multicast group.
					// Since we are interested in the incoming peers' IPs, we will save any interesting data:
					
					// TODO Acceder al SWARM y comprobar que el infoHash que nos manda el peer esta o no metido en el hashmap del SWARM.
					// 		Si no lo está entonces, metemos el infoHash nuevo y guardamos la direccion IP y puerto del peer en cuestion.
					// TODO Enviar un mensaje JMS, UpdateRequest, a todos los SLAVES que estan escuchando para valorar si el master envia o no actualizacion de datos.
					// TODO Independientemente de la respuesta de los trackers SLAVES en cuanto al UpdateRequest, el master ha de devolver la lista de PEERS en relacion
					//		al SWARM en cuestion, de vuelta al peer solicitante. Para eso, enviar los bytes mediante el UDP socket, no el multicast.
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
	public void sendUDPMessage(String msg, String IP, int port) {
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

}

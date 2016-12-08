package es.deusto.ingenieria.ssdd.redundancy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.w3c.dom.Document;

import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.classes.PeerTrackerTemplate;
import es.deusto.ingenieria.ssdd.classes.Swarm;
import es.deusto.ingenieria.ssdd.data.DBManager;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelSwarm;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;

/**
 * A JMS listener that waits for messages related to new incoming peers so that the slave updates its database 
 * @author aitor & kevin
 *
 */
public class RepositorySyncListener implements MessageListener{
	
	private DataModelTracker dmt;
	private DataModelConfiguration dmc;
	private DataModelSwarm dms;
	private MessageProducer producer;
	private Session session;
	private HashMap<Integer, HashMap<String, Boolean>> slaveResponseAvailabilityHashMap; // This HashMap stores True or False depending on the availability of the tracker slave
	private HashMap<Integer, PeerTrackerTemplate> updatePeerList;
	private RepositorySyncTimeout repoSyncTimeout; // Runnable class that ensures that the communication between the master and slaves is not broken. After the timeout is fired, the same message is sent to the master!
	private Thread timeout; // Tracker slave will launch this Thread to ensure that the communication between the master and slaves is not broken.
	private DBManager database;
	
	public RepositorySyncListener(DataModelTracker dmt, DataModelConfiguration dmc, DataModelSwarm dms, MessageProducer p, Session s, DBManager db) {
		this.dmt = dmt;
		this.dmc = dmc;
		this.dms = dms;
		this.producer = p;
		this.session = s;
		this.slaveResponseAvailabilityHashMap = new HashMap<Integer, HashMap<String, Boolean>>();
		this.updatePeerList = new HashMap<Integer, PeerTrackerTemplate>();
		this.database = db;
	}
	
	@Override
	public void onMessage(Message msg) {
		if (msg != null) {
			if (msg instanceof TextMessage) {
				try {
					TextMessage txtmsg = (TextMessage) msg;
					JMSXMLMessages parser = new JMSXMLMessages();
					Document xml = parser.convertFromStringToXML(txtmsg.getText());
					String type = xml.getElementsByTagName("type").item(0).getTextContent();
					switch(type) {
					case "UpdateRequest":
						System.out.println("UpdateRequest from tracker master received! Now, sending SlaveResponse...");
						// JMS message: SlaveResponse
						int updateId = Integer.parseInt(xml.getElementsByTagName("updateid").item(0).getTextContent());
						System.out.println("Is avaialbe to recieve updates??????????????????? " + dmc.isAvailableToReceiveUpdates());
						String slaveresponse = new JMSXMLMessages().convertToStringSlaveResponse(updateId, dmc.getId(), dmc.isAvailableToReceiveUpdates() ? "OK" : "ERROR");
						TextMessage txtmsg_update = session.createTextMessage();
						txtmsg_update.setText(slaveresponse);
						txtmsg_update.setStringProperty("Filter", "IncomingFromSlave");
						producer.send(txtmsg_update);
						repoSyncTimeout = new RepositorySyncTimeout(producer,txtmsg_update);
						timeout = new Thread(repoSyncTimeout);
						timeout.start();
						break;
					case "SlaveResponse":
						// The master captures the availability information coming from tracker slave:
						int updateID = Integer.parseInt(xml.getElementsByTagName("updateid").item(0).getTextContent());
						String slaveID = xml.getElementsByTagName("slaveID").item(0).getTextContent();
						String slaveAvailability = xml.getElementsByTagName("status").item(0).getTextContent(); // Either "OK" or "ERROR"
						// The master stores the availability value for every Slave:
						System.out.println("SlaveResponse received from tracker with ID='"+slaveID+"', now cheking whether the majority of slaves are ready to receive updates...");
						slaveResponseAvailabilityHashMap.get(updateID).put(slaveID, (slaveAvailability.equals("OK"))? true : false);
						// Now, in case that the amount of responses that the master receives is the same as the number of slaves, then
						// the master will continue executing its code. Otherwise, it should wait until it receives every answer:
						if (slaveResponseAvailabilityHashMap.get(updateID).size() == (dmt.getTrackerlist().size()-1)) {
							// Now the master has to evaluate whether it is worth or not to send an the pending update message.
							// To do this, a 80% of the answers should be a positive response:
							int admissionRate = (int) Math.round(slaveResponseAvailabilityHashMap.get(updateID).size() * 0.8);
							int amountPositiveAnswers = 0;
							Iterator<Boolean> itera = slaveResponseAvailabilityHashMap.get(updateID).values().iterator();
							while (itera.hasNext()) {
								boolean hola = itera.next();
								System.out.println("itera.next() = " + hola);
								if (hola) {amountPositiveAnswers++;};
							}
							// Now, it's time to compare the amount of real positive answers and the admission rate set to 80%
							// Depending on that, the master will send either an UPDATE or ABORT message.
							String updateMsg;
							System.out.println("HOLLIIIIIII!! amountPositiveAnswers = " + amountPositiveAnswers + " | admissionRate= "+ admissionRate);
							if (amountPositiveAnswers >= admissionRate) {
								System.out.println("The majority of the tracker slaves are ready to receive updates!");
								// JMS message: Update UDPATE
								PeerTrackerTemplate updateInformation = updatePeerList.get(updateID);
								updateMsg = new JMSXMLMessages().convertToStringUpdate("Update", updateInformation.getInfoHash(), updateInformation.getId(), updateInformation.getIp(), updateInformation.getPort());
								this.integrateNewPeer(updateInformation.getInfoHash(), updateInformation.getId(), updateInformation.getIp(), updateInformation.getPort());
								System.out.println("Sending UPDATE JMS message...");
							} else {
								System.out.println("The majority of the tracker slaves are NOT ready to receive updated :( (update declined)");
								// JMS message: Update ABORT
								updateMsg = new JMSXMLMessages().convertToStringUpdate("Abort", "", 1, "", -1);
								System.out.println("Sending ABORT JMS message...");
							}
							slaveResponseAvailabilityHashMap.remove(updateID);
							updatePeerList.remove(updateID);
							
							TextMessage txtmsg_updateabort = session.createTextMessage();
							txtmsg_updateabort.setText(updateMsg);
							txtmsg_updateabort.setStringProperty("Filter", "IncomingFromMaster");
							producer.send(txtmsg_updateabort);
							// No matter what the answer was (UPDATE or ABORT), the master is responsible for sending the list of peers
							// to the incoming peer who contacted the master:
							this.sendPeersList();
						}
						break;
					case "Update":
						// Firstly, we stop the thread that was keeping alive the communication between the master and slave:
						repoSyncTimeout.cancel();
						// Now, the tracker slave, depending on master's response (update or abort), will update the database with
						// the corresponding new information coming from the peer.
						String resolution = xml.getElementsByTagName("resolution").item(0).getTextContent();
						if (resolution.equals("Update")) {
							String infoHash = xml.getElementsByTagName("info").item(0).getAttributes().getNamedItem("torrentHash").getTextContent();
							String peerID = xml.getElementsByTagName("peerid").item(0).getTextContent();
							String peerIP = xml.getElementsByTagName("ip").item(0).getTextContent();
							int peerPort = Integer.parseInt(xml.getElementsByTagName("port").item(0).getTextContent()); 
							this.integrateNewPeer(infoHash, Integer.parseInt(peerID), peerIP, peerPort);
						} else if (resolution.equals("Abort")) {
							// The master has aborted the updating process. No slave will be getting updates.
							System.out.println("Less than 80% of the tracker slaves are ready to receive updates. Then, NO updates are sent!");	
						}
						break;
					default:
						break;
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * When a new Peer contacts the cluster of trackers (the master indeed), the master will have to send an UpdateRequest with
	 * the information regarding the new peer.
	 * This method will be called whenever the master (cluster of trackers) receives a new incoming peer.
	 */
	public void sendUpdateRequestMessage() {
		System.out.println("UpdateRequest JMS message sent!");
		try {
			// JMS message: UpdateRequest
			int updateId =0;
			do{
				updateId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
			}while(slaveResponseAvailabilityHashMap.containsKey(updateId));
			String ip = UUID.randomUUID().toString();
			String infohash = UUID.randomUUID().toString();
			int id = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
			int port = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
			updatePeerList.put(updateId, new PeerTrackerTemplate(id, ip, port, infohash));
			String updateReq = new JMSXMLMessages().convertToStringUpdateRequest(updateId);
			slaveResponseAvailabilityHashMap.put(updateId, new HashMap<String, Boolean>());
			TextMessage txtmsg = session.createTextMessage();
			txtmsg.setText(updateReq);
			txtmsg.setStringProperty("Filter", "IncomingFromMaster");
			producer.send(txtmsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks if exists the swarm related to the torrent. If not, a new swarm is created.
	 * Adds the incoming peer to the Peers' list of the swarm
	 * @param infoHash
	 * @param peerID
	 * @param peerIP
	 * @param peerPort
	 */
	private void integrateNewPeer(String infoHash, int peerID, String peerIP, int peerPort) {
		// -------------------------------------------
		// The following will CHANGE IN THE FUTURE when facing the implementation of the peers:							
		// First we check if the current swarm list contains the swarm related to this InfoHash (torrent):
//		Peer p = new Peer(peerID, peerIP, peerPort, 0, 0);
//		if (!dms.getSwarmList().containsKey(infoHash)) {
//			Swarm s = new Swarm(infoHash, "EXAMPLE_FILENAME", 200);
//			s.addPeerToList(peerID, p);
//			dms.notifySwarmChanged(s);
//		} else {
//			dms.getSwarmList().get(infoHash).addPeerToList(peerID, p);
//			dms.notifySwarmChanged(dms.getSwarmList().get(infoHash));
//		}
//		// Console outputs:
//		System.out.println("The properties of incoming Peer are:");
//		System.out.println("infoHash (Torrent): " + infoHash);
//		System.out.println("Peer ID: " + peerID);
//		System.out.println("Peer IP: " + peerIP);
//		System.out.println("Peer Port: " + peerPort);
//		
//		//Adding to database
//		database.insertTorrent(infoHash);
//		System.out.println(">>>>>>>>>>>>>>>>"+peerID);
//		database.insertPeer(peerID, peerIP, peerPort);
//		//How to UPDATE the bytes downloaded and left?
//		database.insertPeer_Torrent(peerID, infoHash, 0, 0, 0, 0);
		
	}
	
	/**
	 * Sends the list of peers back to the incoming peer who contacted the master
	 */
	public void sendPeersList() {
		// TODO
	}

}

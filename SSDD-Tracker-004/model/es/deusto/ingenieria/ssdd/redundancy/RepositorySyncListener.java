package es.deusto.ingenieria.ssdd.redundancy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.w3c.dom.Document;

import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.classes.PeerTorrent;
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
	private HashMap<Integer, HashMap<String, Boolean>> slaveResponseAvailabilityHashMap; // This HashMap stores True or False depending on the availability of the tracker slave for each and every peer request that is made.
																						// The first Key represents the UpdateID of the peer (so as to identify who is (which peer) requesting anything).
																						// The second Key represents a tracker.
	private HashMap<Integer, PeerTorrent> updateInformationPeerList; // Information related to any incoming peer that NEEDS to be updated and transmitted to every tracker. 
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
		this.updateInformationPeerList = new HashMap<Integer, PeerTorrent>();
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
						System.out.println("Is avaialbe to recieve updates??? -> " + dmc.isAvailableToReceiveUpdates());
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
							System.out.println("amountPositiveAnswers = " + amountPositiveAnswers + " | admissionRate= "+ admissionRate);
							if (amountPositiveAnswers >= admissionRate) {
								System.out.println("The majority of the tracker slaves are ready to receive updates!");
								// JMS message: Update UDPATE
								PeerTorrent updateInformation = updateInformationPeerList.get(updateID);
								updateMsg = new JMSXMLMessages().convertToStringUpdate("Update", updateInformation.getInfoHash(), updateInformation.getId(), updateInformation.getIp(), updateInformation.getPort());
								// We make sure the master ALSO updates the information:
								this.integrateOrUpdateNewPeer(updateInformation.getInfoHash(), updateInformation.getId(), updateInformation.getIp(), updateInformation.getPort());
								System.out.println("Sending UPDATE JMS message...");
							} else {
								System.out.println("The majority of the tracker slaves are NOT ready to receive updated :( (update declined)");
								// JMS message: Update ABORT
								updateMsg = new JMSXMLMessages().convertToStringUpdate("Abort", "", 1, "", -1);
								System.out.println("Sending ABORT JMS message...");
							}
							slaveResponseAvailabilityHashMap.remove(updateID);
							updateInformationPeerList.remove(updateID);
							
							TextMessage txtmsg_updateabort = session.createTextMessage();
							txtmsg_updateabort.setText(updateMsg);
							txtmsg_updateabort.setStringProperty("Filter", "IncomingFromMaster");
							producer.send(txtmsg_updateabort);
							// No matter what the answer was (UPDATE or ABORT), the master is responsible for sending the list of peers
							// to the incoming peer who contacted the master. This is HANDLED in 'MulticastSocketTracker' java file.
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
							this.integrateOrUpdateNewPeer(infoHash, Integer.parseInt(peerID), peerIP, peerPort);
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
	 * This method will be called whenever the master (cluster of trackers) receives a new incoming peer or an AnnounceRequest.
	 */
	public void sendUpdateRequestMessage(String IP, int port, int peerID, String infohash) {
		if (dmt.getTrackerlist().size() != 1) {
			try {
				// JMS message: UpdateRequest
				int updateId =0;
				do{
					updateId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
				}while(slaveResponseAvailabilityHashMap.containsKey(updateId));
				updateInformationPeerList.put(updateId, new PeerTorrent(peerID, IP, port, infohash)); // We save temporally the information to be spread over all trackers.
				slaveResponseAvailabilityHashMap.put(updateId, new HashMap<String, Boolean>()); // We introduce the UpdateID of the peer in the hashmap that will be used later on.
				String updateReq = new JMSXMLMessages().convertToStringUpdateRequest(updateId);
				TextMessage txtmsg = session.createTextMessage();
				txtmsg.setText(updateReq);
				txtmsg.setStringProperty("Filter", "IncomingFromMaster");
				producer.send(txtmsg);
				System.out.println("UpdateRequest JMS message sent!");
			} catch (Exception e) {
				e.printStackTrace();
			}	
		} else {
			this.integrateOrUpdateNewPeer(infohash, peerID, IP, port);
		}
	}
	
	/**
	 * Save in the database the information that is stored in memory regarding the incoming peer.
	 * There is no need to check if the swarm exist or anything like that. All that was taken into account before.
	 * @param infoHash
	 * @param peerID
	 * @param peerIP
	 * @param peerPort
	 */
	private void integrateOrUpdateNewPeer(String infoHash, int peerID, String peerIP, int peerPort) {
//		Swarm temp0 = dms.getSwarmList().get(infoHash);
//		System.out.println("peerID (integrateNewPeer) = " + peerID);
//		Peer temp1 = temp0.getPeerListHashMap().get(peerID);
//		PeerTorrent temp2 = temp1.getSwarmList().get(infoHash);
		// -----------
		PeerTorrent temp = dms.getSwarmList().get(infoHash).getPeerListHashMap().get(peerID).getSwarmList().get(infoHash);
		System.out.println("Saving data in the database...: PeerID-> "+peerID+" | Peer IP-> "+peerIP+" | Peer port-> "+peerPort+" | InfoHash-> "+infoHash+" | Downloaded-> "+temp.getDownloaded()+" | Uploaded-> " + temp.getUploaded()+" | "+temp.getLeft());
		int returnv = database.updatePeerTorrent(peerID, infoHash, temp.getUploaded(), temp.getDownloaded(), temp.getLeft());
		if (returnv == -2) { // This means that no peer-torrent was found in the database with the specified infoHash. So, it's necessary to insert the peer, torrent and repeat the peer-torrent information.
			System.out.println("No peer-torrent found. Inserting peer + torrent + peer-torrent...");
			database.insertPeer(peerID, peerIP, peerPort);
			database.insertTorrent(infoHash);
			database.insertPeer_Torrent(peerID, infoHash, temp.getUploaded(), temp.getDownloaded(), temp.getLeft());
		}
	}

}

package es.deusto.ingenieria.ssdd.tracker;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;
import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.data.DBManager;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;

/**
 * This class represents the initialization process of a tracker.
 * 1) This Runnable class gets access to the "KeepAliveTopic" topic through JMS
 * 2) Waits for 5 seconds listening to Keepalive messages
 * 3) Evaluates whether the ID introduced by the user is taken or not comparing it with the tracker list
 * 		3.1) If the tracker list is empty, this means that the current tracker is actually the master: the tracker master is created + its own database
 * 		3.2) If the ID collides with an already existing one, a randomly chosen ID is selected + a JMS message (IDSelection) is sent to the master
 * 4) The trackers starts sending Keepalive messages 
 * 5) Afterwards, a thread is started to check whether any of the trackers shutdown or not.
 * @author kevin & aitor
 *
 */
public class EntranceAndKeepAlive implements Runnable{
	
	private DataModelConfiguration dmc;
	private DataModelTracker dmt;
	public static int trackerID; // This is the ID of the tracker, the purpose is to make it available publicly to catch it whenever we want.
	
	public EntranceAndKeepAlive(DataModelConfiguration dmc, DataModelTracker dmt){
		this.dmc = dmc;
		this.dmt = dmt;
		this.trackerID = Integer.parseInt(dmc.getId());
	}

	@Override
	public void run() {
		try {
			Connection connection = null;
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("KeepAliveTopic"); 
            
            // Consumer1 subscribes to KeepAliveTopic
            MessageConsumer consumer1 = session.createConsumer(topic);
            consumer1.setMessageListener(new KeepALiveListener(dmt, dmc, session, topic));
            
            connection.start();    
            
            //Wait 5 seconds to listen the keepalive messages
            Thread.sleep(5000);
            
            //Look at DataModel if the user selected ID is available
            System.out.println("You have chosen the following ID='"+dmc.getId()+"', checking for ID availability...");
            if(dmt.trackerList.get(Integer.parseInt(dmc.getId()))!=null)
            {
            	// The tracker ID is not available, therefore, we assign a random one
            	System.out.println("The selected ID is in use. Taking random one...");
            	dmc.setMaster(false);
            	int randomID =0;
            	do {
            		randomID = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
				} while (dmt.trackerList.containsKey(randomID));
            	
            	// JMS message: IDSelection
            	String idselect = new JMSXMLMessages().convertToStringIDSelection(randomID);
            	TextMessage msg = session.createTextMessage();
	            msg.setText(idselect);
	            MessageProducer producer = session.createProducer(topic);
	            producer.send(msg);
	            DataModelTracker.idRequestUniqueID = msg.getJMSMessageID();
	            trackerID = randomID; 
	            //Wait 5 seconds for master response
	            Thread.sleep(5000);
	            if(dmt.idCorrect){
	            	//Masters response is positive, so you have a database now and the ID.
	            	dmc.setMaster(false);
	            	dmc.setId(randomID+"");
	            	Tracker t = new Tracker(Integer.parseInt(dmc.getId()), dmc.getIp(), dmc.getPort(), false, new Date());
	            	dmt.trackerList.put(t.getId(), t);
	            	//Start sending KeepALives:
            		KeepALiveSender kaps= new KeepALiveSender(dmc, producer, session);
            		dmt.keepaliveSender = kaps;
            		dmt.threadKeepaliveSender = new Thread(kaps);
	        		dmt.threadKeepaliveSender.start();
	        		//Start checking the availability of the rest of the trackers:
	        		KeepALiveTimeChecker kaltc= new KeepALiveTimeChecker(dmt, dmc, session, topic);
            		dmt.keepaliveChecker = kaltc;
	        		dmt.threadKeepaliveChecker = new Thread(kaltc);
	        		dmt.threadKeepaliveChecker.start();
	        		System.out.println("Your randomly chosen ID was approved by the tracker master successfuly!");
	        		System.out.println("You are a TRACKER SLAVE with ID='"+dmc.getId()+"' currently sending and checking keepalive messages...");
	            }
	            else{
	            	//Master response is negative, so you have to start the process again
	            	System.out.println("Tracker master has rejected your ID, write another ID and try again.");
//	            	EntranceAndKeepAlive keepalive = new EntranceAndKeepAlive(dmc, dmt);
//	        		dmt.threadKeepaliveListener = new Thread(keepalive);
//	        		dmt.threadKeepaliveListener.start();
	            }
            	
            }
            else{
            	// The tracker ID is available, we still have to check whether there exists a master or not: 
            	if(dmt.trackerList.isEmpty()){ // Empty: this means that the current running tracker is the Master
            		// We create the tracker and add it to the tracker list:
            		dmc.setMaster(true);
            		Tracker t = new Tracker(Integer.parseInt(dmc.getId()), dmc.getIp(), dmc.getPort(), true, new Date());
            		dmt.trackerList.put(t.getId(), t);
            		//Initialize a new database
            		DBManager database = new DBManager("model/es/deusto/ingenieria/ssdd/redundancy/databases/TrackerDB_"+EntranceAndKeepAlive.trackerID+".db");
            		//Start sending KeepALives
            		MessageProducer producer = session.createProducer(topic);
            		KeepALiveSender kaps= new KeepALiveSender(dmc, producer, session);
            		dmt.keepaliveSender = kaps;
            		dmt.threadKeepaliveSender = new Thread(kaps);
	        		dmt.threadKeepaliveSender.start();
	        		//Start checking the availability of the rest of the trackers:
	        		KeepALiveTimeChecker kaltc= new KeepALiveTimeChecker(dmt, dmc,  session, topic);
            		dmt.keepaliveChecker = kaltc;
	        		dmt.threadKeepaliveChecker = new Thread(kaltc);
	        		dmt.threadKeepaliveChecker.start();
	        		System.out.println("Your ID is elegible and available!");
	        		System.out.println("You are a TRACKER MASTER with ID='"+dmc.getId()+"' currently sending and checking keepalive messages...");
	        		Topic topic2 = session.createTopic("RepositorySyncTopic"); 
            	} else { // Available: this means the ID that the user assigned to the tracker through the GUI, is not taken!
            		dmc.setMaster(false);
            		String idselect = new JMSXMLMessages().convertToStringIDSelection(Integer.parseInt(dmc.getId()));
    	            TextMessage msg = session.createTextMessage(idselect);
    	            MessageProducer producer = session.createProducer(topic);
    	            producer.send(msg);
    	            DataModelTracker.idRequestUniqueID = msg.getJMSMessageID();
    	            //Wait 5 seconds for master response
    	            Thread.sleep(5000);
    	            if(dmt.idCorrect){
    	            	System.out.println("Correct! Your ID is available!");
    	            	//Masters response is positive, so you have a database now and the ID.
    	            	trackerID = Integer.parseInt(dmc.getId());
    	            	dmc.setMaster(false);
    	            	Tracker t = new Tracker(Integer.parseInt(dmc.getId()), dmc.getIp(), dmc.getPort(), false, new Date());
    	            	dmt.trackerList.put(t.getId(), t);
    	            	//Start sending KeepALives
                		KeepALiveSender kaps= new KeepALiveSender(dmc, producer, session);
                		dmt.keepaliveSender = kaps;
                		dmt.threadKeepaliveSender = new Thread(kaps);
    	        		dmt.threadKeepaliveSender.start();
    	        		KeepALiveTimeChecker kaltc= new KeepALiveTimeChecker(dmt, dmc, session, topic);
                		dmt.keepaliveChecker = kaltc;
    	        		dmt.threadKeepaliveChecker = new Thread(kaltc);
    	        		dmt.threadKeepaliveChecker.start();
    	        		System.out.println("Your randomly chosen ID was approved by the tracker master successfuly!");
    	        		System.out.println("You are a TRACKER SLAVE with ID='"+dmc.getId()+"' currently sending and checking keepalive messages...");
    	            }
    	            else{
    	            	//Master response is negative, so you have to start the process again
    	            	System.out.println("Tracker master has rejected your ID, write another ID and try again.");
//    	            	EntranceAndKeepAlive keepalive = new EntranceAndKeepAlive(dmc, dmt);
//    	        		dmt.threadKeepaliveListener = new Thread(keepalive);
//    	        		dmt.threadKeepaliveListener.start();
    	            }
            	}
            	
            }
            		
		} catch (InterruptedException e) {
			System.out.println("Se ejecuta esto???");
			Thread.currentThread().interrupt();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		dmc.setTrackerSetUpFinished(true); // This indicates that the 'entrance-and-keep-alive' process has ended.
	}
	

}

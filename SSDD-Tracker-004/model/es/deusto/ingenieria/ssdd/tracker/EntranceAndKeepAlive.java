package es.deusto.ingenieria.ssdd.tracker;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.data.DBManager;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;

/**
 * This class represents the initialization process of a tracker.
 * 1) This Runnable class gets access to the "KeepAliveTopic" topic through JMS
 * 2) Waits for 3 seconds listening to Keepalive messages
 * 3) Evaluates whether the ID introduced by the user is taken or not comparing it with the tracker list
 * 		3.1) If the tracker list is empty, this means that the current tracker is actually the master: the tracker master is created + its own database
 * 		3.2) If the ID collides with an already existing one, a randomly chosen ID is selected + a JMS message (IDSelection) is sent to the master
 * 4) The trackers starts sending Keepalive messages 
 * @author kevin
 *
 */
public class EntranceAndKeepAlive implements Runnable{
	
	private DataModelConfiguration dmc;
	private DataModelTracker dmt;
	
	public EntranceAndKeepAlive(DataModelConfiguration dmc, DataModelTracker dmt){
		this.dmc = dmc;
		this.dmt = dmt;
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
            consumer1.setMessageListener(new KeepALiveListener(dmt));
            
            connection.start();    
            
            //Wait for 3 seconds to listen the keepalive messages
            Thread.sleep(3000);
            
            //Look at DataModel if the user selected ID is available
            if(dmt.trackerList.containsKey(dmc.getId()))
            {
            	//unavailable
            	int randomID =0;
            	do {
            		randomID = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
				} while (dmt.trackerList.containsKey(randomID));
            	
            	//Select ID
            	String idselect = new JMSXMLMessages().convertToStringIDSelection(randomID);
	            Message msg = session.createTextMessage();
	            System.out.println(msg.getJMSMessageID());
	            dmt.idRequestUniqueID = msg.getJMSMessageID();
	            MessageProducer producer = session.createProducer(topic);
	            producer.send(msg);
	            
	            //Wait 5 seconds for master response
	            Thread.sleep(5000);
	            if(dmt.idCorrect){
	            	//Masters response is positive, so you have a database now and the ID.
	            	//¿Add also this tracker to the list?
	            	dmc.setMaster(false);
	            	Tracker t = new Tracker(Integer.parseInt(dmc.getId()), false, new Date());
	            	dmt.trackerList.put(t.getId(), t);
	            	//Start sending KeepALives
            		KeepALiveSender kaps= new KeepALiveSender(dmc, producer, session);
            		dmt.threadKeepaliveSender = new Thread(kaps);
	        		dmt.threadKeepaliveSender.start();
	            	// What do we do now with the database? where is it?
	            }
	            else{
	            	//Master response is negative, so you have to start the process again
	            	EntranceAndKeepAlive keepalive = new EntranceAndKeepAlive(dmc, dmt);
	        		dmt.threadKeepaliveListener = new Thread(keepalive);
	        		dmt.threadKeepaliveListener.start();
	            }
            	
            }
            else{
            	// Available or Empty: this means that the current running tracker is the Master
            	if(dmt.trackerList.isEmpty()){
            		// We create the tracker and add it to the tracker list:
            		dmc.setMaster(true);
            		Tracker t = new Tracker(Integer.parseInt(dmc.getId()), true, new Date());
            		dmt.trackerList.put(t.getId(), t);
            		//Initialize a new database
            		DBManager database = new DBManager("model/es/deusto/ingenieria/ssdd/redundancy/databases/TrackerDB_master.db");
            		//Start sending KeepALives
            		MessageProducer producer = session.createProducer(topic);
            		KeepALiveSender kaps= new KeepALiveSender(dmc, producer, session);
            		dmt.threadKeepaliveSender = new Thread(kaps);
	        		dmt.threadKeepaliveSender.start();
            	}
            	
            }
            

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}

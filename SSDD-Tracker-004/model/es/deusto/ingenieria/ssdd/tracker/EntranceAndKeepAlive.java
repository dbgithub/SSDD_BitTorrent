package es.deusto.ingenieria.ssdd.tracker;

import java.net.URI;
import java.net.URISyntaxException;
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

import es.deusto.ingenieria.ssdd.data.DBManager;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;
public class EntranceAndKeepAlive implements Runnable{
	
	private DataModelConfiguration dmc;
	private DataModelTracker dmt;
	private boolean idCorrect = false;
	
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
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("KeepAliveTopic"); 
            
            // Consumer1 subscribes to KeepAliveTopic
            MessageConsumer consumer1 = session.createConsumer(topic);
            consumer1.setMessageListener(new KeepALiveListener(dmt, idCorrect));
            
            connection.start();    
            
            //Wait 3 seconds to listen the keepalive messages
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
	            if(idCorrect){
	            	//Masters response is positive, so you have a database now and the ID.
	            	//¿Add also this tracker to the list?
	            	
	            }
	            else{
	            	//Master response is negative, so you have to start the process again
	            	EntranceAndKeepAlive keepalive = new EntranceAndKeepAlive(dmc, dmt);
	        		dmt.threadKeepaliveListener = new Thread(keepalive);
	        		dmt.threadKeepaliveListener.start();
	            }
            	
            }
            else{
            	//available or Empty
            	if(dmt.trackerList.isEmpty()){
            		//¿Add also this tracker to the list?
            		//Init new database
            		DBManager database = new DBManager("test/db/test.db");
            		//Start sending KeepALives
            		KeepALiveSending kaps= new KeepALiveSending(dmc);
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

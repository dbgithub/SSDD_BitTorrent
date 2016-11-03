package es.deusto.ingenieria.ssdd.tracker;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadLocalRandom;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
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
public class KeepALiveSender implements Runnable{
	
	private DataModelConfiguration dmc;
	private boolean idCorrect = false;
	private MessageProducer producer;
	private Session session;
	
	public KeepALiveSender(DataModelConfiguration dmc, MessageProducer producer, Session s){
		this.dmc = dmc;
		this.producer = producer;
		this.session = s;
	}

	@Override
	public void run() {
		// TODO: copio y pego el CONNECTION, CONNECTION FACTORY y demas aqui? o lo paso como variable?
		while(true) {
			String keepalivestr = new JMSXMLMessages().convertToStringKeepAlive(dmc.getId(), (dmc.isMaster()) ? "Master" : "Slave", dmc.getIp(), dmc.getPort());
			// TODO: send keepalive message
			Message msg;
			try {
				msg = session.createTextMessage();
				System.out.println(msg.getJMSMessageID());
		        producer.send(msg);
			} catch (JMSException e1) {
				System.out.println("Error JMS with the KeepALiveSender");
				e1.printStackTrace();
			}
	        
			try {
				// Every second, the tracker will send a Keepalive message
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}

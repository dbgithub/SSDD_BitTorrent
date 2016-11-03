package es.deusto.ingenieria.ssdd.tracker;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;
public class KeepALiveSender implements Runnable{
	
	private DataModelConfiguration dmc;
	private MessageProducer producer;
	private Session session;
	
	public KeepALiveSender(DataModelConfiguration dmc, MessageProducer producer, Session s){
		this.dmc = dmc;
		this.producer = producer;
		this.session = s;
	}

	@Override
	public void run() {
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

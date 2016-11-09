package es.deusto.ingenieria.ssdd.tracker;

import java.util.concurrent.ThreadLocalRandom;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.net.JMSAppender;
import org.w3c.dom.Document;

import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;

public class RepositorySyncListener implements MessageListener{
	
	private DataModelTracker dmt;
	private DataModelConfiguration dmc;
	private MessageProducer producer;
	private Session session;
	
	public RepositorySyncListener(DataModelTracker dmt, DataModelConfiguration dmc, MessageProducer p, Session s) {
		this.dmt = dmt;
		this.dmc = dmc;
		this.producer = p;
		this.session = s;
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
						int updateId = Integer.parseInt(xml.getElementsByTagName("updateid").item(0).getTextContent());
						String slaveresponse = new JMSXMLMessages().convertToStringSlaveResponse(updateId, dmc.getId(), dmc.isAvailabilityToReceiveUpdates() ? "OK" : "ERROR");
						TextMessage txtmsg_update = session.createTextMessage();
						txtmsg_update.setText(slaveresponse);
						producer.send(txtmsg_update);
						break;
					case "SlaveResponse":
						break;
					case "Update":
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
	
	public void sendUpdateRequestMessage() {
		try {
			String updateReq = new JMSXMLMessages().convertToStringUpdateRequest(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));
			TextMessage txtmsg = session.createTextMessage();
			txtmsg.setText(updateReq);
			txtmsg.setStringProperty("Filter", "IncomingFromMaster");
			producer.send(txtmsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

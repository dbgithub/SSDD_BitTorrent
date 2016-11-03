package es.deusto.ingenieria.ssdd.tracker;

import java.util.Date;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.w3c.dom.Document;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;

public class KeepALiveListener implements MessageListener{
	
	
	private DataModelTracker dmt;
	
	public KeepALiveListener(DataModelTracker dmt) {
		this.dmt = dmt;
	}

	@Override
	public void onMessage(Message message) {
		if (message != null) {
			if(message instanceof TextMessage){
				try {
					System.out.println("Unique ID: "+message.getJMSMessageID());
					TextMessage txtmessage = (TextMessage) message;
					System.out.println(txtmessage.getText());
					JMSXMLMessages parser = new JMSXMLMessages();
					Document xml = parser.convertFromStringToXML(txtmessage.getText());
					String type = xml.getElementsByTagName("type").item(0).getTextContent();
					switch (type) {
					case "KeepAlive":
						int id = Integer.parseInt(xml.getElementsByTagName("id").item(0).getTextContent());
						if(dmt.trackerList.containsKey(id)){
							Tracker t = dmt.trackerList.get(id);
							t.setLastKeepAlive(new Date());
							dmt.notifyTrackerChanged(t);
						}
						else{
							String typeTracker = xml.getElementsByTagName("typeTracker").item(0).getTextContent();
							boolean master = false;
							if(typeTracker.equals("Master")){
								master = true;
							}
							Tracker newOne = new Tracker(id, master, new Date());
							dmt.trackerList.put(id, newOne);
							dmt.notifyTrackerChanged(newOne);
						}
						break;
					case "NegativeIDReq":
						//Check if the message is for this tracker
						if(dmt.idRequestUniqueID.equals(message.getJMSMessageID())){
							//this message is not for me
							dmt.idCorrect = false;
						}
					default:
						break;
					}
						
					
				} catch (Exception e) {
					System.err.println("# KeepALiveListener error: " + e.getMessage());
				}
			} else {
				//Check if the message is for this tracker
				if(dmt.idRequestUniqueID.equals(message.getJMSMessageID())){
					//this message is for me
					dmt.idCorrect = true;
					//Save database
					BytesMessage databaseMessage = (BytesMessage) message;
					
				}
			}
		}
		
	}

}

package es.deusto.ingenieria.ssdd.tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.w3c.dom.Document;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;

public class KeepALiveListener implements MessageListener{
	
	
	private DataModelTracker dmt;
	private Session session;
	private Topic topic;
	
	public KeepALiveListener(DataModelTracker dmt, Session s, Topic t) {
		this.dmt = dmt;
		this.session = s;
		this.topic = t;
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
						String ip = xml.getElementsByTagName("ip").item(0).getTextContent();
						int port = Integer.parseInt(xml.getElementsByTagName("port").item(0).getTextContent());
						if(dmt.trackerList.containsKey(id)){
							Tracker t = dmt.trackerList.get(id);
							t.setLastKeepAlive(new Date());
							t.setIp(ip);
							t.setPort(port);
							dmt.notifyTrackerChanged(t);
						}
						else{
							String typeTracker = xml.getElementsByTagName("typeTracker").item(0).getTextContent();
							boolean master = false;
							if(typeTracker.equals("Master")){
								master = true;
							}
							Tracker newOne = new Tracker(id, ip, port, master, new Date());
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
					case "IDSelection":
						// **PREGUNTAR a Kevin si este 'case' esta bien definido aqui donde esta.
						// This 'case' should check whether the ID requested from the new tracker is free or already taken.
						// If it is not taken, then, the master sends back the database to the tracker slave.
						int trackerid = Integer.parseInt(xml.getElementsByTagName("id").item(0).getTextContent());
						if (!dmt.trackerList.containsKey(trackerid)) {
							BytesMessage masterdb = session.createBytesMessage();
							FileInputStream db_stream = new FileInputStream("model/es/deusto/ingenieria/ssdd/redundancy/databases/TrackerDB_"+EntranceAndKeepAlive.trackerID+".db");
							int bytes_read;
							byte[] buff = new byte[64];
							while ((bytes_read = db_stream.read(buff)) != -1) {
								masterdb.writeBytes(buff, 0, bytes_read);
							}
							MessageProducer producer = session.createProducer(topic);
				            producer.send(masterdb);
						}
					default:
						break;
					}
						
					
				} catch (Exception e) {
					System.err.println("# KeepALiveListener error: " + e.getMessage());
				}
			} else {
				// This 'else' statement will happen when the JMS message sent contains a 'body' entirely full of bytes, that is, not a text message.
				try {
					//Check if the message is for this tracker
					if(dmt.idRequestUniqueID.equals(message.getJMSMessageID())){
						//this message is for me
						dmt.idCorrect = true;
						//Save database in a local file
						BytesMessage databaseMessage = (BytesMessage) message;
						byte[] incomingDB = new byte[(int) databaseMessage.getBodyLength()];
						databaseMessage.readBytes(incomingDB, (int)databaseMessage.getBodyLength());
						File db = new File("model/es/deusto/ingenieria/ssdd/redundancy/databases/TrackerDB_"+EntranceAndKeepAlive.trackerID+".db");
						FileOutputStream db_stream = new FileOutputStream(db);
						db_stream.write(incomingDB);
						db_stream.close();
					}
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

}

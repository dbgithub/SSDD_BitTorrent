package es.deusto.ingenieria.ssdd.tracker;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;

public class KeepALiveTimeChecker implements Runnable{
	
	private DataModelTracker dmt;
	private DataModelConfiguration dmc;
	private Session session;
	private Topic topic;
	volatile boolean cancel = false;
	
	public KeepALiveTimeChecker(DataModelTracker dmt, DataModelConfiguration dmc, Session s, Topic t) {
		this.session = s;
		this.topic = t;
		this.dmt = dmt;
		this.dmc = dmc;
	}

	@Override
	public void run() {
		boolean chooseMaster = false;
		while(!cancel){
			try {
				for (Iterator<Tracker> iterator = dmt.trackerList.values().iterator(); iterator.hasNext(); ) {
				    Tracker t = iterator.next();
				    Date last = t.getLastKeepAlive();
					long diffInMillies = new Date().getTime() - last.getTime();
				    long secondsPassed = TimeUnit.SECONDS.convert(diffInMillies,TimeUnit.MILLISECONDS);
				    if(secondsPassed >= 2){
				    	if(t.getMaster()){
				    		//The tracker that dissapeared was the Master
				    		chooseMaster = true;
				    	}
				    	iterator.remove();
				    }
				}
				if(chooseMaster){
					chooseMaster = false;
					boolean mineBigger = true;
					for (Iterator<Tracker> iterator = dmt.trackerList.values().iterator(); iterator.hasNext(); ) {
						Tracker t = iterator.next();
						if(EntranceAndKeepAlive.trackerID < t.getId()){
							mineBigger = false;
							break;
						}
					}
					if(mineBigger){
						//Send a message to the other trackers saying that I'm the new Master
						String masterproclamation = new JMSXMLMessages().convertToStringMasterProclamation(""+EntranceAndKeepAlive.trackerID);
						// TODO: send keepalive message
						TextMessage msg;
						try {
							dmc.setMaster(true);
							msg = session.createTextMessage();
							msg.setText(masterproclamation);
							MessageProducer producer = session.createProducer(topic);
					        producer.send(msg);
						} catch (JMSException e1) {
							System.out.println("Error JMS with the KeepALiveTimeChecker");
							e1.printStackTrace();
						}
					}
				}
				//Checks every 1 sec
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void cancel() {
        cancel = true;
    }

}

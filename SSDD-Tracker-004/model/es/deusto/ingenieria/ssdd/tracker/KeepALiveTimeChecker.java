package es.deusto.ingenieria.ssdd.tracker;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.data.DBManager;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelSwarm;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.redundancy.RepositorySyncListener;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;

public class KeepALiveTimeChecker implements Runnable{
	
	private DataModelTracker dmt;
	private DataModelConfiguration dmc;
	private DataModelSwarm dms;
	private Session session;
	private Topic topic;
	private DBManager database;
	volatile boolean cancel = false;
	
	public KeepALiveTimeChecker(DataModelTracker dmt, DataModelConfiguration dmc, Session s, Topic t, DBManager database) {
		this.session = s;
		this.topic = t;
		this.dmt = dmt;
		this.dmc = dmc;
		this.database = database;
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
				    		//The tracker that disappeared was the Master
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
						// JMS message: MasterProclamation
						String masterproclamation = new JMSXMLMessages().convertToStringMasterProclamation(""+EntranceAndKeepAlive.trackerID);
						TextMessage msg;
						try {
							dmc.setMaster(true);
							msg = session.createTextMessage();
							msg.setText(masterproclamation);
							MessageProducer producer = session.createProducer(topic);
					        producer.send(msg);
					        // Since this tracker, the current tracker has proclaimed itself the MASTER, then, it is necessary
					        // to change its roles when it comes to the listen to the RepositorySyncrhonization update messages:
					        Topic topic2 = session.createTopic("RepositorySyncTopic"); 
					        MessageConsumer consumer_master = session.createConsumer(topic2, "Filter = 'IncomingFromSlave'", false); // We want to filter just messages coming from slaves
					        MessageProducer producer_master = session.createProducer(topic2);
				      		RepositorySyncListener rsl = new RepositorySyncListener(dmt, dmc, dms, producer_master, session, database);
			        		dmt.repositorySyncListener = rsl;
			        		consumer_master.setMessageListener(rsl);
			        		System.out.println("You, as a master with ID='"+dmc.getId()+"', have joined to a new JMS topic regarding repository synchronization!");

						} catch (JMSException e1) {
							System.out.println("Error JMS with the KeepALiveTimeChecker");
							e1.printStackTrace();
						}
					}
				}
				//Checks every 1 sec
				Thread.sleep(1000);
			} catch (InterruptedException e) {		
				System.out.println("ERRRO with Thread sleep in 'KeepALiveTimeChecker' class");
				e.printStackTrace();
			}
		}
		
	}
	
	public void cancel() {
        cancel = true;
    }

}

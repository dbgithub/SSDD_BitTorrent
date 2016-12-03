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

import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.data.DBManager;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelSwarm;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.redundancy.RepositorySyncListener;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;

public class ConnectionIdChecker implements Runnable{
	
	private MulticastSocketTracker mst;
	volatile boolean cancel = false;
	
	public ConnectionIdChecker(MulticastSocketTracker mst) {
		this.mst = mst;
	}

	@Override
	public void run() {
		while(!cancel){
			//Check passed 10 secs
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (Iterator<Peer> iterator = mst.peerList.values().iterator(); iterator.hasNext(); ) {
			    Peer t = iterator.next();
			    Date last = t.getConnection_id_lastupdate();
				long diffInMillies = new Date().getTime() - last.getTime();
			    long secondsPassed = TimeUnit.SECONDS.convert(diffInMillies,TimeUnit.MILLISECONDS);
			    if(secondsPassed >= 120){
			    	//This means that have passed two minutes and the peer didn't renew the connection id
			    	//TODO: Coordinate other trackers to delete the peer from the swarm and database
			    }
			}
		}	
	}
	
	public void cancel() {
        cancel = true;
    }

}

package es.deusto.ingenieria.ssdd.tracker;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.data.DBManager;
import es.deusto.ingenieria.ssdd.data.DataModelPeer;

public class ConnectionIdChecker implements Runnable{
	
	private DBManager dbm;
	private DataModelPeer dmp;
	volatile boolean cancel = false;
	
	public ConnectionIdChecker(DataModelPeer dmp) {
		this.dmp = dmp;
	}

	@Override
	public void run() {
		while(!cancel){
			//Check passed 10 secs
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (Iterator<Peer> iterator = dmp.peerlist.values().iterator(); iterator.hasNext(); ) {
			    Peer t = iterator.next();
			    Date last = t.getConnection_id_lastupdate();
				long diffInMillies = new Date().getTime() - last.getTime();
			    long secondsPassed = TimeUnit.SECONDS.convert(diffInMillies,TimeUnit.MILLISECONDS);
			    if(secondsPassed >= 120){ //This means that have passed two minutes and the peer didn't renew the connection id
			    	dbm.deletePeer(t.getId());
			    	dmp.getPeerlist().remove(t.getTransaction_id());
			    	// TODO: Delete the peer also from the Swarm list? (DataModelSwarm)
			    	dmp.notifyPeerChanged(t);
			    	System.out.println("[ConnectionIdChecker]: the peer with ID='"+t.getId()+"' was removed from database because it didn't renew the ID!");
			    }
			}
		}	
	}
	
	public void cancel() {
        cancel = true;
    }

}

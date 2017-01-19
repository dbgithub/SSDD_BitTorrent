package es.deusto.ingenieria.ssdd.controller;

import java.net.DatagramSocket;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import es.deusto.ingenieria.ssdd.classes.Swarm;

/**
 * This runnable class will represent a thread to re-send an AnnounceRequest in the interval specified by the tracker.
 * The tracker is the one who is telling the peer at which pace should the peer send AnnounceRequests
 * @author aitor & kevin
 *
 */
public class DownloadStateNotifier implements Runnable{
	
	private DatagramSocket send;
	private DatagramSocket receive;
	private MetainfoHandlerSingleFile single;
	private String urlHash;
	private ClientController controller;
	private Swarm swarm;
	volatile boolean cancel = false;

	public DownloadStateNotifier(DatagramSocket send, DatagramSocket receive, MetainfoHandlerSingleFile single, ClientController controller, Swarm s) {
		this.send = send;
		this.receive = receive;
		this.single = single;
		this.urlHash = single.getMetainfo().getInfo().getHexInfoHash();
		this.controller = controller;
		this.swarm = s;
	}

	@Override
	public void run() {
		while(!cancel) {
			try {
				//The time is expressed in seconds and the sleep methods only accepts millis
				Thread.sleep(ClientController.interval*1000);
				//Get the actual status of the swarm
				Swarm temp = controller.torrents.get(urlHash);
				System.out.println(" - Sending a AnnounceRequest to RENEW the state:");
				controller.sendAndWaitUntilAnnounceResponseReceivedLoop(single, send, receive, temp.getDownloaded(), temp.getLeft(), temp.getUploaded(), swarm);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}		
	}

	public void cancel() {
        cancel = true;
    }
}

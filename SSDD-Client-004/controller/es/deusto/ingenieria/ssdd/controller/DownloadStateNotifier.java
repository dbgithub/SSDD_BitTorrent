package es.deusto.ingenieria.ssdd.controller;

import java.net.DatagramSocket;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import es.deusto.ingenieria.ssdd.classes.Swarm;

public class DownloadStateNotifier implements Runnable{
	
	private DatagramSocket send;
	private DatagramSocket receive;
	private MetainfoHandlerSingleFile single;
	private String urlHash;
	private ClientController controller;
	//The time between notifications in seconds
	private int interval;
	volatile boolean cancel = false;

	public DownloadStateNotifier(DatagramSocket send, DatagramSocket receive, MetainfoHandlerSingleFile single, int interval, ClientController controller) {
		this.send = send;
		this.receive = receive;
		this.single = single;
		this.urlHash = single.getMetainfo().getInfo().getHexInfoHash();
		this.interval = interval;
		this.controller = controller;
	}

	@Override
	public void run() {
		while(!cancel) {
			try {
				//The time is expressed in seconds and the sleep methods only accepts millis
				Thread.sleep(interval*1000);
				//Get the actual status of the swarm
				Swarm temp = controller.torrents.get(urlHash);
				System.out.println("Sending AnnounceRequest to renew the state...");
				controller.sendAndWaitUntilAnnounceResponseReceivedLoop(single, send, receive, temp.getDownloaded(), temp.getLeft(), temp.getUploaded());
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	public void cancel() {
        cancel = true;
    }
	
	private void setInterval(int interval){
		this.interval = interval;
	}
}

package es.deusto.ingenieria.ssdd.controller;

import java.net.DatagramSocket;
import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;

/**
 * This runnable class will represent a thread to renew the connection ID every 60 seconds.
 * The renewal of the connection ID is done in tracker side, not client side. The renewal of the connection ID is based on a random number
 * @author aitor & kevin
 *
 */
public class ConnectionIdRenewer implements Runnable{
	
	private DatagramSocket socketSend;
	private DatagramSocket socketReceive;
	private ClientController controller;
	private MetainfoHandlerSingleFile single;
	volatile boolean cancel = false;
	
	public ConnectionIdRenewer(DatagramSocket send, DatagramSocket receive, MetainfoHandlerSingleFile single, ClientController cc){
		this.socketSend = send;
		this.socketReceive = receive;
		this.controller = cc;
		this.single = single;
	}

	@Override
	public void run() {
		while(!cancel) {
			try {
				Thread.sleep(60000);
				controller.sendAndWaitUntilConnectResponseReceivedLoop(single, socketSend, socketReceive, false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void cancel() {
        cancel = true;
    }

}

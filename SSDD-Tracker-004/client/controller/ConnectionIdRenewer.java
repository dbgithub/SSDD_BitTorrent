package controller;

import java.net.DatagramSocket;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.util.JMSXMLMessages;
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
				controller.sendAndWaitUntilConnectResponseReceivedLoop(single, socketSend, socketReceive);
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

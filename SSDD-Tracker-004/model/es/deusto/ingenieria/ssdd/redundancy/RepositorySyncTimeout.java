package es.deusto.ingenieria.ssdd.redundancy;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

/**
 * Runnable class that ensures that the communication between the master and slaves is not broken.
 * After the timeout is fired, the same message is sent to the master!
 * @author aitor & kevin
 *
 */
public class RepositorySyncTimeout implements Runnable{
	
	private TextMessage txtmsg;
	private MessageProducer producer;
	volatile boolean cancel = false;
	
	public RepositorySyncTimeout(MessageProducer p, TextMessage msg){
		this.txtmsg = msg;
		this.producer = p;
	}

	@Override
	public void run() {
		while(!cancel) {
			try {
				Thread.sleep(3000);
				if(!cancel){
					producer.send(txtmsg);
				}
			} catch (InterruptedException e) {
				System.out.println("ERROR in 'RepositorySyncTimeout' runnable class");
				e.printStackTrace();
			} catch (JMSException e) {
				System.out.println("ERROR sending JMS message in 'RepositorySyncTimeout' runnable class");
				e.printStackTrace();
			}		
		}
	}
	
	public void cancel() {
        cancel = true;
    }

}

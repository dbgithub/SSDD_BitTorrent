package es.deusto.ingenieria.ssdd.view;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import es.deusto.ingenieria.ssdd.controller.ClientController;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

@SuppressWarnings("serial")
public class ClientFrame extends JFrame{
	
	  private JButton bChange ; // reference to the button object
	  private JButton bSendScrape; // the goal of this button's functionality is to ask for information regarding the swarms you "own"
	  private JLabel lExplanation; // explanation label
	  private ClientController controller;
	  
	  // constructor for ButtonFrame
	  public ClientFrame(ClientController cc)
	  {
	    super( "Client (peer)" );  // invoke the JFrame constructor
	    this.controller = cc;

	    lExplanation = new JLabel("<html>This is client side. In order to contact the cluster of trackers, please, submit a torrent file (.torrent) using the corresponding button</html>");
	    bChange = new JButton("Search for Torrent..."); // construct a JButton
	    bChange.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Add a Torrent file...");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				FileNameExtensionFilter torrentFilter = new FileNameExtensionFilter(
					     "Torrent files (*.torrent)", "torrent");
				chooser.setFileFilter(torrentFilter);
				
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					controller.startConnection(chooser.getSelectedFile());
				} else {
				  System.out.println("No Selection ");
				}
			}
		});
	    
	    bSendScrape = new JButton("Send Scrape request");
	    bSendScrape.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (ClientController.getTransactionID() != -1) {
					controller.sendAndWaitUntilScrapeResponseReceivedLoop();					
				} else {
					JOptionPane.showMessageDialog(ClientFrame.this, "Hey! You need to load a certain '.torrent' file to start the proccess \n" + 
				"of communicating with the cluster of trackers before sending the \nScrapeRequest", "Action not allowed!", JOptionPane.OK_OPTION);
				}
			}
		});
	    GroupLayout groupLayout = new GroupLayout(getContentPane());
	    groupLayout.setHorizontalGroup(
	    	groupLayout.createParallelGroup(Alignment.LEADING)
	    		.addGroup(groupLayout.createSequentialGroup()
	    			.addGap(45)
	    			.addComponent(lExplanation, GroupLayout.PREFERRED_SIZE, 510, Short.MAX_VALUE)
	    			.addGap(112))
	    		.addGroup(groupLayout.createSequentialGroup()
	    			.addGap(137)
	    			.addComponent(bChange)
	    			.addGap(5)
	    			.addComponent(bSendScrape)
	    			.addContainerGap(136, Short.MAX_VALUE))
	    );
	    groupLayout.setVerticalGroup(
	    	groupLayout.createParallelGroup(Alignment.LEADING)
	    		.addGroup(groupLayout.createSequentialGroup()
	    			.addContainerGap()
	    			.addComponent(lExplanation, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
	    			.addPreferredGap(ComponentPlacement.RELATED)
	    			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
	    				.addComponent(bChange)
	    				.addComponent(bSendScrape))
	    			.addGap(363))
	    );
	    getContentPane().setLayout(groupLayout);
	    setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	    this.setSize( 150, 75 );
	    this.setBounds(200, 200, 600, 480);
	    this.setVisible( true );   
	  }
	  
	  
	  public static void main(String[]args){
			try {
				// Set cross-platform Java L&F (also called "Metal")
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception ex) {
				System.err.println("Error updating look & feel");

			}

			// Schedule a job for the event-dispatching thread: creating and showing

			// this application's GUI.

			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				public void run() {

					new ClientFrame(new ClientController());

				}

			});
		  
	  }

}

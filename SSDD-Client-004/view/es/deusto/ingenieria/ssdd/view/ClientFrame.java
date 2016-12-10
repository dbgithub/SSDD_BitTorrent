package es.deusto.ingenieria.ssdd.view;


import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import es.deusto.ingenieria.ssdd.controller.ClientController;

@SuppressWarnings("serial")
public class ClientFrame extends JFrame{
	
	  private JButton bChange ; // reference to the button object
	  private ClientController controller;
	  
	  // constructor for ButtonFrame
	  public ClientFrame(ClientController cc)
	  {
	    super( "Cliente" );  // invoke the JFrame constructor
	    this.controller = cc;
	    setLayout( new FlowLayout() );      // set the layout manager

	    bChange = new JButton("Buscar Torrent"); // construct a JButton
	    bChange.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Añadir Torrent");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				FileNameExtensionFilter torrentFilter = new FileNameExtensionFilter(
					     "Ficheros torrent (*.torrent)", "torrent");
				chooser.setFileFilter(torrentFilter);
				
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					controller.startConnection(chooser.getSelectedFile());
				} else {
				  System.out.println("No Selection ");
				}
			}
		});
	    add( bChange );                     // add the button to the JFrame
	    setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	    this.setSize( 150, 75 );
	    this.setBounds(700, 100, 600, 480);
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

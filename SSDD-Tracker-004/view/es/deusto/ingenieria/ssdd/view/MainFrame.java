package es.deusto.ingenieria.ssdd.view;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import es.deusto.ingenieria.ssdd.controllers.DashboardController;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class is the main window. This JFrame contains the required tabs for the application
 * @author aitor & kevin
 *
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame{
	
	private JFrame basicFrame;
	private DashboardController dc;
	private JTabbedPane tabbedPane;
	private SeePeersPane peers;
	
	/**
	 * 'dc' is the controller from which the business logic will flow.
	 * This Constructor creates every tab, assigns them the Controller and
	 * guarantees that "Peers" tab is attached when needed.
	 * @param dc
	 */
	public MainFrame(DashboardController dc)
	{

		this.dc = dc;
		this.basicFrame = this;
		basicFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		tabbedPane = new JTabbedPane();
		// This listener ensures that the Peers tab disappear when it loses the focus.
		tabbedPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// When the user clicks on another tab, the recently opened tab (Peers) will disappear
				if (tabbedPane.getSelectedIndex() != 3 && tabbedPane.getTabCount() == 4) {
					tabbedPane.removeTabAt(3);
				}
			}
		});

		ConfigurationTrackerPane configuration = new ConfigurationTrackerPane(dc);
		tabbedPane.addTab("Configuration",configuration);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		this.dc.setConfigurationObserver(configuration);

		peers = new SeePeersPane(dc);
		this.dc.setPeersObserver(peers);
		
		SeeSwarmsPane swarms = new SeeSwarmsPane(dc);
		tabbedPane.addTab("Swarms", swarms);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		this.dc.setSwarmObserver(swarms);
		swarms.setMainFrame(basicFrame); // This is done like that so that we can have access to MainFrame from SeeSwarmsPane.java
		
		SeeTrackersPane trackers = new SeeTrackersPane(dc);
		tabbedPane.addTab("Trackers", trackers);
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
		this.dc.setTrackerObserver(trackers);
		
		this.setMinimumSize(new Dimension(600, 480));
		this.setBounds(100, 100, 600, 480);
		getContentPane().add(tabbedPane);
		this.setVisible(true);
		
		
		 // Here a prompt is defined to ensure the user really wants to exist the application
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Ask for confirmation before exiting the program.
				int option = JOptionPane.showConfirmDialog(
						basicFrame, 
						"Are you sure you want to close this tracker?",
						"Exit confirmation", 
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE);
				if (option == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
	}
	
	/**
	 * It adds dynamically a new tab when the user double clicks on a certain swarm
	 * This tab will be closed automatically when it lost focus
	 */
	public void addPeersListTab() {
		tabbedPane.addTab("Peers",peers);
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_3);
		tabbedPane.setSelectedIndex(3);	
	}
	
	public static void main(String[] args) {

		DashboardController dc = new DashboardController();
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

				new MainFrame(dc);

			}

		});

	}
}

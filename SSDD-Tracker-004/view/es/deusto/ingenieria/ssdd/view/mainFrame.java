package es.deusto.ingenieria.ssdd.view;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import es.deusto.ingenieria.ssdd.controllers.DashboardController;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class mainFrame extends JFrame{
	
	private JFrame basicFrame;
	private DashboardController dc;
	private JTabbedPane tabbedPane;
	private SeePeersPane peers;
	
	public mainFrame(DashboardController dc)
	{
		this.dc = dc;
		this.basicFrame = this;
		basicFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		tabbedPane = new JTabbedPane();
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
//		tabbedPane.addTab("Peers",peers);
//		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
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
		this.setBounds(100, 100, 450, 300);
		getContentPane().add(tabbedPane);
		this.setVisible(true);
		
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
	
	

}

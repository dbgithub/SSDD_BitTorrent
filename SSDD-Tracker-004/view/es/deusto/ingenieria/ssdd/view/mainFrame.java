package es.deusto.ingenieria.ssdd.view;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import es.deusto.ingenieria.ssdd.controllers.DashboardController;

public class mainFrame extends JFrame{
	
	private JFrame basicFrame;
	private DashboardController dc;
	
	public mainFrame(DashboardController dc)
	{
		this.dc = dc;
		this.basicFrame = this;
		JTabbedPane tabbedPane = new JTabbedPane();

		ConfigurationTrackerPane configuration = new ConfigurationTrackerPane(dc);
		tabbedPane.addTab("Configuration",configuration);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		this.dc.setConfigurationObserver(configuration);

		SeePeersPane peers = new SeePeersPane(dc);
		tabbedPane.addTab("Peers",peers);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		this.dc.setPeersObserver(peers);
		
		SeeSwarmsPane swarms = new SeeSwarmsPane(dc);
		tabbedPane.addTab("Swarms", swarms);
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
		this.dc.setSwarmObserver(swarms);
		
		SeeTrackersPane trackers = new SeeTrackersPane(dc);
		tabbedPane.addTab("Trackers", trackers);
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
		this.dc.setTrackerObserver(trackers);
		
		this.setMinimumSize(new Dimension(600, 480));
		this.setBounds(100, 100, 450, 300);
		this.add(tabbedPane);
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
	
	
	
	

}

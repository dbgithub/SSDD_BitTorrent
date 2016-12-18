package es.deusto.ingenieria.ssdd.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import es.deusto.ingenieria.ssdd.classes.Swarm;
import es.deusto.ingenieria.ssdd.controllers.*;
import es.deusto.ingenieria.ssdd.data.DataModelSwarm;

/**
 * This class is part of the GUI. It corresponds to the inner content of one of the tabs
 * of the main window
 * @author aitor & kevin
 *
 */
@SuppressWarnings("serial")
public class SeeSwarmsPane extends JPanel implements Observer{

	private JPanel SwarmsSee;
	private JFrame mainFrame;
	private JTable swarmTable;
	private DashboardController controller;

	/**
	 * Create the application.
	 */
	public SeeSwarmsPane() {
		initialize();
	}

	/**
	 * The View knows about the Controller, but no the other way around
	 * Everything the View does, it has to communicate it to the Controller
	 * @param dashboardController
	 */
	public SeeSwarmsPane(DashboardController dashboardController) {
		this.controller = dashboardController;
		initialize();
		this.SwarmsSee.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		SwarmsSee = this;
		SwarmsSee.setMinimumSize(new Dimension(600, 480));
		SwarmsSee.setBounds(100, 100, 600, 480);
		SwarmsSee.setBackground(new Color(0, 102, 153));
		
		JLabel lblSeeSwarms = new JLabel("See Swarms");
		lblSeeSwarms.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSeeSwarms.setHorizontalAlignment(SwingConstants.CENTER);
		lblSeeSwarms.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblSeeSwarms.setBorder(null);
		lblSeeSwarms.setForeground(Color.WHITE);
		lblSeeSwarms.setFont(new Font("Ubuntu", Font.BOLD, 34));
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(SwarmsSee);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(111)
					.addComponent(lblSeeSwarms, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
					.addGap(110))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(5)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
					.addGap(5))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(27)
					.addComponent(lblSeeSwarms)
					.addGap(40)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
					.addGap(5))
		);
		
		swarmTable = new JTable();
		swarmTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable tempTable = (JTable)e.getSource();
				    System.out.println("double clicked in row#" + (tempTable.rowAtPoint(e.getPoint()) + 1));
				    ((MainFrame) mainFrame).addPeersListTab();
				    controller.setCurrentlyDisplayedInfoHash((String)tempTable.getValueAt(tempTable.rowAtPoint(e.getPoint()), 0));
				  }
			}
		});
		swarmTable.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null},
				{null, null, null, null},
				{null, null, null, null},
				{null, null, null, null},
			},
			new String[] {
				"Swarm (infohash)", "Size", "Total seeders", "Total leechers"
			}
		) {
			boolean[] columnEditables = new boolean[] {
				false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		swarmTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		swarmTable.getColumnModel().getColumn(0).setMinWidth(150);
		swarmTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		swarmTable.getColumnModel().getColumn(1).setMinWidth(100);
		swarmTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		swarmTable.getColumnModel().getColumn(2).setMinWidth(100);
		swarmTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		swarmTable.getColumnModel().getColumn(3).setMinWidth(100);
		swarmTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		swarmTable.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		scrollPane.setViewportView(swarmTable);
		
		SwarmsSee.setLayout(groupLayout);
	}

	/**
	 * This method is called from the Model side, to provoke certain changes in the View. 
	 */
	@Override
	public void update(Observable o, Object arg) {
		if( o instanceof DataModelSwarm){
			//The update is related with the value that we are observing
			DataModelSwarm dms = (DataModelSwarm)o;
			SwingUtilities.invokeLater(new Runnable(){public void run(){
				int size = dms.getSwarmList().size();
				String[][] arrayTable = new String[size][4];
				int l = 0;
				for (Swarm sw : dms.getSwarmList().values()) {
					arrayTable[l][0] = sw.getInfoHash();
					arrayTable[l][1] = sw.getSize()+"";
					arrayTable[l][2] = sw.getTotalSeeders()+"";
					arrayTable[l][3] = sw.getTotalLeecher()+"";
				}
				swarmTable.setModel(new DefaultTableModel(arrayTable, new String[] {
						"Swarm (infohash)", "Size", "Total seeders", "Total leechers"
				}) {
					boolean[] columnEditables = new boolean[] {
							false, false, false, false
						};
					public boolean isCellEditable(int row, int column) {
						return columnEditables[column];
					}
				});
			}});
		}
		
	}

	/**
	 * @param mainFrame the mainFrame to set
	 */
	public void setMainFrame(JFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
}

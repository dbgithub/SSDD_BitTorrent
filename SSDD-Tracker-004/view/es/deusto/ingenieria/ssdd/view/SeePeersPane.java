package es.deusto.ingenieria.ssdd.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.util.Observable;
import java.util.Observer;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.controllers.*;

import es.deusto.ingenieria.ssdd.data.DataModelPeer;;

/**
 * This class is part of the GUI. It corresponds to the inner content of one of the tabs
 * of the main window
 * @author aitor & kevin
 *
 */
@SuppressWarnings("serial")
public class SeePeersPane extends JPanel implements Observer{

	private JPanel PeersSee;
	private JTable peerTable;
	private DashboardController controller;

	/**
	 * Create the application.
	 */
	public SeePeersPane() {
		initialize();
	}

	/**
	 * The View knows about the Controller, but no the other way around
	 * Everything the View does, it has to communicate it to the Controller
	 * @param dc
	 */
	public SeePeersPane(DashboardController dc) {
		this.controller = dc;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		PeersSee = this;
		PeersSee.setMinimumSize(new Dimension(600, 480));
		PeersSee.setBounds(100, 100, 600, 480);
		PeersSee.setBackground(new Color(0, 102, 153));
		
		JLabel lblPeersList = new JLabel("List of peers");
		lblPeersList.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPeersList.setHorizontalAlignment(SwingConstants.CENTER);
		lblPeersList.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblPeersList.setBorder(null);
		lblPeersList.setForeground(Color.WHITE);
		lblPeersList.setFont(new Font("Ubuntu", Font.BOLD, 34));
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(PeersSee);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(111)
					.addComponent(lblPeersList, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
					.addGap(110))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(5)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
					.addGap(5))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(27)
					.addComponent(lblPeersList)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
					.addGap(5))
		);
		
		peerTable = new JTable();
		peerTable.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null, null},
				{null, null, null, null, null},
				{null, null, null, null, null},
				{null, null, null, null, null},
			},
			new String[] {
				"Peers ID", "IP", "Port", "Downloaded", "Uploaded"
			}
		) {
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		peerTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		peerTable.getColumnModel().getColumn(0).setMinWidth(150);
		peerTable.getColumnModel().getColumn(1).setPreferredWidth(130);
		peerTable.getColumnModel().getColumn(1).setMinWidth(130);
		peerTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		peerTable.getColumnModel().getColumn(2).setMinWidth(100);
		peerTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		peerTable.getColumnModel().getColumn(3).setMinWidth(100);
		peerTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		peerTable.getColumnModel().getColumn(4).setMinWidth(100);
		peerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		peerTable.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		scrollPane.setViewportView(peerTable);
		
		PeersSee.setLayout(groupLayout);
	}

	/**
	 * This method is called from the Model side, to provoke certain changes in the View. 
	 */
	@Override
	public void update(Observable o, Object arg) {
		if( o instanceof DataModelPeer){
			if (controller.getCurrentlyDisplayedInfoHash() != "") { // If no infohash was set, the this means that the user is not watching "Peers" tab. Then, there is no need no update anything
				DataModelPeer dmp = (DataModelPeer)o;
				SwingUtilities.invokeLater(new Runnable(){public void run(){
					int size = dmp.getPeerlist().values().size();
					String[][] arrayTable = new String[size][5];
					int l = 0;
					for (Peer pr : dmp.getPeerlist().values()) {
						arrayTable[l][0] = pr.getId()+"";
						arrayTable[l][1] = pr.getIp()+"";
						arrayTable[l][2] = pr.getPort()+"";
						arrayTable[l][3] = pr.getSwarmList().get(controller.getCurrentlyDisplayedInfoHash()).getDownloaded()+"";
						arrayTable[l][4] = pr.getSwarmList().get(controller.getCurrentlyDisplayedInfoHash()).getUploaded()+"";
					}
					peerTable.setModel(new DefaultTableModel(arrayTable, new String[] {
							"Peers ID", "IP", "Port", "Downloaded", "Uploaded"
					}));
				}});			
			}
		}		
	}
}

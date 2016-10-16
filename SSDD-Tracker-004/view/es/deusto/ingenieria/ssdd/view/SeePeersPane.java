package es.deusto.ingenieria.ssdd.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import java.util.Observable;
import java.util.Observer;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;

import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.controllers.*;

import es.deusto.ingenieria.ssdd.data.DataModelPeers;;

public class SeePeersPane extends JPanel implements Observer{

	private JPanel PeersSee;
	private static DashboardController controller;

	/**
	 * Create the application.
	 */
	public SeePeersPane() {
		initialize();
	}

	public SeePeersPane(DashboardController dc) {
		this.controller = dc;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		PeersSee = this;
		PeersSee.setMinimumSize(new Dimension(600, 480));
		PeersSee.setBounds(100, 100, 450, 300);
		//PeersSee.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
		
		JTable table = new JTable();
		table.setModel(new DefaultTableModel(
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
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(130);
		table.getColumnModel().getColumn(1).setMinWidth(130);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setMinWidth(100);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
		table.getColumnModel().getColumn(4).setMinWidth(100);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		scrollPane.setViewportView(table);
		
		PeersSee.setLayout(groupLayout);
	}

	@Override
	public void update(Observable o, Object arg) {
		if( o instanceof DataModelPeers){
			//The update is related with the value that we are observing
			if(arg == null)
			{
				//This is the peer object that provoked the notification
				Peer p = (Peer)arg;
			}
			
			
		}
		
	}
}

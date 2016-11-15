package es.deusto.ingenieria.ssdd.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.SimpleDateFormat;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.controllers.*;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;

/**
 * This class is part of the GUI. It corresponds to the inner content of one of the tabs
 * of the main window
 * @author aitor & kevin
 *
 */
public class SeeTrackersPane extends JPanel implements Observer{

	private static final long serialVersionUID = 1L;
	private JPanel TrackersSee;
	@SuppressWarnings("unused")
	private DashboardController controller;
	private JTable trackerTable;

	/**
	 * Create the application.
	 */
	public SeeTrackersPane() {
		initialize();
	}

	/**
	 * The View knows about the Controller, but no the other way around
	 * Everything the View does, it has to communicate it to the Controller
	 * @param stc
	 */
	public SeeTrackersPane(DashboardController stc) {
		this.controller = stc;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		TrackersSee = this;
		TrackersSee.setMinimumSize(new Dimension(600, 480));
		TrackersSee.setBounds(100, 100, 600, 480);
		TrackersSee.setBackground(new Color(0, 102, 153));
		
		JLabel lblSeeTrackers = new JLabel("See Trackers");
		lblSeeTrackers.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSeeTrackers.setHorizontalAlignment(SwingConstants.CENTER);
		lblSeeTrackers.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblSeeTrackers.setBorder(null);
		lblSeeTrackers.setForeground(Color.WHITE);
		lblSeeTrackers.setFont(new Font("Ubuntu", Font.BOLD, 34));

		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(TrackersSee);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(111)
					.addComponent(lblSeeTrackers, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
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
					.addComponent(lblSeeTrackers)
					.addGap(40)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
					.addGap(5))
		);
		
		trackerTable = new JTable();
		trackerTable.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null, null},
				{null, null, null, null, null},
				{null, null, null, null, null},
				{null, null, null, null, null},
			},
			new String[] {
				"ID tracker", "IP", "Port", "Master?", "Last keepalive"
			}
		) {
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		trackerTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		trackerTable.getColumnModel().getColumn(0).setMinWidth(100);
		trackerTable.getColumnModel().getColumn(1).setPreferredWidth(130);
		trackerTable.getColumnModel().getColumn(1).setMinWidth(130);
		trackerTable.getColumnModel().getColumn(2).setPreferredWidth(130);
		trackerTable.getColumnModel().getColumn(2).setMinWidth(130);
		trackerTable.getColumnModel().getColumn(3).setPreferredWidth(80);
		trackerTable.getColumnModel().getColumn(3).setMinWidth(80);
		trackerTable.getColumnModel().getColumn(4).setPreferredWidth(130);
		trackerTable.getColumnModel().getColumn(4).setMinWidth(130);
		trackerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		trackerTable.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		scrollPane.setViewportView(trackerTable);
		
		TrackersSee.setLayout(groupLayout);
	}

	/**
	 * This method is called from the Model side, to provoke certain changes in the View. 
	 */
	@Override
	public void update(Observable o, Object arg) {
		if( o instanceof DataModelTracker){
			//The update is related with the value that we are observing
			//System.out.println("Updating Tracker Table...");
			DataModelTracker dmt = (DataModelTracker)o;
			ArrayList<Tracker> trackers = new ArrayList<Tracker>(dmt.getTrackerlist().values());
			//System.out.println("Size : "+ trackers.size());
			SwingUtilities.invokeLater(new Runnable(){public void run(){
				String[][] arrayTable = new String[trackers.size()][5];
				for(int i =0; i<trackers.size(); i++){
					Tracker temp = trackers.get(i);
					arrayTable[i][0] = temp.getId()+"";
					arrayTable[i][1] = temp.getIp();
					arrayTable[i][2] = temp.getPort()+"";
					arrayTable[i][3] = (temp.getMaster())? "Yes" : "No";
					arrayTable[i][4] = new SimpleDateFormat("HH:mm:ss.S yyyy-MM-dd ").format(temp.getLastKeepAlive());
				}
				trackerTable.setModel(new DefaultTableModel(arrayTable, new String[] {
					"ID tracker", "IP", "Port", "Master?", "Last keepalive"
				}));
			}});
			
		}
		
	}
}

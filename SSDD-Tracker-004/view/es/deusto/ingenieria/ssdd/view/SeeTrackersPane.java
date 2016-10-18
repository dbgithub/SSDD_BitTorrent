package es.deusto.ingenieria.ssdd.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.LineBorder;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.controllers.*;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;

public class SeeTrackersPane extends JPanel implements Observer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel TrackersSee;
	private DashboardController controller;

	/**
	 * Create the application.
	 */
	public SeeTrackersPane() {
		initialize();
	}

	public SeeTrackersPane(DashboardController stc) {
		this.controller = stc;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		System.out.println(this.controller);
		TrackersSee = this;
		TrackersSee.setMinimumSize(new Dimension(600, 480));
		TrackersSee.setBounds(100, 100, 450, 300);
		TrackersSee.setBackground(new Color(0, 102, 153));
		
		JLabel lblSeeTrackers = new JLabel("See Trackers");
		lblSeeTrackers.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSeeTrackers.setHorizontalAlignment(SwingConstants.CENTER);
		lblSeeTrackers.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblSeeTrackers.setBorder(null);
		lblSeeTrackers.setForeground(Color.WHITE);
		lblSeeTrackers.setFont(new Font("Ubuntu", Font.BOLD, 34));
		
		JButton btnBack = new JButton("❰");
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		btnBack.setMargin(new Insets(0, 0, 0, 0));
		btnBack.setFont(new Font("Dialog", Font.BOLD, 40));
		btnBack.setHorizontalTextPosition(SwingConstants.CENTER);
		btnBack.setFocusPainted(false);
		btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnBack.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		btnBack.setBackground(Color.WHITE);
		btnBack.setForeground(Color.BLACK);
		btnBack.setVisible(false);

		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(TrackersSee);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(12)
					.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
					.addGap(29)
					.addComponent(lblSeeTrackers, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
					.addGap(110))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(5)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
					.addGap(5))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(27)
							.addComponent(lblSeeTrackers)))
					.addGap(40)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
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
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(130);
		table.getColumnModel().getColumn(1).setMinWidth(130);
		table.getColumnModel().getColumn(2).setPreferredWidth(130);
		table.getColumnModel().getColumn(2).setMinWidth(130);
		table.getColumnModel().getColumn(3).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setMinWidth(80);
		table.getColumnModel().getColumn(4).setPreferredWidth(130);
		table.getColumnModel().getColumn(4).setMinWidth(130);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		scrollPane.setViewportView(table);
		
		TrackersSee.setLayout(groupLayout);
	}

	@SuppressWarnings("null")
	@Override
	public void update(Observable o, Object arg) {
		if( o instanceof DataModelTracker){
			//The update is related with the value that we are observing
			if(arg == null)
			{
				//This is the Tracker object that provoked the notification
				Tracker t = (Tracker)arg;
				System.out.println(t.toString());
			}
		}
		
	}
}

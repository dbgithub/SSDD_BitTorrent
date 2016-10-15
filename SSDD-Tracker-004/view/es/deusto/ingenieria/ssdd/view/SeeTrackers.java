package es.deusto.ingenieria.ssdd.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.LineBorder;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import es.deusto.ingenieria.ssdd.controllers.*;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;

public class SeeTrackers implements Observer{

	private JFrame TrackersSee;
	private static SeeTrackersController controller;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SeeTrackers window = new SeeTrackers();
					window.TrackersSee.setVisible(true);
					controller.showExampleMessage(); // TO DELETE line, it's just for testing purposes
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SeeTrackers() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		TrackersSee = new JFrame();
		TrackersSee.setMinimumSize(new Dimension(600, 480));
		TrackersSee.setBounds(100, 100, 450, 300);
		TrackersSee.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		TrackersSee.getContentPane().setBackground(new Color(0, 102, 153));
		TrackersSee.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Ask for confirmation before exiting the program.
				int option = JOptionPane.showConfirmDialog(
						TrackersSee, 
						"Are you sure you want to close this tracker?",
						"Exit confirmation", 
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE);
				if (option == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
		
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
				Dashboard.show(true);
				TrackersSee.dispose();
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
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(TrackersSee.getContentPane());
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
		
		TrackersSee.getContentPane().setLayout(groupLayout);
	}

	@Override
	public void update(Observable o, Object arg) {
		if( o instanceof DataModelTracker){
			//The update is related with the value that we are observing
		}
		
	}

	/**
	 * @return the controller
	 */
	public SeeTrackersController getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public static void setController(SeeTrackersController controller) {
		SeeTrackers.controller = controller;
	}
}

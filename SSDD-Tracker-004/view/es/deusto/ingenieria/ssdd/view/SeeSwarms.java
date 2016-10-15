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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import es.deusto.ingenieria.ssdd.controllers.*;
import es.deusto.ingenieria.ssdd.data.DataModelSwarm;

public class SeeSwarms implements Observer{

	private JFrame SwarmsSee;
	private static SeeSwarmsController controller;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SeeSwarms window = new SeeSwarms();
					window.SwarmsSee.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SeeSwarms() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		SwarmsSee = new JFrame();
		SwarmsSee.setMinimumSize(new Dimension(600, 480));
		SwarmsSee.setBounds(100, 100, 450, 300);
		SwarmsSee.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		SwarmsSee.getContentPane().setBackground(new Color(0, 102, 153));
		SwarmsSee.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Ask for confirmation before exiting the program.
				int option = JOptionPane.showConfirmDialog(
						SwarmsSee, 
						"Are you sure you want to close this tracker?",
						"Exit confirmation", 
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE);
				if (option == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
		
		
		JLabel lblSeeSwarms = new JLabel("See Swarms");
		lblSeeSwarms.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSeeSwarms.setHorizontalAlignment(SwingConstants.CENTER);
		lblSeeSwarms.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblSeeSwarms.setBorder(null);
		lblSeeSwarms.setForeground(Color.WHITE);
		lblSeeSwarms.setFont(new Font("Ubuntu", Font.BOLD, 34));
		
		JButton btnBack = new JButton("❰");
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Dashboard.show(true);
				SwarmsSee.dispose();
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
		GroupLayout groupLayout = new GroupLayout(SwarmsSee.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(12)
					.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
					.addGap(29)
					.addComponent(lblSeeSwarms, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
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
							.addComponent(lblSeeSwarms)))
					.addGap(40)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
					.addGap(5))
		);
		
		JTable table = new JTable();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable tempTable = (JTable)e.getSource();
				    System.out.println("double clicked in row#" + (tempTable.rowAtPoint(e.getPoint()) + 1));
				    SeePeers.main(null);
				    SeePeersController.main(null);  
				    controller.showExampleMessage(); // TO DELETE line, it's just for testing purposes
				  }
			}
		});
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{"example", "example", "example", "example"},
				{null, null, null, null},
				{null, null, null, null},
				{null, null, null, null},
			},
			new String[] {
				"Swarm content", "Size", "Total seeders", "Total leechers"
			}
		) {
			boolean[] columnEditables = new boolean[] {
				false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setMinWidth(100);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		scrollPane.setViewportView(table);
		
		SwarmsSee.getContentPane().setLayout(groupLayout);
	}

	@Override
	public void update(Observable o, Object arg) {
		if( o instanceof DataModelSwarm){
			//The update is related with the value that we are observing
		}
		
	}

	/**
	 * @return the controller
	 */
	public SeeSwarmsController getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public static void setController(SeeSwarmsController controller) {
		SeeSwarms.controller = controller;
	}
}

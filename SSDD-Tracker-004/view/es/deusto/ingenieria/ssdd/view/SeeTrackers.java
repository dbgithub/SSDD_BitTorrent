package es.deusto.ingenieria.ssdd.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.LineBorder;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import java.awt.Rectangle;
import java.util.Observable;
import java.util.Observer;

import javax.swing.border.EmptyBorder;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

//import es.deusto.ingenieria.ssdd.data.DataModel;

public class SeeTrackers implements Observer{

	private JFrame TrackerConfiguration;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SeeTrackers window = new SeeTrackers();
					window.TrackerConfiguration.setVisible(true);
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
	private void initialize() {
		TrackerConfiguration = new JFrame();
		TrackerConfiguration.setMinimumSize(new Dimension(600, 480));
		TrackerConfiguration.setBounds(100, 100, 450, 300);
		TrackerConfiguration.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		TrackerConfiguration.getContentPane().setBackground(new Color(0, 102, 153));
		TrackerConfiguration.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblTrackerConfiguring = new JLabel("See Trackers");
		lblTrackerConfiguring.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTrackerConfiguring.setHorizontalAlignment(SwingConstants.CENTER);
		lblTrackerConfiguring.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblTrackerConfiguring.setBorder(null);
		lblTrackerConfiguring.setForeground(Color.WHITE);
		lblTrackerConfiguring.setFont(new Font("Ubuntu", Font.BOLD, 34));
		
		JButton btnBack = new JButton("❰");
		btnBack.setMargin(new Insets(0, 0, 0, 0));
		btnBack.setFont(new Font("Dialog", Font.BOLD, 40));
		btnBack.setHorizontalTextPosition(SwingConstants.CENTER);
		btnBack.setFocusPainted(false);
		btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnBack.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		btnBack.setBackground(Color.WHITE);
		btnBack.setForeground(Color.BLACK);
		GroupLayout groupLayout = new GroupLayout(TrackerConfiguration.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(12)
					.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
					.addGap(29)
					.addComponent(lblTrackerConfiguring, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
					.addGap(110))
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
							.addComponent(lblTrackerConfiguring)))
					.addContainerGap(398, Short.MAX_VALUE))
		);
		TrackerConfiguration.getContentPane().setLayout(groupLayout);
	}

	@Override
	public void update(Observable o, Object arg) {
//		if( o instanceof DataModel){
//			//The update is related with the value that we are observing
//		}
		
	}
}

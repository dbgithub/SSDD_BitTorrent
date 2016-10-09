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
import javax.swing.border.EmptyBorder;

public class Dashboard {

	private JFrame TrackerDashboard;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Dashboard window = new Dashboard();
					window.TrackerDashboard.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Dashboard() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		TrackerDashboard = new JFrame();
		TrackerDashboard.setMinimumSize(new Dimension(600, 480));
		TrackerDashboard.setBounds(100, 100, 450, 300);
		TrackerDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		TrackerDashboard.getContentPane().setBackground(new Color(0, 102, 153));
		TrackerDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblTrackerDashboard = new JLabel("Tracker dashboard");
		lblTrackerDashboard.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTrackerDashboard.setHorizontalAlignment(SwingConstants.CENTER);
		lblTrackerDashboard.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblTrackerDashboard.setBorder(new LineBorder(Color.WHITE, 3, true));
		lblTrackerDashboard.setForeground(Color.WHITE);
		lblTrackerDashboard.setFont(new Font("Ubuntu", Font.BOLD, 34));
		
		JLabel lblWhatDoYou = new JLabel("What do you want to do?");
		lblWhatDoYou.setHorizontalTextPosition(SwingConstants.CENTER);
		lblWhatDoYou.setHorizontalAlignment(SwingConstants.CENTER);
		lblWhatDoYou.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblWhatDoYou.setBorder(null);
		lblWhatDoYou.setForeground(Color.WHITE);
		lblWhatDoYou.setFont(new Font("Ubuntu", Font.BOLD, 24));
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setOpaque(false);
		panel.setBorder(null);
		GroupLayout groupLayout = new GroupLayout(TrackerDashboard.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(62)
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
					.addGap(62))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(127)
					.addComponent(lblTrackerDashboard, GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
					.addGap(127))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(155)
					.addComponent(lblWhatDoYou, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(155))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(36)
					.addComponent(lblTrackerDashboard)
					.addGap(18)
					.addComponent(lblWhatDoYou)
					.addGap(55)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(206, Short.MAX_VALUE))
		);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{158, 0};
		gbl_panel.rowHeights = new int[]{55, 55, 55, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnConfigureTracker = new JButton("Configure tracker");
		btnConfigureTracker.setFocusPainted(false);
		btnConfigureTracker.setMargin(new Insets(0, 14, 0, 14));
		btnConfigureTracker.setBorder(null);
		btnConfigureTracker.setBackground(new Color(204, 51, 0));
		btnConfigureTracker.setForeground(Color.WHITE);
		btnConfigureTracker.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		btnConfigureTracker.setHorizontalTextPosition(SwingConstants.CENTER);
		btnConfigureTracker.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnConfigureTracker = new GridBagConstraints();
		gbc_btnConfigureTracker.fill = GridBagConstraints.BOTH;
		gbc_btnConfigureTracker.insets = new Insets(0, 0, 5, 0);
		gbc_btnConfigureTracker.gridx = 0;
		gbc_btnConfigureTracker.gridy = 0;
		panel.add(btnConfigureTracker, gbc_btnConfigureTracker);
		
		JButton btnSeeTrackers = new JButton("See trackers");
		btnSeeTrackers.setFocusPainted(false);
		btnSeeTrackers.setMargin(new Insets(0, 14, 0, 14));
		btnSeeTrackers.setBorder(null);
		btnSeeTrackers.setForeground(Color.WHITE);
		btnSeeTrackers.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		btnSeeTrackers.setBackground(new Color(255, 153, 0));
		btnSeeTrackers.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnSeeTrackers = new GridBagConstraints();
		gbc_btnSeeTrackers.fill = GridBagConstraints.BOTH;
		gbc_btnSeeTrackers.insets = new Insets(0, 0, 5, 0);
		gbc_btnSeeTrackers.gridx = 0;
		gbc_btnSeeTrackers.gridy = 1;
		panel.add(btnSeeTrackers, gbc_btnSeeTrackers);
		
		JButton btnSeeSwarms = new JButton("See swarms");
		btnSeeSwarms.setFocusPainted(false);
		btnSeeSwarms.setMargin(new Insets(0, 14, 0, 14));
		btnSeeSwarms.setBorder(null);
		btnSeeSwarms.setForeground(Color.WHITE);
		btnSeeSwarms.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		btnSeeSwarms.setBackground(new Color(50, 205, 50));
		btnSeeSwarms.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnSeeSwarms = new GridBagConstraints();
		gbc_btnSeeSwarms.fill = GridBagConstraints.BOTH;
		gbc_btnSeeSwarms.gridx = 0;
		gbc_btnSeeSwarms.gridy = 2;
		panel.add(btnSeeSwarms, gbc_btnSeeSwarms);
		TrackerDashboard.getContentPane().setLayout(groupLayout);
	}

}

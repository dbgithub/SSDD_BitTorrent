package es.deusto.ingenieria.ssdd.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.LineBorder;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import java.awt.Rectangle;
import java.util.Observable;
import java.util.Observer;

import javax.swing.border.EmptyBorder;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import javax.swing.JRadioButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ConfigurationTracker implements Observer{

	private JFrame TrackerConfiguration;
	private JTextField txtIP;
	private JTextField txtPort;
	private JTextField txtId;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigurationTracker window = new ConfigurationTracker();
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
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
	public ConfigurationTracker() {
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
		
		JLabel lblTrackerConfiguring = new JLabel("Configuring Tracker");
		lblTrackerConfiguring.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTrackerConfiguring.setHorizontalAlignment(SwingConstants.CENTER);
		lblTrackerConfiguring.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblTrackerConfiguring.setBorder(null);
		lblTrackerConfiguring.setForeground(Color.WHITE);
		lblTrackerConfiguring.setFont(new Font("Ubuntu", Font.BOLD, 34));
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setOpaque(false);
		panel.setBorder(null);
		
		JButton btnBack = new JButton("❰");
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Dashboard.show(true);
				TrackerConfiguration.dispose();
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
		
		JButton btnTestFailure = new JButton("TEST CONNECTION FAILURE");
		btnTestFailure.setBorder(new MatteBorder(3, 3, 3, 3, (Color) new Color(0, 0, 0)));
		btnTestFailure.setBackground(Color.RED);
		btnTestFailure.setForeground(Color.WHITE);
		btnTestFailure.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		
		JButton btnStartStop = new JButton("Start / Stop");
		btnStartStop.setBackground(new Color(50, 205, 50));
		btnStartStop.setForeground(Color.WHITE);
		btnStartStop.setFont(new Font("Noto Sans CJK JP Regular", Font.BOLD, 30));
		GroupLayout groupLayout = new GroupLayout(TrackerConfiguration.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(12)
					.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
					.addGap(29)
					.addComponent(lblTrackerConfiguring, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
					.addGap(110))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnTestFailure)
					.addContainerGap(367, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(62)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
						.addComponent(btnStartStop, GroupLayout.PREFERRED_SIZE, 222, GroupLayout.PREFERRED_SIZE))
					.addGap(62))
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
					.addGap(31)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE)
					.addGap(72)
					.addComponent(btnStartStop, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
					.addComponent(btnTestFailure)
					.addContainerGap())
		);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{158, 0, 0};
		gbl_panel.rowHeights = new int[]{55, 55, 55, 55, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblIp = new JLabel("IP");
		lblIp.setForeground(Color.WHITE);
		lblIp.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		GridBagConstraints gbc_lblIp = new GridBagConstraints();
		gbc_lblIp.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblIp.insets = new Insets(0, 0, 5, 5);
		gbc_lblIp.gridx = 0;
		gbc_lblIp.gridy = 0;
		panel.add(lblIp, gbc_lblIp);
		
		txtIP = new JTextField();
		txtIP.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		GridBagConstraints gbc_txtIP = new GridBagConstraints();
		gbc_txtIP.insets = new Insets(0, 0, 5, 0);
		gbc_txtIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtIP.gridx = 1;
		gbc_txtIP.gridy = 0;
		panel.add(txtIP, gbc_txtIP);
		txtIP.setColumns(10);
		
		JLabel lblPort = new JLabel("Port");
		lblPort.setForeground(Color.WHITE);
		lblPort.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblPort.gridx = 0;
		gbc_lblPort.gridy = 1;
		panel.add(lblPort, gbc_lblPort);
		
		txtPort = new JTextField();
		txtPort.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		GridBagConstraints gbc_txtPort = new GridBagConstraints();
		gbc_txtPort.insets = new Insets(0, 0, 5, 0);
		gbc_txtPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPort.gridx = 1;
		gbc_txtPort.gridy = 1;
		panel.add(txtPort, gbc_txtPort);
		txtPort.setColumns(10);
		
		JLabel lblID = new JLabel("ID");
		lblID.setForeground(Color.WHITE);
		lblID.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		GridBagConstraints gbc_lblID = new GridBagConstraints();
		gbc_lblID.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblID.insets = new Insets(0, 0, 5, 5);
		gbc_lblID.gridx = 0;
		gbc_lblID.gridy = 2;
		panel.add(lblID, gbc_lblID);
		
		txtId = new JTextField();
		txtId.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		GridBagConstraints gbc_txtId = new GridBagConstraints();
		gbc_txtId.insets = new Insets(0, 0, 5, 0);
		gbc_txtId.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtId.gridx = 1;
		gbc_txtId.gridy = 2;
		panel.add(txtId, gbc_txtId);
		txtId.setColumns(10);
		
		JLabel lblMaster = new JLabel("Is master?");
		lblMaster.setForeground(Color.WHITE);
		lblMaster.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		GridBagConstraints gbc_lblMaster = new GridBagConstraints();
		gbc_lblMaster.anchor = GridBagConstraints.WEST;
		gbc_lblMaster.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaster.gridx = 0;
		gbc_lblMaster.gridy = 3;
		panel.add(lblMaster, gbc_lblMaster);
		
		JPanel panel_radiobtns = new JPanel();
		GridBagConstraints gbc_panel_radiobtns = new GridBagConstraints();
		gbc_panel_radiobtns.fill = GridBagConstraints.BOTH;
		gbc_panel_radiobtns.gridx = 1;
		gbc_panel_radiobtns.gridy = 3;
		panel.add(panel_radiobtns, gbc_panel_radiobtns);
		
		JRadioButton rdbtnYes = new JRadioButton("Yes");
		panel_radiobtns.add(rdbtnYes);
		JRadioButton rdbtnNo = new JRadioButton("No");
		panel_radiobtns.add(rdbtnNo);
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnYes);
		group.add(rdbtnNo);
		
		TrackerConfiguration.getContentPane().setLayout(groupLayout);
	}

	@Override
	public void update(Observable o, Object arg) {
		if( o instanceof DataModelConfiguration){
			//The update is related with the value that we are observing
		}
		
	}
}

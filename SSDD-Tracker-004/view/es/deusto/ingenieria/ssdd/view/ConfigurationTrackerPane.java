package es.deusto.ingenieria.ssdd.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
import java.awt.Dimension;
import javax.swing.SwingConstants;

import java.util.Observable;
import java.util.Observer;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import javax.swing.JRadioButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import es.deusto.ingenieria.ssdd.controllers.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * This class is part of the GUI. It corresponds to the inner content of one of the tabs
 * of the main window
 * @author aitor & kevin
 *
 */
public class ConfigurationTrackerPane extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;
	private JPanel TrackerConfiguration;
	private JTextField txtIP;
	private JTextField txtPort;
	private JTextField txtId;
	private JRadioButton rdbtnYes;
	private JRadioButton rdbtnNo;
	private JButton btnStartStop;
	private JButton btnIncomingPeer;
	private DashboardController controller;
	private boolean workingOnIt = false;
	private boolean threadSetUpFinished = false;
	//Boolean to specify if a slave is prepared to receive an update ¿What evidence do we use to determine this behavior?
	@SuppressWarnings("unused")
	private boolean availabilityToReceiveUpdates = true;

	/**
	 * Create the application.
	 */
	public ConfigurationTrackerPane() {
		initialize();
	}

	/**
	 * The View knows about the Controller, but no the other way around
	 * Everything the View does, it has to communicate it to the Controller
	 * @param dashboardController
	 */
	public ConfigurationTrackerPane(DashboardController dashboardController) {
		this.controller = dashboardController;
		initialize();
		this.TrackerConfiguration.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		TrackerConfiguration = this;
		TrackerConfiguration.setMinimumSize(new Dimension(600, 480));
		TrackerConfiguration.setBounds(100, 100, 600, 480);
		TrackerConfiguration.setBackground(new Color(0, 102, 153));
		
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

		
		JButton btnTestFailure = new JButton("TEST CONNECTION FAILURE");
		btnTestFailure.setFocusPainted(false);
		btnTestFailure.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controller.testFailure();
				setStartState();
			}
		});
		btnTestFailure.setBorder(new MatteBorder(3, 3, 3, 3, (Color) new Color(0, 0, 0)));
		btnTestFailure.setBackground(Color.RED);
		btnTestFailure.setForeground(Color.WHITE);
		btnTestFailure.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		
		btnStartStop = new JButton("Start");
		btnStartStop.setFocusPainted(false);
		btnStartStop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!workingOnIt) {
					controller.startStopFunction(txtIP.getText(), txtId.getText(), txtPort.getText(), true);
					btnStartStop.setBackground(new Color(128,0,0));
					btnStartStop.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 14));
					btnStartStop.setText("setup in progress....");	
					workingOnIt = true;
					threadSetUpFinished = false;
				} else {
					if(threadSetUpFinished){
						controller.interruptAllThreads();
						resetStartStopBtnState();
						threadSetUpFinished = false;
						workingOnIt = false;
					}
					else{
						resetStartStopBtnState();
						threadSetUpFinished = false;
						workingOnIt = false;
					}
					
				}
			}
		});
		btnStartStop.setBackground(new Color(50, 205, 50));
		btnStartStop.setForeground(Color.WHITE);
		btnStartStop.setFont(new Font("Noto Sans CJK JP Regular", Font.BOLD, 30));
		
		JLabel lblfootnote = new JLabel("(*) Read-only values");
		lblfootnote.setForeground(Color.WHITE);
		lblfootnote.setFont(new Font("Symbola", Font.BOLD, 12));
		
		btnIncomingPeer = new JButton("SIMULATE NEW INCOMING PEER");
		btnIncomingPeer.setEnabled(false);
		btnIncomingPeer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(ConfigurationTrackerPane.this, "Hey! There is no need to simulate an incoming peer message anymore.\n" + 
						"Please, launch the client side to test the communication between the cluster of trackers and peers", "Not used anymore!", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		btnIncomingPeer.setForeground(Color.WHITE);
		btnIncomingPeer.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 16));
		btnIncomingPeer.setFocusPainted(false);
		btnIncomingPeer.setBorder(new MatteBorder(3, 3, 3, 3, (Color) new Color(0, 0, 0)));
		btnIncomingPeer.setBackground(new Color(153, 51, 204));
		GroupLayout groupLayout = new GroupLayout(TrackerConfiguration);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(111)
					.addComponent(lblTrackerConfiguring, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
					.addGap(110))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnTestFailure)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnIncomingPeer, GroupLayout.PREFERRED_SIZE, 265, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(90, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(62)
							.addComponent(panel, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(12)
									.addComponent(lblfootnote, GroupLayout.PREFERRED_SIZE, 194, GroupLayout.PREFERRED_SIZE))
								.addComponent(btnStartStop, GroupLayout.PREFERRED_SIZE, 222, GroupLayout.PREFERRED_SIZE))))
					.addGap(62))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(27)
					.addComponent(lblTrackerConfiguring)
					.addGap(31)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE)
					.addGap(43)
					.addComponent(btnStartStop, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblfootnote)
					.addPreferredGap(ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnTestFailure)
						.addComponent(btnIncomingPeer, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE))
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
		txtIP.setText("228.5.6.7");
		txtIP.setFont(new Font("Noto Sans CJK JP Regular", Font.BOLD, 16));
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
		txtPort.setText("9000");
		txtPort.setFont(new Font("Noto Sans CJK JP Regular", Font.BOLD, 16));
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
		txtId.setText("1");
		txtId.setFont(new Font("Noto Sans CJK JP Regular", Font.BOLD, 16));
		GridBagConstraints gbc_txtId = new GridBagConstraints();
		gbc_txtId.insets = new Insets(0, 0, 5, 0);
		gbc_txtId.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtId.gridx = 1;
		gbc_txtId.gridy = 2;
		panel.add(txtId, gbc_txtId);
		txtId.setColumns(10);
		
		JLabel lblMaster = new JLabel("Is master?*");
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
		
		rdbtnYes = new JRadioButton("Yes");
		rdbtnYes.setEnabled(false);
		panel_radiobtns.add(rdbtnYes);
		rdbtnNo = new JRadioButton("No");
		rdbtnNo.setEnabled(false);
		panel_radiobtns.add(rdbtnNo);
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnYes);
		group.add(rdbtnNo);
		
		TrackerConfiguration.setLayout(groupLayout);
	}

	private void resetStartStopBtnState() {
		controller.startStopFunction(null, null, null, false);
		setStartState();
	}
	
	private void setStopState() {
		btnStartStop.setBackground(new Color(255, 0, 0));
		btnStartStop.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 30));
		btnStartStop.setText("Stop");
		//btnIncomingPeer.setEnabled(true);
	}
	
	private void setStartState() {
		btnStartStop.setBackground(new Color(50, 205, 50));
		btnStartStop.setFont(new Font("Noto Sans CJK JP Regular", Font.PLAIN, 30));
		btnStartStop.setText("Start");
	}
	
	/**
	 * This method is called from the Model side, to provoke certain changes in the View. 
	 */
	@Override
	public void update(Observable o, Object arg) {
		if( o instanceof DataModelConfiguration){
			//The update is related with the value that we are observing
			DataModelConfiguration dmc = (DataModelConfiguration)o;
			txtId.setText(dmc.getId());
			txtIP.setText(dmc.getIp());
			txtPort.setText(dmc.getPort()+"");
			if (dmc.isMaster()) {rdbtnYes.setSelected(true); btnIncomingPeer.setEnabled(true);}  else {rdbtnNo.setSelected(true); btnIncomingPeer.setEnabled(false);};
			if (dmc.isTrackerSetUpFinished()) {
				threadSetUpFinished = true; 
				setStopState();
			}
			availabilityToReceiveUpdates = dmc.isAvailableToReceiveUpdates();
		}
		
	}
}

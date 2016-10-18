package es.deusto.ingenieria.ssdd.controllers;

import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelPeer;
import es.deusto.ingenieria.ssdd.data.DataModelSwarm;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.view.ConfigurationTrackerPane;
import es.deusto.ingenieria.ssdd.view.SeePeersPane;
import es.deusto.ingenieria.ssdd.view.SeeSwarmsPane;
import es.deusto.ingenieria.ssdd.view.SeeTrackersPane;
import es.deusto.ingenieria.ssdd.view.mainFrame;

public class DashboardController {
	
	private DataModelConfiguration dmc;
	private DataModelSwarm dms;
	private DataModelTracker dmt;
	private DataModelPeer dmp;
	
	public DashboardController() {
		dmc = new DataModelConfiguration();
		dmt = new DataModelTracker();
		dms = new DataModelSwarm();
		dmp = new DataModelPeer();
	}
	
	/**
	 * Method that displays a Example Message, just to asure that the connection between the controller and the view is correct.
	 */
	public void showExampleMessage() {
		System.out.println("This message is called by Dashboard and executed in DashboardController");
	}
	
	/**
	 * Method to add an observer to the DataModelConfiguration
	 * @param ctp ConfigurationTrackerPane related with the DataModel
	 */
	public void setConfigurationObserver(ConfigurationTrackerPane ctp) {
		dmc.addObserver(ctp);
	}
	
	/**
	 * Method to add an observer to the DataModelTracker
	 * @param ctp SeeTrackersPane related with the DataModel
	 */
	public void setTrackerObserver(SeeTrackersPane stp) {
		dmt.addObserver(stp);
	}
	
	/**
	 * Method to add an observer to the DataModelSwarm
	 * @param ctp SeeSwarmsPane related with the DataModel
	 */
	public void setSwarmObserver(SeeSwarmsPane ssp) {
		dms.addObserver(ssp);
	}
	
	/**
	 * Method to add an observer to the DataModelPeer
	 * @param ctp SeePeersPane related with the DataModel
	 */
	public void setPeersObserver(SeePeersPane spp) {
		dmp.addObserver(spp);
	}
	
	public static void main(String[]args)
	{
		DashboardController dc = new DashboardController();
		new mainFrame(dc);
	}

	/**
	 * Method to display a test failure error. In next deliverables will be implemented correctly.
	 * 
	 */
	public void testFailure() {
		System.out.println("Test Failure Button");
	}

	/**
	 * Method triggered when pushed the Start and Stop method, simulating a change at the model
	 * and showing Observer functionality
	 */
	public void startStopFunction() {
		System.out.println("Start/Stop Presset");
		//This additional code is a prove that observer is working
		dmc.setId("ID 1");
		dmc.setIp("127.0.0.1");
		dmc.setPort(8080);
		dmc.setMaster(true);
		
	}
	/**
	 * Provisional method to populate with example data some of the tables
	 */
	public void populateWithExampleData()
	{
		
	}
}

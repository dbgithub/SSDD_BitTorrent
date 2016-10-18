package es.deusto.ingenieria.ssdd.controllers;

import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelPeers;
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
	private DataModelPeers dmp;
	
	public DashboardController() {
		dmc = new DataModelConfiguration();
		dmt = new DataModelTracker();
		dms = new DataModelSwarm();
		dmp = new DataModelPeers();
	}
	
	public void showExampleMessage() {
		System.out.println("This message is called by Dashboard and executed in DashboardController");
	}
	public void setConfigurationObserver(ConfigurationTrackerPane ctp) {
		dmc.addObserver(ctp);
	}
	
	public void setTrackerObserver(SeeTrackersPane stp) {
		dmt.addObserver(stp);
	}
	
	public void setSwarmObserver(SeeSwarmsPane ssp) {
		dms.addObserver(ssp);
	}
	
	public void setPeersObserver(SeePeersPane spp) {
		dmp.addObserver(spp);
	}
	
	public static void main(String[]args)
	{
		DashboardController dc = new DashboardController();
		new mainFrame(dc);
	}

	public void testFailure() {
		System.out.println("Test Failure Button");
	}

	public void startStopFunction() {
		System.out.println("Start/Stop Presset");
		//This additional code is a prove that observer is working
		dmc.setId("ID 1");
		dmc.setIp("127.0.0.1");
		dmc.setPort(8080);
		dmc.setMaster(true);
		
	}
	
	public void populateWithExampleData()
	{
		
	}
}

package es.deusto.ingenieria.ssdd.controllers;

import java.util.HashMap;

import es.deusto.ingenieria.ssdd.classes.Tracker;
import es.deusto.ingenieria.ssdd.data.DataModelConfiguration;
import es.deusto.ingenieria.ssdd.data.DataModelPeer;
import es.deusto.ingenieria.ssdd.data.DataModelSwarm;
import es.deusto.ingenieria.ssdd.data.DataModelTracker;
import es.deusto.ingenieria.ssdd.view.ConfigurationTrackerPane;
import es.deusto.ingenieria.ssdd.view.SeePeersPane;
import es.deusto.ingenieria.ssdd.view.SeeSwarmsPane;
import es.deusto.ingenieria.ssdd.view.SeeTrackersPane;

/**
 * This is the Controller for the application. Everything has to go through this class theoretically.
 * Within this class, the DataModels corresponding to each and every View class are created.
 * @author aitor & kevin
 *
 */
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
	
	/**
	 * Method to display a test failure error. In next deliverables will be implemented correctly.
	 * 
	 */
	public void testFailure() {
		System.out.println("Test-Failure-Button pressed!");
		dmt.stopEntranceStep();
		this.interruptAllThreads();
	}

	/**
	 * Method triggered when pushed the Start and Stop method, simulating a change at the model
	 * and showing Observer functionality
	 * @param ip 
	 * @param id 
	 * @param string 
	 * @param start
	 */
	public void startStopFunction(String ip, String id, String port, boolean start) {
		if (start) {
			System.out.println("Starting entrance-and-keepalive process...");
			// This code will change in the future in accordance with the data inputed by the user.
			dmc.setId(id);
			dmc.setIp(ip);
			dmc.setPort(Integer.parseInt(port));
			dmt.startEntranceStep(dmc, dms);
		} else {
			System.out.println("Stoping entrance-and-keepalive process...");
			dmt.stopEntranceStep();
		}	
	}
	
	public void interruptAllThreads() {
		dmt.keepaliveChecker.cancel();
		dmt.keepaliveSender.cancel();
		dmt.setTrackerlist(new HashMap<Integer, Tracker>());
		dmc.destroyDataRepository();
	}
	
	public void sendRepositoryUpdateRequestMessage() {
		dmt.sendRepositoryUpdateRequestMessage();
	}
}

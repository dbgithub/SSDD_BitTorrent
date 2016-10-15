package es.deusto.ingenieria.ssdd.controllers;

import es.deusto.ingenieria.ssdd.view.ConfigurationTracker;

public class ConfigurationTrackerController {

	public ConfigurationTrackerController() {
		ConfigurationTracker.setController(this);
	}
	public static void main(String[] args) {
		new ConfigurationTrackerController();
	}
	public void showExampleMessage() {
		System.out.println("This message is called by ConfigurationTracker and executed in ConfigurationTrackerController");
	}
}

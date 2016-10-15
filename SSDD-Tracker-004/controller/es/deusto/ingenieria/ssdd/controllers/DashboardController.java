package es.deusto.ingenieria.ssdd.controllers;

import es.deusto.ingenieria.ssdd.view.Dashboard;

public class DashboardController {
	
	public DashboardController() {
		Dashboard.setController(this);
		Dashboard.main(null);
	}
	public static void main(String[] args) {
		new DashboardController();
	}
	
	public void showExampleMessage() {
		System.out.println("This message is called by Dashboard and executed in DashboardController");
	}
}

package es.deusto.ingenieria.ssdd.controllers;

import es.deusto.ingenieria.ssdd.view.SeeTrackers;

public class SeeTrackersController {
	public SeeTrackersController () {
		SeeTrackers.setController(this);
	}
	public static void main(String[] args) {
		new SeeTrackersController();
	}
	
	public void showExampleMessage() {
		System.out.println("This message is called by SeeTrackers and executed in SeeTrackersController");
	}
}

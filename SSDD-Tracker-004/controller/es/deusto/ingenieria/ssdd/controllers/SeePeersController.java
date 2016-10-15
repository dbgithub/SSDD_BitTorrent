package es.deusto.ingenieria.ssdd.controllers;

import es.deusto.ingenieria.ssdd.view.SeePeers;

public class SeePeersController {
	public SeePeersController () {
		SeePeers.setController(this);
	}
	public static void main(String[] args) {
		new SeePeersController();
	}
	
	public void showExampleMessage() {
		System.out.println("This message is called by SeePeers and executed in SeePeersController");
	}
}

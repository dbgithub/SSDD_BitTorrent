package es.deusto.ingenieria.ssdd.controllers;

import es.deusto.ingenieria.ssdd.view.SeeSwarms;

public class SeeSwarmsController {
	public SeeSwarmsController () {
		SeeSwarms.setController(this);
	}
	public static void main(String[] args) {
		new SeeSwarmsController();
	}
	
	public void showExampleMessage() {
		System.out.println("This message is called by SeeSwarms and executed in SeeSwarmsController");
	}
}

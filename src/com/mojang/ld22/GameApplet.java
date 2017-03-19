package com.mojang.ld22;

import java.applet.Applet;
import java.awt.BorderLayout;

public class GameApplet extends Applet {
	private static final long serialVersionUID = 1L; //for eclipse
	
	/* This class is called when the game is loaded in a website (like playminicraft.com) */
	
	private Game game = new Game(); // Creates a new Game object.
	
	/** Initialization step, called when the applet is started */
	public void init() {
		setLayout(new BorderLayout()); // creates a layout for the game to use.
		add(game, BorderLayout.CENTER); // Adds the game (since it's a canvas) to the center of the applet.
	}
	
	/** starts the game on the applet */
	public void start() {
		game.start();
	}
	
	/** stops the game on the applet */
	public void stop() {
		game.stop();
	}
}

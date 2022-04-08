package minicraft.core;

import java.awt.GraphicsEnvironment;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jetbrains.annotations.Nullable;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.network.Analytics;
import minicraft.network.MinicraftProtocol;
import minicraft.saveload.Load;
import minicraft.saveload.Version;
import minicraft.screen.Display;
import minicraft.screen.TitleDisplay;
import org.tinylog.Logger;

public class Game {
	Game() {} // Can't instantiate the Game class.
	
	public static boolean debug = false;
	public static boolean packet_debug = false;
	
	public static final String NAME = "Minicraft Plus"; // This is the name on the application window.
	public static final Version VERSION = new Version("2.1.0-dev2");
	
	public static InputHandler input; // Input used in Game, Player, and just about all the *Menu classes.
	public static Player player;
	
	public static List<String> notifications = new ArrayList<>();

	public static int MAX_FPS = (int) Settings.get("fps");

	/**
	 * This specifies a custom port instead of default to server-side using
	 * --port parameter if something goes wrong in setting the new port
	 * it'll use the default one {@link MinicraftProtocol#PORT}
	 */
	public static int CUSTOM_PORT = MinicraftProtocol.PORT;

	static Display menu = null, newMenu = null; // The current menu you are on.

	// Sets the current menu.
	public static void setMenu(@Nullable Display display) { newMenu = display; }
	public static void exitMenu() {
		if (menu == null) {
			Logger.debug("No menu found, returning!");
			return; // No action required; cannot exit from no menu
		}
		Sound.back.play();
		newMenu = menu.getParent();
	}
	public static Display getMenu() { return newMenu; }

	public static boolean isMode(String mode) { return ((String)Settings.get("mode")).equalsIgnoreCase(mode); }

	// LEVEL
	public static Level[] levels = new Level[6]; // This array stores the different levels.
	public static int currentLevel = 3; // This is the level the player is on. It defaults to 3, the surface.

	// GAME
	public static String gameDir; // The directory in which all the game files are stored
	static boolean gameOver = false; // If the player wins this is set to true.

	static boolean running = true;
	public static void quit() {
		running = false;
	}


	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			throwable.printStackTrace();

			StringWriter string = new StringWriter();
			PrintWriter printer = new PrintWriter(string);
			throwable.printStackTrace(printer);
			
			Future ping = Analytics.Crashes.ping();
			
			if(GraphicsEnvironment.isHeadless()) {
				// ensure ping finishes before program closes
				try {
					ping.get();
				} catch (Exception ignored) {}
				return;
			}
			
			JTextArea errorDisplay = new JTextArea(string.toString());
			errorDisplay.setEditable(false);
			JScrollPane errorPane = new JScrollPane(errorDisplay);
			JOptionPane.showMessageDialog(null, errorPane, "An error has occurred", JOptionPane.ERROR_MESSAGE);
			
			// Ensure ping finishes before program closes
			try {
				ping.get();
			} catch (Exception ignored) {}
		});
		
		Analytics.GameStartup.ping();

		Initializer.parseArgs(args); // Parses the command line arguments
		
		input = new InputHandler(Renderer.canvas);
		
		Tiles.initTileList();
		Sound.init();

		World.resetGame(); // "half"-starts a new game, to set up initial variables
		player.eid = 0;
		new Load(true); // This loads any saved preferences.


		setMenu(new TitleDisplay()); // Sets menu to the title screen.


		Initializer.createAndDisplayFrame();
		
		Renderer.initScreen();

		// Update fullscreen frame if Updater.FULLSCREEN was updated previously
		if (Updater.FULLSCREEN) {
			Updater.updateFullscreen();
		}
		Initializer.run();
		
		Logger.debug("Main game loop ended; Terminating application...");
		System.exit(0);
	}
}

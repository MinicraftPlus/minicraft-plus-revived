package minicraft.core;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.network.MinicraftClient;
import minicraft.network.MinicraftProtocol;
import minicraft.network.MinicraftServer;
import minicraft.saveload.Load;
import minicraft.saveload.Version;
import minicraft.screen.Display;
import minicraft.screen.MultiplayerDisplay;
import minicraft.screen.TitleDisplay;

import org.jetbrains.annotations.Nullable;

public class Game {
	Game() {} // can't instantiate the Game class.
	
	public static boolean debug = false;
	public static boolean packet_debug = false;
	public static boolean HAS_GUI = true;
	
	public static final String NAME = "Minicraft Plus"; // This is the name on the application window.
	public static final Version VERSION = new Version("2.0.7-dev3");
	
	public static InputHandler input; // input used in Game, Player, and just about all the *Menu classes.
	public static Player player;
	
	public static String gameDir; // The directory in which all the game files are stored
	public static List<String> notifications = new ArrayList<>();
	
	public static int MAX_FPS = (int) Settings.get("fps");

	/**
	 * This specifies a custom port instead of default to server-side using
	 * --port parameter if something goes wrong in setting the new port
	 * it'll use the default one {@link MinicraftProtocol#PORT}
	 */
	public static int CUSTOM_PORT = MinicraftProtocol.PORT;
	
	static Display menu = null, newMenu = null; // the current menu you are on.
	// Sets the current menu.
	public static void setMenu(@Nullable Display display) { newMenu = display; }
	public static void exitMenu() {
		if (menu == null) {
			if (debug) System.out.println("Game.exitMenu(): No menu found, returning!");
			return; // no action required; cannot exit from no menu
		}
		Sound.back.play();
		newMenu = menu.getParent();
	}
	public static Display getMenu() { return newMenu; }
	
	public static boolean isMode(String mode) { return ((String)Settings.get("mode")).equalsIgnoreCase(mode); }
	
	
	// MULTIPLAYER
	
	public static boolean ISONLINE = false;
	public static boolean ISHOST = false;
	
	public static MinicraftClient client = null;
	public static boolean isValidClient() { return ISONLINE && client != null; }
	public static boolean isConnectedClient() { return isValidClient() && client.isConnected(); }
	
	public static MinicraftServer server = null;

	/** Checks if you are a host and the game is a server */
	public static boolean isValidServer() { return ISONLINE && ISHOST && server != null; }
	public static boolean hasConnectedClients() { return isValidServer() && server.hasClients(); }
	
	
	// LEVEL
	
	public static Level[] levels = new Level[6]; // This array stores the different levels.
	public static int currentLevel = 3; // This is the level the player is on. It defaults to 3, the surface.
	
	static boolean gameOver = false; // If the player wins this is set to true.
	
	
	static boolean running = true;
	
	public static void quit() {
		if (isConnectedClient()) client.endConnection();
		if (isValidServer()) server.endConnection();
		running = false;
	}
	
	
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			throwable.printStackTrace();
			
			StringWriter string = new StringWriter();
			PrintWriter printer = new PrintWriter(string);
			throwable.printStackTrace(printer);
			
			JTextArea errorDisplay = new JTextArea(string.toString());
			errorDisplay.setEditable(false);
			JScrollPane errorPane = new JScrollPane(errorDisplay);
			JOptionPane.showMessageDialog(null, errorPane, "An error has occurred", JOptionPane.ERROR_MESSAGE);
		});

		Initializer.parseArgs(args); // Parses the command line arguments
		
		input = new InputHandler(Renderer.canvas);
		
		Tiles.initTileList();
		Sound.init();
		Settings.init();

		World.resetGame(); // "half"-starts a new game, to set up initial variables
		player.eid = 0;
		new Load(true); // this loads any saved preferences.
		
		
		if (Network.autoclient)
			setMenu(new MultiplayerDisplay("localhost"));
		else if (!HAS_GUI)
			Network.startMultiplayerServer();
		else
			setMenu(new TitleDisplay()); //sets menu to the title screen.
		
		
		Initializer.createAndDisplayFrame();
		
		Renderer.initScreen();
		
		Initializer.run();
		
		
		if (debug) System.out.println("Main game loop ended; Terminating application...");
		System.exit(0);
	}
}

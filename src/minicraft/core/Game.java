package minicraft.core;

import java.util.ArrayList;
import java.util.List;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.network.MinicraftClient;
import minicraft.network.MinicraftServer;
import minicraft.saveload.Load;
import minicraft.screen.Display;
import minicraft.screen.MultiplayerDisplay;
import minicraft.screen.TitleDisplay;

import org.jetbrains.annotations.Nullable;

public class Game {
	Game() {} // can't instantiate the Game class.
	
	public static boolean debug = false;
	public static boolean HAS_GUI = true;
	
	public static final String NAME = "Minicraft Plus"; // This is the name on the application window
	public static final String VERSION = "2.0.4";
	
	public static InputHandler input; // input used in Game, Player, and just about all the *Menu classes.
	public static Player player;
	
	public static String gameDir; // The directory in which all the game files are stored
	public static List<String> notifications = new ArrayList<>();
	
	public static int MAX_FPS = (int) Settings.get("fps");
	
	
	static Display menu = null, newMenu = null; // the current menu you are on.
	// Sets the current menu.
	public static void setMenu(@Nullable Display display) { newMenu = display; }
	public static void exitMenu() {
		if(menu == null) return; // no action required; cannot exit from no menu
		Sound.back.play();
		newMenu = menu.getParent();
	}
	public static Display getMenu() { return newMenu; }
	
	public static boolean isMode(String mode) { return ((String)Settings.get("mode")).equalsIgnoreCase(mode); }
	
	
	/// MULTIPLAYER
	
	public static boolean ISONLINE = false;
	public static boolean ISHOST = false;
	
	public static MinicraftClient client = null;
	public static boolean isValidClient() { return ISONLINE && client != null; }
	public static boolean isConnectedClient() { return isValidClient() && client.isConnected(); }
	
	public static MinicraftServer server = null;
	public static boolean isValidServer() { return ISONLINE && ISHOST && server != null; }
	public static boolean hasConnectedClients() { return isValidServer() && server.hasClients(); }
	
	
	// LEVEL
	
	public static Level[] levels = new Level[6]; // This array stores the different levels.
	public static int currentLevel = 3; // This is the level the player is on. It defaults to 3, the surface.
	
	static boolean gameOver = false; // If the player wins this is set to true.
	
	
	static boolean running = true;
	
	public static void quit() {
		if(isConnectedClient()) client.endConnection();
		if(isValidServer()) server.endConnection();
		running = false;
	}
	
	
	public static void main(String[] args) {
		Initializer.parseArgs(args);
		
		input = new InputHandler(Renderer.canvas);
		
		Tiles.initTileList();
		Sound.init();
		Settings.init();
		
		World.resetGame(); // "half"-starts a new game, to set up initial variables
		player.eid = 0;
		new Load(true); // this loads any saved preferences.
		
		
		if(Network.autoclient)
			setMenu(new MultiplayerDisplay( "localhost"));
		else if(!HAS_GUI)
			Network.startMultiplayerServer();//setMenu(null);//new WorldSelectMenu());
		else
			setMenu(new TitleDisplay()); //sets menu to the title screen.
		
		
		Initializer.createAndDisplayFrame();
		
		Renderer.initScreen();
		
		Initializer.run();
		
		
		if (debug) System.out.println("main game loop ended; terminating application...");
		System.exit(0);
	}
}

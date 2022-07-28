package minicraft.core;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.network.Analytics;
import minicraft.saveload.Load;
import minicraft.saveload.Version;
import minicraft.screen.Display;
import minicraft.screen.ResourcePackDisplay;
import minicraft.screen.TitleDisplay;
import minicraft.util.Logging;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public class Game {
	protected Game() {} // Can't instantiate the Game class.

	public static boolean debug = false;

	public static final String NAME = "Minicraft Plus"; // This is the name on the application window.

	public static final Version VERSION = new Version("2.2.0-dev1");

	public static InputHandler input; // Input used in Game, Player, and just about all the *Menu classes.
	public static Player player;

	public static List<String> notifications = new ArrayList<>();

	public static int MAX_FPS = (int) Settings.get("fps");

	// DISPLAY
	static Display display = null, newDisplay = null;
	public static void setDisplay(@Nullable Display display) { newDisplay = display; }
	public static void exitDisplay() {
		if (display == null) {
			Logging.GAMEHANDLER.warn("Game tried to exit display, but no menu is open.");
			return;
		}
		Sound.back.play();
		newDisplay = display.getParent();
	}
	@Nullable
	public static Display getDisplay() { return newDisplay; }

	// GAMEMODE
	public static boolean isMode(String mode) { return ((String)Settings.get("mode")).equalsIgnoreCase(mode); }

	// LEVEL
	public static Level[] levels = new Level[6]; // This array stores the different levels.
	public static int currentLevel = 3; // This is the level the player is on. It defaults to 3, the surface.

	// GAME
	public static String gameDir; // The directory in which all the game files are stored
	static boolean gameOver = false; // If the player wins this is set to true.

	static boolean running = true;
	public static void quit() { running = false; }


	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(CrashHandler::crashHandle);

		Analytics.GameStartup.ping();

		Initializer.parseArgs(args); // Parses the command line arguments

		input = new InputHandler(Renderer.canvas);

		Tiles.initTileList();
		Sound.init();

		// Load default loc.
		Localization.loadLanguage();

		World.resetGame(); // "half"-starts a new game, to set up initial variables
		player.eid = 0;
		new Load(true); // This loads any saved preferences.
		MAX_FPS = (int) Settings.get("fps");

		// Load the selected language.
		Initializer.createAndDisplayFrame();

		setDisplay(new TitleDisplay()); // Sets menu to the title screen.

		Renderer.initScreen();

		// Loads the resorce pack locaded in save.
		new ResourcePackDisplay().initResourcePack();

		// Update fullscreen frame if Updater.FULLSCREEN was updated previously
		if (Updater.FULLSCREEN) {
			Updater.updateFullscreen();
		}

		// Actually start the game.
		Initializer.run();

		Logging.GAMEHANDLER.debug("Main game loop ended; Terminating application...");
		System.exit(0);
	}
}

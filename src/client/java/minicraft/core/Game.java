package minicraft.core;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.saveload.Load;
import minicraft.saveload.Version;
import minicraft.screen.Display;
import minicraft.screen.ResourcePackDisplay;
import minicraft.screen.TitleDisplay;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Game {
	protected Game() {
	} // Can't instantiate the Game class.

	public static final String NAME = "Minicraft Plus"; // This is the name on the application window.

	public static final Version VERSION = new Version("2.3.0-infdev2");

	public static InputHandler input; // Input used in Game, Player, and just about all the *Menu classes.
	public static Player player;

	public static List<String> notifications = new ArrayList<>();

	public static int MAX_FPS;

	// DISPLAY
	static Display currentDisplay = null;
	static final ArrayDeque<Display> displayQuery = new ArrayDeque<>();

	public static void setDisplay(@Nullable Display display) {
		if (display == null)
			displayQuery.clear();
		else
			displayQuery.add(display);
	}

	public static void exitDisplay() {
		exitDisplay(1);
	}

	public static void exitDisplay(int depth) {
		if (depth < 1) return; // There is nothing needed to exit.
		if (displayQuery.isEmpty()) {
			Logging.GAMEHANDLER.warn("Game tried to exit display, but no menu is open.");
			return;
		}
		Sound.play("craft");
		// Exiting the display remaining and checking if there are more displays available.
		// If there are more displays, displays with maximum amount exit.
		for (int i = 0; i < depth && !displayQuery.isEmpty(); i++) {
			Display parent = displayQuery.peekLast().getParent();
			if (parent == null)
				displayQuery.clear();
			else
				displayQuery.add(parent);
		}
	}

	@Nullable
	public static Display getDisplay() {
		return displayQuery.isEmpty() ? null : displayQuery.peekLast();
	}

	// GAMEMODE
	public static boolean isMode(String mode) {
		return ((String) Settings.get("mode")).equalsIgnoreCase(mode);
	}

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
		Thread.setDefaultUncaughtExceptionHandler(CrashHandler::crashHandle);

		Initializer.parseArgs(args); // Parses the command line arguments
		// Applying Game#debug first.

		new Load(true, true); // This loads basic saved preferences.
		// Reference: https://stackoverflow.com/a/13832805
		if ((boolean) Settings.get("hwa")) System.setProperty("sun.java2d.opengl", "true");
		MAX_FPS = (int) Settings.get("fps"); // DO NOT put this above.

		input = new InputHandler(Renderer.canvas);

		ResourcePackDisplay.initPacks();
		ResourcePackDisplay.reloadResources();

		Tiles.initTileList();

		// Load the selected language.
		Initializer.createAndDisplayFrame();

		setDisplay(new TitleDisplay()); // Sets menu to the title screen.

		Renderer.initScreen();

		World.resetGame(); // "half"-starts a new game, to set up initial variables
		player.eid = 0;
		new Load(true, false); // This loads any saved preferences.

		// Update fullscreen frame if Updater.FULLSCREEN was updated previously
		if (Updater.FULLSCREEN) {
			Updater.updateFullscreen();
		}

		Initializer.launchWindow();
		// Actually start the game.
		Initializer.run();

		Logging.GAMEHANDLER.debug("Main game loop ended; Terminating application...");
		System.exit(0);
	}
}

package minicraft.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.network.Analytics;
import minicraft.saveload.Load;
import minicraft.saveload.Version;
import minicraft.screen.Display;
import minicraft.screen.TitleDisplay;
import minicraft.util.Logging;
import minicraft.util.MyUtils;
import minicraft.util.resource.ReloadableResourceManager;
import minicraft.util.resource.ResourceLoader;
import minicraft.util.resource.ResourcePackManager;
import minicraft.util.resource.VanillaResourcePack;
import minicraft.util.resource.provider.DefaultResourcePackProvider;
import minicraft.util.resource.reloader.SplashManager;

public class Game {
	protected Game() {} // Can't instantiate the Game class.

	public static boolean debug = false;

	public static final String NAME = "Minicraft Plus"; // This is the name on the application window.

	public static final Version VERSION = new Version("2.2.0-dev2");

	public static InputHandler input; // Input used in Game, Player, and just about all the *Menu classes.
	public static Player player;

	public static List<String> notifications = new ArrayList<>();

	public static int MAX_FPS;

	// DISPLAY
	static Display display = null, newDisplay = null;
	public static void setDisplay(@Nullable Display display) { newDisplay = display; }
	public static void exitDisplay() {
		if (display == null) {
			Logging.GAMEHANDLER.warn("Game tried to exit display, but no menu is open.");
			return;
		}
		Sound.play("craft");
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

	public static final ReloadableResourceManager resourceManager = new ReloadableResourceManager();
	public static ResourcePackManager resourcePackManager;
	public static VanillaResourcePack defaultResourcePack;
	public static boolean reloading;
	@Nullable
	public static ResourceLoader currentLoader;

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(CrashHandler::crashHandle);

		Initializer.parseArgs(args); // Parses the command line arguments
		// Applying Game#debug first.

		Analytics.GameStartup.ping();

		input = new InputHandler(Renderer.canvas);

		Tiles.initTileList();

		// Load the selected language.
		Initializer.createAndDisplayFrame();

		setDisplay(new TitleDisplay()); // Sets menu to the title screen.

		defaultResourcePack = new VanillaResourcePack();
		resourcePackManager = new ResourcePackManager();
		resourcePackManager.addProvider(new DefaultResourcePackProvider(defaultResourcePack));
		resourcePackManager.findPacks();
		resourceManager.registerReloader(new SplashManager());
		// resourceManager.registerReloader(Sound.reloader);
		// resourceManager.registerReloader(new LocalizationReloader());
		// resourceManager.registerReloader(new SoundReloader());
		// resourceManager.registerReloader(new BookReloader());
		// resourceManager.registerReloader(new TextureReloader());
		resourcePackManager.setEnabledPacks(Arrays.asList("vanilla"));
		Game.reloading = true;
		Game.currentLoader = resourceManager.reload(resourcePackManager.getEnabledPacks(), () -> {
			Game.reloading = false;
			Game.currentLoader = null;
		});

		// TODO: remove reloading from this method so that the reload overlay can be shown >:(
		// So remove the splash window too because is not needed. When reloading, no screen and level should be render.
		Renderer.initScreen();

		World.resetGame(); // "half"-starts a new game, to set up initial variables
		player.eid = 0;
		new Load(true); // This loads any saved preferences.
		MAX_FPS = (int) Settings.get("fps"); // DO NOT put this above.

		// Update fullscreen frame if Updater.FULLSCREEN was updated previously
		if (Updater.FULLSCREEN) {
			Updater.updateFullscreen();
		}

		// Actually start the game.
		Initializer.run();

		Logging.GAMEHANDLER.debug("Main game loop ended; Terminating application...");

		MyUtils.shutdownWorkers();

		System.exit(0);
	}
}

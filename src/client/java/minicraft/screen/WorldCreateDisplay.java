package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Dimension;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.DisplayString;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.OptionalLong;
import java.util.regex.Pattern;

public class WorldCreateDisplay extends Display {
	public static final String DEFAULT_NAME = "New World";

	private static final Pattern detailedFilenamePattern;
	private static final String worldNameRegex;

	static {
		if (FileHandler.OS.contains("windows")) {
			// Reference: https://stackoverflow.com/a/6804755
			worldNameRegex = "[^<>:\"/\\\\|?*\\x00-\\x1F]+";
			//noinspection RegExpRepeatedSpace,RegExpRedundantEscape,RegExpUnexpectedAnchor
			detailedFilenamePattern = Pattern.compile(
				"# Match a valid Windows filename (unspecified file system).          \n" +
					"^                                # Anchor to start of string.        \n" +
					"(?!                              # Assert filename is not: CON, PRN, \n" +
					"  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
					"    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
					"    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
					"  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
					"  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
					"  $                              # and end of string                 \n" +
					")                                # End negative lookahead assertion. \n" +
					"[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
					"[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
					"$                                # Anchor to end of string.            ",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
		} else if (FileHandler.OS.contains("mac")) {
			worldNameRegex = "[^/:]+";
			detailedFilenamePattern = null;
		} else { // Reference: https://en.wikipedia.org/wiki/Filename#Length_restrictions
			worldNameRegex = "[^/\0]+";
			detailedFilenamePattern = null;
		}
	}

	private final InputEntry worldSeed;

	private OptionalLong getSeed() {
		String seedStr = worldSeed.getUserInput();

		// If there is no input seed, generate random number
		if (seedStr.isEmpty())
			return OptionalLong.empty();

		// If the seed is only numbers, just use numbers
		if (Pattern.matches("-?[0-9]*", seedStr) && seedStr.length() < 20) {
			try {
				return OptionalLong.of(Long.parseLong(seedStr));
			} catch (NumberFormatException e) {
				// In case if there is any unexpected exception occurs.
				Logging.UNEXPECTED.error(e);
			}
		}

		// If the seed is some combination of numbers/letters, hash them into an int64 number
		long seed = 1125899906842597L; // rather large prime number
		int len = seedStr.length();

		for (int i = 0; i < len; i++) {
			seed = 31 * seed + seedStr.charAt(i);
		}

		return OptionalLong.of(seed);
	}

	/** Checks only with the filesystem. */
	public static boolean isWorldNameLegal (String input) {
		try { // Checking if the folder name is valid;
			Paths.get(Game.gameDir + "/saves/" + input + "/");
		} catch (InvalidPathException e) {
			Logging.WORLD.debug("Invalid world name (InvalidPathException) \"{}\": {}", input, e.getMessage());
			return false;
		}

		if (detailedFilenamePattern != null && !detailedFilenamePattern.matcher(input).matches()) {
			Logging.WORLD.debug("Invalid file name (Not matches to the valid pattern with the corresponding system) \"{}\".", input);
			return false;
		}

		return input.length() < 120; // surety about filename length limits
	}

	/**
	 * Makes an input for user configuration on world name.
	 * @param beforeRenamed {@code null} if it is not renaming a world; else the original world name,
	 * this will be used to check if the user input matches the original world name and thus perform no change.
	 */
	public static WorldNameInputEntry makeWorldNameInput(@Nullable DisplayString prompt,
	                                                     String initValue, @Nullable String beforeRenamed) {
		return new WorldNameInputEntry(prompt, initValue, beforeRenamed);
	}

	public static class WorldNameInputEntry extends InputEntry {
		private final @Nullable String beforeRenamed;
		private String lastInput;
		private boolean valid;

		public WorldNameInputEntry(@Nullable DisplayString prompt, String initValue,
		                           @Nullable String beforeRenamed) {
			super(prompt, WorldCreateDisplay.worldNameRegex, 36, initValue);
			this.beforeRenamed = beforeRenamed;
			valid = false;
		}

		@Override
		public boolean isValid() {
			String input = getUserInput();
			if (input.isEmpty()) return true; // Default value
			if (!super.isValid()) return false;
			if (input.equals(beforeRenamed)) return true;
			if (!input.equals(lastInput)) {
				valid = isWorldNameLegal(input);
				lastInput = input;
			}

			return valid;
		}

		public @Nullable String getWorldName() {
			return !isValid() ? null :
				WorldSelectDisplay.getValidWorldName(getUserInput(), beforeRenamed != null);
		}

//			@Override
//			public void render(Screen screen, int x, int y, boolean isSelected, @Nullable IntRange bounds) {
//				super.render(screen, isGen ?
//					(getUserInput().length() > 11 ? x - (getUserInput().length() - 11) * 8 : x) :
//					x, y, isSelected, bounds);
//			}
	}

	private final SelectEntry createWorld;

	public WorldCreateDisplay() {
		super(true);

		WorldNameInputEntry nameField = makeWorldNameInput(Localization.getStaticDisplay(
			"minicraft.displays.world_create.options.world_name"), "", null);

		worldSeed = new InputEntry(Localization.getStaticDisplay("minicraft.displays.world_create.options.seed"),
			"[-!\"#%/()=+,a-zA-Z0-9]+", 20) {
			@Override
			public boolean isValid() { return true; }
		};

		StringEntry nameNotify = new StringEntry(Localization.getStaticDisplay(
			"minicraft.display.world_naming.world_name_notify", DEFAULT_NAME), Color.DARK_GRAY);

		createWorld = new SelectEntry(Localization.getStaticDisplay("minicraft.displays.world_create.create_world"), () -> {
			if (!nameField.isValid()) return;
			WorldSelectDisplay.setWorldName(nameField.getWorldName(), false);
			OptionalLong seed = getSeed();
			Long seedObj = seed.isPresent() ? seed.getAsLong() : null;
			Game.setDisplay(new LoadingDisplay(new WorldSettings(seedObj)));
		}) {
			@Override
			public void render(Screen screen, @Nullable Screen.RenderingLimitingModel limitingModel, int x, int y, boolean isSelected) {
				Font.draw(limitingModel, toString(), screen, x, y, isSelectable() ? Color.CYAN : Color.tint(Color.CYAN, -1, true));
			}
		};
		nameField.setChangeListener(o -> {
			boolean valid = nameField.isValid();
			createWorld.setSelectable(valid);
			nameNotify.setText(valid ?
				Localization.getStaticDisplay("minicraft.display.world_naming.world_name_notify",
					nameField.getWorldName()) :
				Localization.getStaticDisplay("minicraft.display.world_naming.world_name_notify_invalid"));
		});

		Menu mainMenu =
			new Menu.Builder(false, 3, RelPos.CENTER,
				nameField,
				nameNotify,
				new BlankEntry(),
				worldSeed,
				Settings.getEntry("size"),
				Settings.getEntry("theme"),
				Settings.getEntry("type"),
				Settings.getEntry("quests"),
				Settings.getEntry("tutorials"),
				Settings.getEntry("mode"),
				Settings.getEntry("scoretime"),
				new BlankEntry(),
				createWorld,
				new StringEntry(Localization.getStaticDisplay("minicraft.display.popup.cancel",
					Game.input.getMapping("EXIT")), Color.GRAY)
			)
				.setTitle(Localization.getStaticDisplay("minicraft.displays.world_create.title"))
				.createMenu();

		onScreenKeyboardMenu = OnScreenKeyboardMenu.checkAndCreateMenu();
		if (onScreenKeyboardMenu == null)
			menus = new Menu[] { mainMenu };
		else
			menus = new Menu[] { onScreenKeyboardMenu, mainMenu };
	}

	public static class WorldSettings {
		public final @Nullable Long seed;
		public WorldSettings(@Nullable Long seed) {
			this.seed = seed;
		}
	}

	OnScreenKeyboardMenu onScreenKeyboardMenu;

	@Override
	public void render(Screen screen) {
		super.render(screen);
		if (onScreenKeyboardMenu != null)
			onScreenKeyboardMenu.render(screen);
	}

	@Override
	public void tick(InputHandler input) {
		boolean acted = false; // Checks if typing action is needed to be handled.
		boolean takeExitHandle = true;
		if (onScreenKeyboardMenu == null)
			super.tick(input);
		else { // Handling the on-screen keyboard.
			try {
				onScreenKeyboardMenu.tick(input);
			} catch (OnScreenKeyboardMenu.OnScreenKeyboardMenuTickActionCompleted e) {
				acted = true;
			} catch (OnScreenKeyboardMenu.OnScreenKeyboardMenuBackspaceButtonActed e) {
				takeExitHandle = false;
				acted = true;
			}

			if (takeExitHandle && input.inputPressed("exit")) {
				Game.exitDisplay();
				return;
			}

			if (menus[1].getSelection() == 0) {
				if (input.buttonPressed(ControllerButton.X)) { // Hide the keyboard.
					onScreenKeyboardMenu.setVisible(!onScreenKeyboardMenu.isVisible());
					if (!onScreenKeyboardMenu.isVisible())
						selection = 1;
					else
						selection = 0;
				}

				if (!acted) menus[1].tick(input); // Process the tick of the main menu.
			} else if (selection == 0) {
				onScreenKeyboardMenu.setVisible(false);
				selection = 1;
				menus[1].tick(input);
			} else {
				menus[1].tick(input);
			}
		}
	}
}

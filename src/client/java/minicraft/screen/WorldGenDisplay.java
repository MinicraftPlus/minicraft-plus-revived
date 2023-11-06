package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.regex.Pattern;

public class WorldGenDisplay extends Display {
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
			seed = 31*seed + seedStr.charAt(i);
		}

		return OptionalLong.of(seed);
	}

	public static InputEntry makeWorldNameInput(String prompt, List<String> takenNames, String initValue, boolean isGen) {
		return new InputEntry(prompt, worldNameRegex, 36, initValue) {
			private String lastName;

			@Override
			public boolean isValid() {
				if(!super.isValid()) return false;
				String name = getUserInput();
				for(String other: takenNames)
					if(other.equalsIgnoreCase(name)) {
						if (!name.equals(lastName)) {
							Logging.WORLD.debug("Duplicated or existed world name \"{}\".", name);
							lastName = name;
						}

						return false;
					}

				try { // Checking if the folder name is valid;
					Paths.get(Game.gameDir+"/saves/"+name+"/");
				} catch (InvalidPathException e) {
					if (!name.equals(lastName)) {
						Logging.WORLD.debug("Invalid world name (InvalidPathException) \"{}\": {}", name, e.getMessage());
						lastName = name;
					}

					return false;
				}

				if (detailedFilenamePattern != null) {
					if (!detailedFilenamePattern.matcher(name).matches()) {
						if (!name.equals(lastName)) {
							Logging.WORLD.debug("Invalid file name (Not matches to the valid pattern with the corresponding system) \"{}\".", name);
							lastName = name;
						}

						return false;
					}
				}

				lastName = name;
				return name.length() <= 255; // If it is lower than the general valid folder name length.
			}

			@Override
			public String getUserInput() {
				return super.getUserInput().toLowerCase(Localization.getSelectedLocale());
			}

//			@Override
//			public void render(Screen screen, int x, int y, boolean isSelected, @Nullable IntRange bounds) {
//				super.render(screen, isGen?
//					(getUserInput().length() > 11? x - (getUserInput().length()-11) * 8: x):
//					x, y, isSelected, bounds);
//			}
		};
	}

	public WorldGenDisplay() {
		super(true);

		InputEntry nameField = makeWorldNameInput("minicraft.displays.world_gen.enter_world", WorldSelectDisplay.getWorldNames(), "", true)
			.setRenderingBounds(new ListEntry.IntRange(MinicraftImage.boxWidth * 2, Screen.w - MinicraftImage.boxWidth * 2)).setEntryPos(RelPos.LEFT);

		SelectEntry nameHelp = new SelectEntry("minicraft.displays.world_gen.troublesome_input", () -> Game.setDisplay(new MessageDisplay("minicraft.displays.world_gen.troublesome_input.msg"))) {
			@Override
			public int getColor(boolean isSelected) {
				return Color.get(1, 204);
			}
		};

		nameHelp.setVisible(false);

		HashSet<String> controls = new HashSet<>();
		controls.addAll(Arrays.asList(Game.input.getMapping("cursor-up").split("/")));
		controls.addAll(Arrays.asList(Game.input.getMapping("cursor-down").split("/")));
		for (String key: controls) {
			if(key.matches("^\\w$")) {
				nameHelp.setVisible(true);
				break;
			}
		}

		worldSeed = new InputEntry("minicraft.displays.world_gen.world_seed", "[-!\"#%/()=+,a-zA-Z0-9]+", 20) {
			@Override
			public boolean isValid() { return true; }
		}.setRenderingBounds(new ListEntry.IntRange(MinicraftImage.boxWidth * 2, Screen.w - MinicraftImage.boxWidth * 2)).setEntryPos(RelPos.LEFT);

		Menu mainMenu =
			new Menu.Builder(false, 10, RelPos.LEFT,
				nameField,
				nameHelp,
				Settings.getEntry("mode"),
				Settings.getEntry("scoretime"),

				new SelectEntry("minicraft.displays.world_gen.create_world", () -> {
					if(!nameField.isValid()) return;
					WorldSelectDisplay.setWorldName(nameField.getUserInput(), false);
					OptionalLong seed = getSeed();
					Long seedObj = seed.isPresent() ? seed.getAsLong() : null;
					Game.setDisplay(new LoadingDisplay(new WorldSettings(seedObj)));
				}) {
					@Override
					public void render(Screen screen, int x, int y, boolean isSelected, @Nullable IntRange bounds) {
						Font.draw(toString(), screen, x, y, Color.CYAN, bounds);
					}
				},

				Settings.getEntry("size"),
				Settings.getEntry("theme"),
				Settings.getEntry("type"),
				Settings.getEntry("quests"),
				Settings.getEntry("tutorials"),
				worldSeed
			)
				.setDisplayLength(5)
				.setScrollPolicies(0.8f, false)
				.setTitle("minicraft.displays.world_gen.title")
				.setMaxBoundsAsRenderingBounds()
				.createMenu();

		Rectangle menuBounds = mainMenu.getBounds();
		ListEntry.IntRange xBounds = new ListEntry.IntRange(menuBounds.getLeft() + MinicraftImage.boxWidth * 2, menuBounds.getRight() - MinicraftImage.boxWidth * 2);
		nameField.setBounds(xBounds);
		worldSeed.setBounds(xBounds);

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

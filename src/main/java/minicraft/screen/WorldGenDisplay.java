package minicraft.screen;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.ptr.IntByReference;
import minicraft.core.Game;
import minicraft.core.io.FileHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.util.Logging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class WorldGenDisplay extends Display {

	private static final String worldNameRegex;
	static {
		if (FileHandler.OS.contains("windows")) {
			// Reference: https://stackoverflow.com/a/6804755
			worldNameRegex = "[^<>:\"/\\\\|?*\\x00-\\x1F]+";
//				Pattern.compile(
//				"# Match a valid Windows filename (unspecified file system).          \n" +
//					"^                                # Anchor to start of string.        \n" +
//					"(?!                              # Assert filename is not: CON, PRN, \n" +
//					"  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
//					"    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
//					"    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
//					"  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
//					"  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
//					"  $                              # and end of string                 \n" +
//					")                                # End negative lookahead assertion. \n" +
//					"[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
//					"[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
//					"$                                # Anchor to end of string.            ",
//				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS).toString();
		} else if (FileHandler.OS.contains("mac")) {
			worldNameRegex = "[^/:]+";
		} else { // Reference: https://en.wikipedia.org/wiki/Filename#Length_restrictions
			worldNameRegex = "[^/\0]+";
		}
	}

	private static InputEntry worldSeed = new InputEntry("minicraft.displays.world_gen.world_seed", "[-!\"#%/()=+,a-zA-Z0-9]+", 20);

	public static OptionalLong getSeed() {
		String seedStr = worldSeed.getUserInput();

		// If there is no input seed, generate random number
		if(seedStr.length() == 0)
			return OptionalLong.empty();

		// If the seed is only numbers, just use numbers
		if (Pattern.matches("-?[0-9]*", seedStr) && seedStr.length() < 20) {
			return OptionalLong.of(Long.parseLong(seedStr));
		}

		// If the seed is some combination of numbers/letters, hash them into a floating point number
		long seed = 1125899906842597L; // rather large prime number
		int len = seedStr.length();

		for (int i = 0; i < len; i++) {
			seed = 31*seed + seedStr.charAt(i);
		}

		return OptionalLong.of(seed);
	}

	public static InputEntry makeWorldNameInput(String prompt, List<String> takenNames, String initValue, boolean isGen) {
		return new InputEntry(prompt, worldNameRegex, 36, initValue) {
			@Override
			public boolean isValid() {
				if(!super.isValid()) return false;
				String name = getUserInput();
				for(String other: takenNames)
					if(other.equalsIgnoreCase(name))
						return false;

				// Checking length.
				int maxlength;
				if (FileHandler.OS.contains("windows")) {
					String drive = null;
					FindDrive:
					{ // Reference: https://github.com/apache/commons-io/blob/37cad9653b46ad4f0b2da2ac570546e5941694c1/src/main/java/org/apache/commons/io/FilenameUtils.java#L806
						String fileName = FileHandler.getSystemGameDir() + "/" + FileHandler.getLocalGameDir();
						char ch0 = fileName.charAt(0);
						if (ch0 == '~') {
							int posUnix = fileName.indexOf("/", 1);
							int posWin = fileName.indexOf("\\", 1);
							posUnix = posUnix == -1 ? posWin : posUnix;
							posWin = posWin == -1 ? posUnix : posWin;
							drive = fileName.substring(0, Math.min(posUnix, posWin) + 1);
							break FindDrive;
						}
						final char ch1 = fileName.charAt(1);
						if (ch1 == ':') {
							ch0 = Character.toUpperCase(ch0);
							if (ch0 >= 'A' && ch0 <= 'Z') {
								drive = fileName.substring(0, 3);
								break FindDrive;
							}
							break FindDrive;
						}
						int posUnix = fileName.indexOf("/", 2);
						int posWin = fileName.indexOf("\\", 2);
						posUnix = posUnix == -1 ? posWin : posUnix;
						posWin = posWin == -1 ? posUnix : posWin;
						final int pos = Math.min(posUnix, posWin) + 1;
						final String hostnamePart = fileName.substring(2, pos - 1);
						// Reference: http://www.java2s.com/example/java/java.util.regex/is-ipv6-address-by-regex.html
						if (hostnamePart.matches("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$") ||
							hostnamePart.matches("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$") ||
							((Supplier<Boolean>) () -> {
								// Reference: https://github.com/apache/commons-io/blob/37cad9653b46ad4f0b2da2ac570546e5941694c1/src/main/java/org/apache/commons/io/FilenameUtils.java#L1166
								final String[] parts = name.split("\\.", -1);
								for (int i = 0; i < parts.length; i++) {
									if (parts[i].isEmpty()) {
										// trailing dot is legal, otherwise we've hit a .. sequence
										return i == parts.length - 1;
									}
									if (!parts[i].matches("^[a-zA-Z0-9][a-zA-Z0-9-]*$")) {
										return false;
									}
								}
								return true;
							}).get())
							drive = fileName.substring(0, pos + 1);
					}

					maxlength = 260; // Default value when LongPaths is not enabled.
					// Reference: https://stackoverflow.com/a/15656224
					IntByReference buf = new IntByReference();
					if (Kernel32.INSTANCE.GetVolumeInformation(drive, null, 0, null, buf, null, null, 0)) {
						if (buf.getValue() < maxlength) maxlength = buf.getValue();
					} else { // On error.
						Logging.WORLD.error("Unable to get volume information (Windows): " + Kernel32.INSTANCE.GetLastError());
					}
				} else if (!FileHandler.OS.contains("mac")) { // Linux
					LibC.Statvfs statvfs = new LibC.Statvfs();
					if (LibC.INSTANCE.statvfs(FileHandler.getSystemGameDir()+"/"+FileHandler.getLocalGameDir(), statvfs) == 0) {
						int v = statvfs.f_namemax.intValue();
						if (v <= 0) maxlength = 255;
						else maxlength = v;
					} else { // On error. Error number please refer to com.sun.jna.platform.linux.ErrNo.
						Logging.WORLD.error("Unable to get filesystem information (Linux): " + Native.getLastError());
						maxlength = 255;
					}
				} else {
					maxlength = 255; // Basically 255 for mac.
				}

				return name.length() <= maxlength;
			}

			@Override
			public String getUserInput() {
				return super.getUserInput().toLowerCase(Localization.getSelectedLocale());
			}

			@Override
			public void render(Screen screen, int x, int y, boolean isSelected) {
				super.render(screen, isGen?
					(getUserInput().length() > 11? x - (getUserInput().length()-11) * 8: x):
					x, y, isSelected);
			}
		};
	}

	public WorldGenDisplay() {
		super(true);

		InputEntry nameField = makeWorldNameInput("minicraft.displays.world_gen.enter_world", WorldSelectDisplay.getWorldNames(), "", true);

		SelectEntry nameHelp = new SelectEntry("minicraft.displays.world_gen.troublesome_input", () -> Game.setDisplay(new PopupDisplay(null, null, "minicraft.displays.world_gen.troublesome_input.msg"))) {
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
		};

		menus = new Menu[] {
			new Menu.Builder(false, 10, RelPos.LEFT,
				nameField,
				nameHelp,
				Settings.getEntry("mode"),
				Settings.getEntry("scoretime"),

				new SelectEntry("minicraft.displays.world_gen.create_world", () -> {
					if(!nameField.isValid()) return;
					WorldSelectDisplay.setWorldName(nameField.getUserInput(), false);
					Game.setDisplay(new LoadingDisplay());
				}) {
					@Override
					public void render(Screen screen, int x, int y, boolean isSelected) {
						Font.draw(toString(), screen, x, y, Color.CYAN);
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
				.createMenu()
		};
	}
}

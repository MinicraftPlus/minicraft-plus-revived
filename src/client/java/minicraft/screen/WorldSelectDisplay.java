package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.saveload.Version;
import minicraft.screen.entry.ArrayEntry;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.DisplayString;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class WorldSelectDisplay extends Display {

	private static final ArrayList<WorldInfo> worlds = new ArrayList<>();

	private static final String worldsDir = Game.gameDir + "/saves/";
	private static final DateTimeFormatter dateTimeFormat;

	static {
		dateTimeFormat = new DateTimeFormatterBuilder()
			.appendValue(ChronoField.YEAR)
			.appendLiteral('/').padNext(2, '0')
			.appendValue(ChronoField.MONTH_OF_YEAR)
			.appendLiteral('/').padNext(2, '0')
			.appendValue(ChronoField.DAY_OF_MONTH)
			.appendLiteral(' ').padNext(2, '0')
			.appendValue(ChronoField.HOUR_OF_DAY)
			.appendLiteral(':').padNext(2, '0')
			.appendValue(ChronoField.MINUTE_OF_HOUR)
			.toFormatter();
	}

	private static String worldName = "";
	private static boolean loadedWorld = true;

	private int worldSelected = 0;

	public WorldSelectDisplay() {
		super(true);
	}

	public static class WorldInfo {
		/** The world name */
		public final String name;
		public final @Nullable Version version;
		public final String mode;
		/** The world save folder (filesystem) name */
		public final String saveName;
		public final LocalDateTime lastPlayed;
		/** The state of the Air Wizard, {@code true} if defeated */
		public final boolean cleared;

		public WorldInfo(String name, @Nullable Version version, String mode, String saveName, LocalDateTime lastPlayed,
		                 boolean cleared) {
			this.name = name;
			this.version = version;
			this.mode = mode;
			this.saveName = saveName;
			this.lastPlayed = lastPlayed;
			this.cleared = cleared;
		}
	}

	private final List<StringEntry> bottomEntries = new ArrayList<>();

	@Override
	public void init(Display parent) {
		super.init(parent);

		worldName = "";
		loadedWorld = true;
		menus = new Menu[2];
		menus[1] = new Menu.Builder(true, 2, RelPos.CENTER)
			.setDisplayLength(3)
			.setSize(Screen.w - 16, 8 * 5 + 2 * 2)
			.setPositioning(new Point(Screen.w / 2, Screen.h - 36), RelPos.TOP)
			.createMenu();

		// Update world list
		updateWorlds();
		updateEntries();

		// Entries on the bottom, 6 items and 3 lines, 2 items per line in order
		bottomEntries.add(new StringEntry(Localization.getStaticDisplay(
			"minicraft.displays.world_select.action.play",
			Game.input.getMapping("SELECT"))) {
			@Override
			public int getColor(boolean isSelected) {
				return !worlds.isEmpty() ? Color.GRAY : Color.DIMMED_GRAY;
			}
		});
		bottomEntries.add(new StringEntry(Localization.getStaticDisplay("minicraft.displays.world_select.action.return",
			Game.input.getMapping("EXIT")), Color.GRAY));
		bottomEntries.add(new StringEntry(Localization.getStaticDisplay("minicraft.displays.world_select.action.new",
			Game.input.selectMapping("N", "X")), Color.GRAY));
		bottomEntries.add(new StringEntry(Localization.getStaticDisplay(
			"minicraft.displays.world_select.action.delete",
			Game.input.selectMapping("D", "LEFTRIGHTTRIGGER"))) {
			@Override
			public int getColor(boolean isSelected) {
				return !worlds.isEmpty() ? Color.RED : Color.DIMMED_RED;
			}
		});
		bottomEntries.add(new StringEntry(Localization.getStaticDisplay(
			"minicraft.displays.world_select.action.rename",
			Game.input.selectMapping("R", "RIGHTBUMPER"))) {
			@Override
			public int getColor(boolean isSelected) {
				return !worlds.isEmpty() ? Color.GREEN : Color.DIMMED_GREEN;
			}
		});
		bottomEntries.add(new StringEntry(Localization.getStaticDisplay(
			"minicraft.displays.world_select.action.copy",
			Game.input.selectMapping("C", "LEFTBUMPER"))) {
			@Override
			public int getColor(boolean isSelected) {
				return !worlds.isEmpty() ? Color.BLUE : Color.DIMMED_BLUE;
			}
		});
	}

	private void updateEntries() {
		ArrayList<ListEntry> entries = new ArrayList<>();

		for (WorldInfo world : worlds) {
			entries.add(new SelectEntry(new DisplayString.StaticString(world.name),
				() -> loadWorld(world)));
		}

		menus[0] = new Menu.Builder(false, 0, RelPos.CENTER, entries)
			.setDisplayLength(10)
			.setPositioning(new Point(Screen.w / 2, 18), RelPos.BOTTOM)
			.setScrollPolicies(1, false)
			.setSearcherBar(true)
			.createMenu();
		updateWorldDescription(0);
	}

	private void updateWorldDescription(int selection) {
		worldSelected = selection;
		if (worlds.isEmpty()) menus[1].setEntries(new ListEntry[0]);
		else {
			WorldInfo world = worlds.get(selection);
			menus[1].setEntries(new ListEntry[] {
				new StringEntry(new DisplayString.StaticString(world.lastPlayed.format(dateTimeFormat))),
				new StringEntry(Localization.getStaticDisplay("minicraft.displays.world_select.world_desc",
					world.mode.equals("minicraft.displays.world_create.options.game_mode.hardcore") ?
						Color.RED_CODE : "", Localization.getStaticDisplay(world.mode), Color.WHITE_CODE,
					world.version != null ? world.version.compareTo(Game.VERSION) > 0 ? Color.RED_CODE :
						// Checks if either the world or the game is pre-release.
						world.version.toArray()[3] != 0 || Game.VERSION.toArray()[3] != 0 ? Color.GREEN_CODE : "" : "",
					world.version == null ? "< 1.9.1" : world.version)),
				world.cleared ? new StringEntry(Localization.getStaticDisplay(
					"minicraft.displays.world_select.world_desc.cleared"), Color.CYAN) : new BlankEntry()
			});
		}

		menus[1].setSelection(selection);
	}

	private static void loadWorld(WorldInfo world) {
		worldName = world.name;
		Game.setDisplay(new LoadingDisplay(null));
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		if (input.getMappedKey("N").isClicked() || input.buttonPressed(ControllerButton.X)) {
			Game.setDisplay(new WorldCreateDisplay());
			return;
		}

		if (worldSelected != menus[0].getSelection()) {
			updateWorldDescription(menus[0].getSelection());
		}

		if (!worlds.isEmpty()) {
			if (input.getMappedKey("C").isClicked() || input.buttonPressed(ControllerButton.LEFTBUMPER)) {
				String worldName = worlds.get(menus[0].getSelection()).name;
				WorldCreateDisplay.WorldNameInputEntry nameInput = WorldCreateDisplay.makeWorldNameInput(null, worldName, null);
				//noinspection DuplicatedCode
				StringEntry nameNotify = new StringEntry(Localization.getStaticDisplay(
					"minicraft.display.world_naming.world_name_notify", nameInput.getWorldName()), Color.DARK_GRAY);
				nameInput.setChangeListener(o -> nameNotify.setText(nameInput.isValid() ?
					Localization.getStaticDisplay("minicraft.display.world_naming.world_name_notify",
						nameInput.getWorldName()) :
					Localization.getStaticDisplay("minicraft.display.world_naming.world_name_notify_invalid")));
				ArrayList<ListEntry> entries = new ArrayList<>();
				entries.add(new StringEntry(Localization.getStaticDisplay(
					"minicraft.displays.world_select.popups.display.change"), Color.BLUE));
				//noinspection DuplicatedCode
				entries.add(nameInput);
				entries.add(nameNotify);
				entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
					Localization.getLocalized("minicraft.displays.world_select.popups.display.confirm", Game.input.getMapping("select")),
					Localization.getLocalized("minicraft.displays.world_select.popups.display.cancel", Game.input.getMapping("exit"))
				)));

				ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
				callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
					// The location of the world folder on the disk.
					File world = new File(worldsDir + worlds.get(menus[0].getSelection()).saveName);

					// Do the action.
					if (!nameInput.isValid())
						return false;
					//user hits enter with a valid new name; copy is created here.
					File newworld = new File(worldsDir + nameInput.getWorldName());
					newworld.mkdirs();
					Logging.GAMEHANDLER.debug("Copying world {} to world {}.", world, newworld);
					// walk file tree
					try {
						FileHandler.copyFolderContents(world.toPath(), newworld.toPath(), FileHandler.REPLACE_EXISTING, false);
					} catch (IOException e) {
						e.printStackTrace();
					}

					//noinspection DuplicatedCode
					Sound.play("confirm");
					updateWorlds();
					updateEntries();
					Game.exitDisplay();

					return true;
				}));

				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 0), entries.toArray(new ListEntry[0])));
			} else if (input.getMappedKey("R").isClicked() || input.buttonPressed(ControllerButton.RIGHTBUMPER)) {
				String worldName = worlds.get(menus[0].getSelection()).name;
				WorldCreateDisplay.WorldNameInputEntry nameInput = WorldCreateDisplay.makeWorldNameInput(null, worldName, worldName);
				//noinspection DuplicatedCode
				StringEntry nameNotify = new StringEntry(Localization.getStaticDisplay(
					"minicraft.display.world_naming.world_name_notify", nameInput.getWorldName()), Color.DARK_GRAY);
				nameInput.setChangeListener(o -> nameNotify.setText(nameInput.isValid() ?
					Localization.getStaticDisplay("minicraft.display.world_naming.world_name_notify",
						nameInput.getWorldName()) :
					Localization.getStaticDisplay("minicraft.display.world_naming.world_name_notify_invalid")));
				ArrayList<ListEntry> entries = new ArrayList<>();
				entries.add(new StringEntry(Localization.getStaticDisplay(
					"minicraft.displays.world_select.popups.display.change"), Color.GREEN));
				//noinspection DuplicatedCode
				entries.add(nameInput);
				entries.add(nameNotify);
				entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
					Localization.getLocalized("minicraft.displays.world_select.popups.display.confirm", Game.input.getMapping("select")),
					Localization.getLocalized("minicraft.displays.world_select.popups.display.cancel", Game.input.getMapping("exit"))
				)));

				ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
				callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
					// The location of the world folder on the disk.
					File world = new File(worldsDir + worlds.get(menus[0].getSelection()).saveName);

					// Do the action.
					if (!nameInput.isValid())
						return false;

					// User hits enter with a vaild new name; name is set here:
					String name = nameInput.getWorldName();

					// Try to rename the file, if it works, return
					if (world.renameTo(new File(worldsDir + name))) {
						Logging.GAMEHANDLER.debug("Renaming world {} to new name: {}", world, name);
						WorldSelectDisplay.updateWorlds();
					} else {
						Logging.GAMEHANDLER.error("Rename failed in WorldEditDisplay.");
					}

					//noinspection DuplicatedCode
					Sound.play("confirm");
					updateWorlds();
					updateEntries();
					Game.exitDisplay();

					return true;
				}));

				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 0), entries.toArray(new ListEntry[0])));
			} else if (input.getMappedKey("D").isClicked() || input.leftTriggerPressed() && input.rightTriggerPressed()) {
				ArrayList<ListEntry> entries = new ArrayList<>();
				entries.addAll(Arrays.asList(StringEntry.useLines(Color.RED, Localization.getLocalized("minicraft.displays.world_select.popups.display.delete",
					Color.toStringCode(Color.tint(Color.RED, 1, true)), worlds.get(menus[0].getSelection()).name,
					Color.RED_CODE))
				));

				entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
					Localization.getLocalized("minicraft.displays.world_select.popups.display.confirm", Game.input.getMapping("select")),
					Localization.getLocalized("minicraft.displays.world_select.popups.display.cancel", Game.input.getMapping("exit"))
				)));

				ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
				callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
					// The location of the world folder on the disk.
					File world = new File(worldsDir + worlds.get(menus[0].getSelection()).saveName);

					// Do the action.
					Logging.GAMEHANDLER.debug("Deleting world: {}", world);
					File[] list = world.listFiles();
					for (File file : list) {
						file.delete();
					}
					world.delete();

					Sound.play("confirm");
					updateWorlds();
					updateEntries();
					Game.exitDisplay();
					if (menus[0].getSelection() >= worlds.size()) {
						menus[0].setSelection(worlds.size() - 1);
					}

					return true;
				}));

				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 0), entries.toArray(new ListEntry[0])));
			}
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Rendering entries on the bottom
		int gap = 2 * 8; // a gap between 2 entries
		for (int i = 0; i < 3; ++i) {
			int leftWidth = bottomEntries.get(i * 2).getWidth();
			int rightWidth = bottomEntries.get(i * 2 + 1).getWidth();
			int halfTotalWidth = (leftWidth + rightWidth + gap) / 2;
			bottomEntries.get(i * 2).render(screen, null, Screen.w / 2 - halfTotalWidth,
				Screen.h - (3 - i) * 10, false);
			bottomEntries.get(i * 2 + 1).render(screen, null, Screen.w / 2 + halfTotalWidth - rightWidth,
				Screen.h - (3 - i) * 10, false);
		}

		// Title
		Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.select_world"),
			screen, 0, Color.LIGHT_GRAY);
	}

	public static boolean anyWorld() {
		return !worlds.isEmpty();
	}

	public static void updateWorlds() {
		Logging.GAMEHANDLER.debug("Updating worlds list.");

		// Get folder containing the worlds and load them.
		File worldSavesFolder = new File(worldsDir);

		// Try to create the saves folder if it doesn't exist.
		if (worldSavesFolder.mkdirs()) {
			Logging.GAMEHANDLER.trace("World save folder created.");
		}

		// Get all the files (worlds) in the folder.
		File[] worldFolders = worldSavesFolder.listFiles();

		if (worldFolders == null) {
			Logging.GAMEHANDLER.error("Game location file folder is null, somehow...");
			return;
		}

		worlds.clear();

		// Check if there are no files in folder.
		if (worldFolders.length == 0) {
			Logging.GAMEHANDLER.debug("No worlds in folder. Won't bother loading.");
			return;
		}


		// Iterate between every file in worlds.
		for (File file : worldFolders) {
			if (file.isDirectory()) {
				String[] files = file.list();
				if (files != null && files.length > 0 && Arrays.stream(files).anyMatch(f -> f.equalsIgnoreCase("Game" + Save.extension))) {
					WorldInfo world = loadWorldInfo(file);
					if (world != null) worlds.add(world);
				}
			}
		}

		worlds.sort(Comparator.<WorldInfo, LocalDateTime>comparing(a -> a.lastPlayed).reversed());
	}

	@Nullable
	private static WorldInfo loadWorldInfo(File folder) {
		try {
			String name = folder.getName();
			List<String> data = Arrays.asList(Load.loadFromFile(
				new File(folder, "Game" + Save.extension).toString(), true).split(","));
			Version version = new Version(data.get(0));

			String modeData;
			if (version.compareTo(new Version("2.2.0-dev1")) >= 0)
				modeData = data.get(2);
			else if (version.compareTo(new Version("2.0.4-dev8")) >= 0)
				modeData = data.get(1);
			else {
				List<String> playerData = Arrays.asList(Load.loadFromFile(
					new File(folder, "Player" + Save.extension).toString(), true).split(","));
				if (version.compareTo(new Version("2.0.4-dev7")) >= 0)
					modeData = playerData.get(Integer.parseInt(playerData.get(6)) > 0 ? 11 : 9);
				else
					modeData = playerData.get(9);
			}

			int mode = modeData.contains(";") ? Integer.parseInt(modeData.split(";")[0]) : Integer.parseInt(modeData);
			if (version.compareTo(new Version("2.0.3")) <= 0)
				mode--; // We changed the min mode idx from 1 to 0.

			long lastModified = folder.lastModified();
			boolean cleared = Boolean.parseBoolean(data.get(
				version.compareTo(new Version("2.2.0-dev1")) >= 0 ? 6 :
					version.compareTo(new Version("2.0.4-dev8")) >= 0 ? 5 : 4));
			//noinspection unchecked
			return new WorldInfo(name, version, ((ArrayEntry<String>) Settings.getEntry("mode")).getValue(mode), name,
				LocalDateTime.ofEpochSecond(lastModified / 1000,
					(int) (lastModified % 1000) * 1000000, ZoneOffset.UTC), cleared);
		} catch (IOException | IndexOutOfBoundsException e) {
			Logging.WORLD.warn(e, "Unable to load world \"{}\"", folder.getName());
			return null;
		}
	}

	public static String getWorldName() {
		return worldName;
	}

	public static void setWorldName(String world, boolean loaded) {
		worldName = world;
		loadedWorld = loaded;
	}

	public static boolean hasLoadedWorld() {
		return loadedWorld;
	}

	/**
	 * Solves for filename problems.
	 * {@link WorldCreateDisplay#isWorldNameLegal(String)} should be checked before this.
	 */
	public static String getValidWorldName(String input, boolean ignoreDuplicate) {
		if (input.isEmpty()) return WorldCreateDisplay.DEFAULT_NAME;

		if (!ignoreDuplicate && worlds.stream().anyMatch(w -> w.name.equalsIgnoreCase(input))) {
			Logging.WORLD.debug("Duplicated or existed world name \"{}\".", input);
			int count = 0;
			File folder;
			String filename;
			do {
				count++;
				filename = String.format("%s (%d)", input, count);
				folder = new File(Game.gameDir + "/saves/", filename);
			} while (folder.exists());
			return filename;
		}

		return input;
	}
}

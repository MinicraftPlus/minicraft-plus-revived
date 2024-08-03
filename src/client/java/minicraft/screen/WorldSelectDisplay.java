package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.saveload.Version;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Logging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class WorldSelectDisplay extends Display {

	private static final ArrayList<String> worldNames = new ArrayList<>();
	private static final ArrayList<Version> worldVersions = new ArrayList<>();

	private static final String worldsDir = Game.gameDir + "/saves/";

	private static String worldName = "";
	private static boolean loadedWorld = true;

	static {
		updateWorlds();
	}

	public WorldSelectDisplay() {
		super(true);
	}

	@Override
	public void init(Display parent) {
		super.init(parent);

		worldName = "";
		loadedWorld = true;

		// Update world list
		updateWorlds();
		updateEntries();
	}

	private void updateEntries() {
		SelectEntry[] entries = new SelectEntry[worldNames.size()];

		for (int i = 0; i < entries.length; i++) {
			final String name = worldNames.get(i);
			final Version version = worldVersions.get(i);
			entries[i] = new SelectEntry(name, () -> {
				// Executed when we select a world.
				if (version.compareTo(Game.VERSION) > 0)
					return; // cannot load a game saved by a higher version!
				worldName = name;
				Game.setDisplay(new LoadingDisplay());
			}, false);
		}

		menus = new Menu[] {
			new Menu.Builder(false, 0, RelPos.CENTER, entries)
				.setDisplayLength(5)
				.setScrollPolicies(1, true)
				.createMenu()
		};
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		if (input.getMappedKey("SHIFT-C").isClicked() || input.buttonPressed(ControllerButton.LEFTBUMPER)) {
			ArrayList<ListEntry> entries = new ArrayList<>();
			ArrayList<String> names = WorldSelectDisplay.getWorldNames();
			entries.add(new StringEntry("minicraft.displays.world_select.popups.display.change", Color.BLUE));
			entries.add(WorldGenDisplay.makeWorldNameInput("", names, worldName, false));
			entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
				Localization.getLocalized("minicraft.displays.world_select.popups.display.confirm", Game.input.getMapping("select")),
				Localization.getLocalized("minicraft.displays.world_select.popups.display.cancel", Game.input.getMapping("exit"))
			)));

			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
				InputEntry entry;

				// The location of the world folder on the disk.
				File world = new File(worldsDir + worldNames.get(menus[0].getSelection()));

				// Do the action.
				entry = (InputEntry) popup.getCurEntry();
				if (!entry.isValid())
					return false;
				//user hits enter with a valid new name; copy is created here.
				String newname = entry.getUserInput();
				File newworld = new File(worldsDir + newname);
				newworld.mkdirs();
				Logging.GAMEHANDLER.debug("Copying world {} to world {}.", world, newworld);
				// walk file tree
				try {
					FileHandler.copyFolderContents(world.toPath(), newworld.toPath(), FileHandler.REPLACE_EXISTING, false);
				} catch (IOException e) {
					e.printStackTrace();
				}

				Sound.play("confirm");
				updateWorlds();
				updateEntries();
				if (WorldSelectDisplay.getWorldNames().size() > 0) {
					Game.exitDisplay();
				} else {
					Game.exitDisplay(3); // Exiting to title display.
					Game.setDisplay(new WorldGenDisplay());
				}

				return true;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 0), entries.toArray(new ListEntry[0])));
		} else if (input.getMappedKey("SHIFT-R").isClicked() || input.buttonPressed(ControllerButton.RIGHTBUMPER)) {
			ArrayList<ListEntry> entries = new ArrayList<>();
			ArrayList<String> names = WorldSelectDisplay.getWorldNames();
			names.remove(worldName);
			entries.add(new StringEntry("minicraft.displays.world_select.popups.display.change", Color.GREEN));
			entries.add(WorldGenDisplay.makeWorldNameInput("", names, worldName, false));
			entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
				Localization.getLocalized("minicraft.displays.world_select.popups.display.confirm", Game.input.getMapping("select")),
				Localization.getLocalized("minicraft.displays.world_select.popups.display.cancel", Game.input.getMapping("exit"))
			)));

			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
				// The location of the world folder on the disk.
				File world = new File(worldsDir + worldNames.get(menus[0].getSelection()));

				// Do the action.
				InputEntry entry = (InputEntry) popup.getCurEntry();
				if (!entry.isValid())
					return false;

				// User hits enter with a vaild new name; name is set here:
				String name = entry.getUserInput();

				// Try to rename the file, if it works, return
				if (world.renameTo(new File(worldsDir + name))) {
					Logging.GAMEHANDLER.debug("Renaming world {} to new name: {}", world, name);
					WorldSelectDisplay.updateWorlds();
				} else {
					Logging.GAMEHANDLER.error("Rename failed in WorldEditDisplay.");
				}

				Sound.play("confirm");
				updateWorlds();
				updateEntries();
				if (WorldSelectDisplay.getWorldNames().size() > 0) {
					Game.exitDisplay();
				} else {
					Game.exitDisplay(3); // Exiting to title display.
					Game.setDisplay(new WorldGenDisplay());
				}

				return true;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 0), entries.toArray(new ListEntry[0])));
		} else if (input.getMappedKey("SHIFT-D").isClicked() || input.leftTriggerPressed() && input.rightTriggerPressed()) {
			ArrayList<ListEntry> entries = new ArrayList<>();
			entries.addAll(Arrays.asList(StringEntry.useLines(Color.RED, Localization.getLocalized("minicraft.displays.world_select.popups.display.delete",
				Color.toStringCode(Color.tint(Color.RED, 1, true)), worldNames.get(menus[0].getSelection()),
				Color.RED_CODE))
			));

			entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
				Localization.getLocalized("minicraft.displays.world_select.popups.display.confirm", Game.input.getMapping("select")),
				Localization.getLocalized("minicraft.displays.world_select.popups.display.cancel", Game.input.getMapping("exit"))
			)));

			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
				// The location of the world folder on the disk.
				File world = new File(worldsDir + worldNames.get(menus[0].getSelection()));

				// Do the action.
				Logging.GAMEHANDLER.debug("Deleting world: " + world);
				File[] list = world.listFiles();
				for (File file : list) {
					file.delete();
				}
				world.delete();

				Sound.play("confirm");
				updateWorlds();
				updateEntries();
				if (WorldSelectDisplay.getWorldNames().size() > 0) {
					Game.exitDisplay();
					if (menus[0].getSelection() >= worldNames.size()) {
						menus[0].setSelection(worldNames.size() - 1);
					}
				} else {
					Game.exitDisplay(3); // Exiting to title display.
					Game.setDisplay(new WorldGenDisplay());
				}

				return true;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 0), entries.toArray(new ListEntry[0])));
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		int sel = menus[0].getSelection();
		if (sel >= 0 && sel < worldVersions.size()) {
			Version version = worldVersions.get(sel);
			int col = Color.WHITE;
			if (version.compareTo(Game.VERSION) > 0) {
				col = Color.RED;
				Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.display.world_too_new"), screen, Font.textHeight() * 5, col);
			}
			Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.display.world_version", (version.compareTo(new Version("1.9.2")) <= 0 ? "~" : "") + version), screen, Font.textHeight() * 7 / 2, col);
		}

		Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.display.help.0", Game.input.getMapping("select")), screen, Screen.h - 60, Color.GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.display.help.1", Game.input.getMapping("exit")), screen, Screen.h - 40, Color.GRAY);

		Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.display.help.2", Game.input.selectMapping("SHIFT-C", "LEFTBUMPER")), screen, Screen.h - 24, Color.BLUE);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.display.help.3", Game.input.selectMapping("SHIFT-R", "RIGHTBUMPER")), screen, Screen.h - 16, Color.GREEN);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.display.help.4", Game.input.selectMapping("SHIFT-D", "LEFTRIGHTTRIGGER")), screen, Screen.h - 8, Color.RED);

		Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.select_world"), screen, 0, Color.WHITE);
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
		File[] worlds = worldSavesFolder.listFiles();

		if (worlds == null) {
			Logging.GAMEHANDLER.error("Game location file folder is null, somehow...");
			return;
		}

		worldNames.clear();
		worldVersions.clear();

		// Check if there are no files in folder.
		if (worlds.length == 0) {
			Logging.GAMEHANDLER.debug("No worlds in folder. Won't bother loading.");
			return;
		}

		// Iterate between every file in worlds.
		for (File file : worlds) {
			if (file.isDirectory()) {
				String path = worldsDir + file.getName() + "/";
				File folder2 = new File(path);
				folder2.mkdirs();
				String[] files = folder2.list();
				if (files != null && files.length > 0 && Arrays.stream(files).anyMatch(f -> f.endsWith(Save.extension))) {
					String name = file.getName();
					worldNames.add(name);
					worldVersions.add(new Load(name, false).getWorldVersion());
				}
			}
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

	public static ArrayList<String> getWorldNames() {
		return worldNames;
	}
}

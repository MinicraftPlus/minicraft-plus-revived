package minicraft.screen;

import java.io.File;
import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.saveload.Version;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.WorldEditDisplay.Action;
import org.tinylog.Logger;

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
		if (parent instanceof WorldEditDisplay && parent.getParent() != null) {
			// this should get original parent when World Select Display
			// changed to World Edit Display
			super.init(parent.getParent().getParent());
		} else {
			super.init(parent);
		}

		worldName = "";
		loadedWorld = true;

		// Update world list
		updateWorlds();

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

		for (Action a : Action.values()) {
			if (input.getKey(a.key).clicked) {
				Game.setDisplay(new WorldEditDisplay(a));
				break;
			}
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
			Font.drawCentered(Localization.getLocalized("World Version:") + " " + (version.compareTo(new Version("1.9.2")) <= 0 ? "~" : "") + version, screen, Font.textHeight() * 7/2, col);
		}

		Font.drawCentered(Game.input.getMapping("select") + Localization.getLocalized(" to confirm"), screen, Screen.h - 60, Color.GRAY);
		Font.drawCentered(Game.input.getMapping("exit") + Localization.getLocalized(" to return"), screen, Screen.h - 40, Color.GRAY);

		int y = Screen.h - Font.textHeight() * Action.values().length;
		for (Action a : Action.values()) {
			Font.drawCentered(a.key + Localization.getLocalized(" to " + a), screen, y, a.color);
			y += Font.textHeight();
		}

		Font.drawCentered(Localization.getLocalized("minicraft.displays.world_select.select_world"), screen, 0, Color.WHITE);
	}

	public static void updateWorlds() {
		Logger.debug("Updating worlds list.");

		// Get folder containing the worlds and load them.
		File worldSavesFolder = new File(worldsDir);

		// Try to create the saves folder if it doesn't exist.
		if (worldSavesFolder.mkdirs()) {
			Logger.trace("World save folder created.");
		}

		// Get all the files (worlds) in the folder.
		File[] worlds = worldSavesFolder.listFiles();

		if (worlds == null) {
			Logger.error("Game location file folder is null, somehow...");
			return;
		}

		worldNames.clear();
		worldVersions.clear();

		// Check if there are no files in folder.
		if (worlds.length == 0) {
			Logger.debug("No worlds in folder. Won't bother loading.");
			return;
		}

		// Iterate between every file in worlds.
		for (File file : worlds) {
			if (file.isDirectory()) {
				String path = worldsDir + file.getName() + "/";
				File folder2 = new File(path);
				folder2.mkdirs();
				String[] files = folder2.list();
				if (files != null && files.length > 0 && files[0].endsWith(Save.extension)) {
					String name = file.getName();
					worldNames.add(name);
					worldVersions.add(new Load(name, false).getWorldVersion());
				}
			}
		}
	}

	public static String getWorldName() { return worldName; }
	public static void setWorldName(String world, boolean loaded) {
		worldName = world;
		loadedWorld = loaded;
	}

	public static boolean hasLoadedWorld() { return loadedWorld; }

	public static ArrayList<String> getWorldNames() { return worldNames; }
}

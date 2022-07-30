package minicraft.screen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import minicraft.core.io.FileHandler;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.util.Logging;

import org.jetbrains.annotations.Nullable;

public class ResourcePackDisplay extends Display {

	private static final String[] SHEET_NAMES = new String[] { "items.png", "tiles.png", "entities.png", "gui.png" };
	private static final int SHEET_DIMENSIONS = 256;

	private static final String DEFAULT_RESOURCE_PACK = "Default";
	private static final File FOLDER_LOCATION = new File(FileHandler.getSystemGameDir() + "/" + FileHandler.getLocalGameDir() + "/resourcepacks");

	private static String loadedPack = DEFAULT_RESOURCE_PACK;

	private static List<ListEntry> getPacksAsEntries() {
		List<ListEntry> resourceList = new ArrayList<>();
		resourceList.add(new SelectEntry(ResourcePackDisplay.DEFAULT_RESOURCE_PACK, Game::exitDisplay, false));

		// Generate resource packs folder
		if (FOLDER_LOCATION.mkdirs()) {
			Logging.RESOURCEHANDLER_RESOURCEPACK.info("Created resource packs folder at {}.", FOLDER_LOCATION);
		}

		// Read and add the .zip file to the resource pack list.
		for (String fileName : Objects.requireNonNull(FOLDER_LOCATION.list())) {
			// Only accept files ending with .zip.
			if (fileName.endsWith(".zip")) {
				resourceList.add(new SelectEntry(fileName, Game::exitDisplay, false));
			}
		}

		return resourceList;
	}

	public ResourcePackDisplay() {
		super(true, true,
				new Menu.Builder(false, 2, RelPos.CENTER, getPacksAsEntries())
				.setSize(48, 64)
				.setPositioning(new Point(Screen.w/2, Screen.h*3/5), RelPos.CENTER)
				.createMenu());

		ListEntry[] ent = menus[0].getEntries();
		for (int i = 0; i < ent.length; i++) {
			if (ent[i].toString().equals(loadedPack)) {
				menus[0].setSelection(i);
			}
		}
	}

	@Override
	public void onExit() {
		updateResourcePack();
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Title
		Font.drawCentered(Localization.getLocalized("Resource Packs"), screen, 16, Color.WHITE);

		// Info text at the bottom.
		Font.drawCentered("Use "+ Game.input.getMapping("cursor-down") + " and " + Game.input.getMapping("cursor-up") + " to move.", screen, Screen.h - 17, Color.DARK_GRAY);
		Font.drawCentered(Game.input.getMapping("SELECT") + " to select.", screen, Screen.h - 9, Color.DARK_GRAY);

		// Does not work as intended because the sprite sheets aren't updated when changing selection.
		int h = 2;
		int w = 15;
		int xo = (Screen.w - w * 8) / 2;
		int yo = 28;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				// Texture pack logo
				screen.render(xo + x * 8, yo + y * 8, x + y * 32, 0, 3);
			}
		}
	}

	public void initResourcePack() {
		if (!loadedPack.equals(DEFAULT_RESOURCE_PACK)) {
			ZipFile zipFile;
			try {
				zipFile = new ZipFile(new File(FOLDER_LOCATION, loadedPack));
			} catch (IOException e) {
				e.printStackTrace();
				Logging.RESOURCEHANDLER_RESOURCEPACK.error("Could not load resource pack zip at {}.", FOLDER_LOCATION + "/" + loadedPack);
				return;
			}

			updateSheets(zipFile);
			updateLocalization(zipFile);
		}
	}

	private void updateResourcePack() {
		loadedPack = Objects.requireNonNull(menus[0].getCurEntry()).toString();

		ZipFile zipFile = null;
		if (!loadedPack.equals(DEFAULT_RESOURCE_PACK)) {
			try {
				zipFile = new ZipFile(new File(FOLDER_LOCATION, loadedPack));
			} catch (IOException e) {
				e.printStackTrace();
				Logging.RESOURCEHANDLER_RESOURCEPACK.error("Could not load resource pack zip at {}.", FOLDER_LOCATION + "/" + loadedPack);
				return;
			}
		}

		updateSheets(zipFile);
		updateLocalization(zipFile);
	}

	private void updateSheets(@Nullable ZipFile zipFile) {
		try {
			// Load default sprite sheet.
			SpriteSheet[] sheets = Renderer.loadDefaultSpriteSheets();

			if (zipFile != null) {
				try {
					HashMap<String, HashMap<String, ZipEntry>> resources = getPackFromZip(zipFile);

					// Load textures
					HashMap<String, ZipEntry> textures = resources.get("textures");

					// Load default sheets instead if there aren't any sheets to load.
					if (textures == null || textures.isEmpty()) {
						Renderer.screen.setSheets(sheets[0], sheets[1], sheets[2], sheets[3]);
						return;
					}

					for (int i = 0; i < SHEET_NAMES.length; i++) {
						ZipEntry entry = textures.get(SHEET_NAMES[i]);
						if (entry != null) {
							try (InputStream inputEntry = zipFile.getInputStream(entry)) {
								SpriteSheet sheet = new SpriteSheet(ImageIO.read(inputEntry));

								// Check if sheet has the correct dimensions.
								if (sheet.width == SHEET_DIMENSIONS && sheet.height == SHEET_DIMENSIONS) {
									sheets[i] = sheet;
								} else {
									Logging.RESOURCEHANDLER_RESOURCEPACK.error("Sheet with name {} has wrong dimensions. Should be {}px in both directions.", SHEET_NAMES[i], SHEET_DIMENSIONS);
									return;
								}
							} catch (IOException e) {
								e.printStackTrace();
								Logging.RESOURCEHANDLER_RESOURCEPACK.error("Loading sheet {} failed. Aborting.", SHEET_NAMES[i]);
								return;
							}
						} else {
							Logging.RESOURCEHANDLER_RESOURCEPACK.debug("Couldn't load sheet {}, ignoring.", SHEET_NAMES[i]);
						}
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Logging.RESOURCEHANDLER_RESOURCEPACK.error("Could not load resource pack with name {}.", zipFile.getName());
					return;
				} catch (NullPointerException e) {
					e.printStackTrace();
					return;
				}
			}

			Renderer.screen.setSheets(sheets[0], sheets[1], sheets[2], sheets[3]);
		} catch(NullPointerException e) {
			e.printStackTrace();
			Logging.RESOURCEHANDLER_RESOURCEPACK.error("Changing resource pack failed.");
			return;
		}

		Logging.RESOURCEHANDLER_RESOURCEPACK.info("Changed resource pack.");
	}

	private void updateLocalization(@Nullable ZipFile zipFile) {
		// Reload all hard-coded loc-files. (also clears old custom loc)
		Localization.reloadLocalizationFiles();

		// Load the custom loc as long as this isn't the default pack.
		if (zipFile != null) {
			ArrayList<String> paths = new ArrayList<>();
			HashMap<String, HashMap<String, ZipEntry>> resources = getPackFromZip(zipFile);
			if (resources.containsKey("localization"))
				for (Entry<String, ZipEntry> entry : resources.get("localization").entrySet()) {
					if (entry.getKey().endsWith(".json")) {
						paths.add(loadedPack + "/" + entry.getKey());
					}
				}

			Localization.updateLocalizationFiles(paths.toArray(new String[0]));
		}
	}

	// TODO: Make resource packs support sound
	private void updateSounds() { }

	/**
	 * Get all the resources in a resource pack in a hashmap.
	 *
	 * Example folder structure:
	 * 	- textures
	 * 		- items.png
	 * 		- gui.png
	 * 		- entities.png
	 * 		- tiles.png
	 * 	- localization
	 * 		- english_en-us.json
	 * 	- sound
	 * 		- bossdeath.wav
	 *
	 * Gets a hashmap containing several hashmaps with all the files inside.
	 *
	 * Example getter:
	 * resources.get("textures").get("tiles.png")
	 * @param zipFile The resource pack .zip
	 */
	public static HashMap<String, HashMap<String, ZipEntry>> getPackFromZip(ZipFile zipFile) {
		HashMap<String, HashMap<String, ZipEntry>> resources = new HashMap<>();

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();

			String[] path = entry.getName().split("/");

			// Only allow two levels of folders.
			if (path.length <= 2) {
				// Check if entry is a folder. If it is, add it to the first map, if not, add it to the second map.
				String[] validNames = { "textures", "localization", "sound" };
				if (entry.isDirectory()) {
					for (String name : validNames) {
						if (path[0].equals(name)) {
							resources.put(path[0], new HashMap<>());
						}
					}
				} else {
					// If it is a file in the root folder, ignore it.
					if (path.length == 1) continue;

					HashMap<String, ZipEntry> directory = resources.get(path[0]);
					if (directory == null) {
						// If it is not exist, create it.
						for (String name : validNames) {
							if (path[0].equals(name)) {
								resources.put(path[0], new HashMap<>());
							}
						}
						directory = resources.get(path[0]);
						if (directory == null) continue;
					};

					String[] validSuffixes = { ".json", ".wav", ".png" };
					for (String suffix : validSuffixes) {
						if (path[1].endsWith(suffix)) {
							directory.put(path[1], entry);
						}
					}
				}
			}
		}

		return resources;
	}

	public static File getFolderLocation() {
		return FOLDER_LOCATION;
	}

	public void setLoadedPack(String name) {
		ListEntry[] entries = menus[0].getEntries();
		for (ListEntry entry : entries) {
			// If provided pack exists in list, set it.
			if (entry.toString().equals(name)) {
				loadedPack = name;
			}
		}
	}

	public static String getLoadedPack() {
		return loadedPack;
	}
}

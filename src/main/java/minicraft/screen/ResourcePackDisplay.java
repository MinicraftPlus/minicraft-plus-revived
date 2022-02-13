package minicraft.screen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import minicraft.core.FileHandler;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

public class ResourcePackDisplay extends Display {

	private static final String DEFAULT_RESOURCE_PACK = "Default"; // Default texture
	private static final String[] ENTRY_NAMES = new String[] { "items.png", "tiles.png", "entities.png", "gui.png" }; // Spritesheets
	private static final File LOCATION = new File(FileHandler.getSystemGameDir() + "/" + FileHandler.getLocalGameDir() + "/resourcepacks");

	private static String loadedPack = DEFAULT_RESOURCE_PACK;

	private static List<ListEntry> getPacksAsEntries() {
		List<ListEntry> resourceList = new ArrayList<>();
		resourceList.add(new SelectEntry(ResourcePackDisplay.DEFAULT_RESOURCE_PACK, Game::exitMenu, false));

		// Generate resource packs folder
		if (LOCATION.mkdirs()) {
			Logger.info("Created resource packs folder at {}.", LOCATION);
		}

		// Read and add the .zip file to the resource pack list.
		for (String fileName : Objects.requireNonNull(LOCATION.list())) {
			// Only accept files ending with .zip.
			if (fileName.endsWith(".zip")) {
				resourceList.add(new SelectEntry(fileName, Game::exitMenu, false));
			}
		}

		return resourceList;
	}
	
	public ResourcePackDisplay() {
		super(true, true,
				new Menu.Builder(false, 2, RelPos.CENTER, getPacksAsEntries()).setSize(48, 64).createMenu());
	}

	@Override
	public void onExit() {
		super.onExit();
		updateResourcePack();
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Title
		Font.drawCentered(Localization.getLocalized("Resource Packs"), screen, Screen.h - 180, Color.WHITE);

		// Info text at the bottom.
		Font.drawCentered("Use "+ Game.input.getMapping("cursor-down") + " and " + Game.input.getMapping("cursor-up") + " to move.", screen, Screen.h - 17, Color.DARK_GRAY);
		Font.drawCentered(Game.input.getMapping("SELECT") + " to select.", screen, Screen.h - 9, Color.DARK_GRAY);

		/* Does not work as intended because the sprite sheets aren't updated when changing selection.
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
		 */

	    /* Unnecessary for now...
	     * This part is used to test the change of textures
	     * in some future it may be useful
	     *
	     *screen.render(24, 48, 134, Color.get(-1, 110, 330, 550), 0);
	     *screen.render(48, 48, 136, Color.get(-1, 110, 330, 550), 0);
	     *screen.render(72, 48, 164, Color.get(-1, 100, 321, 45), 0);
	     *screen.render(96, 48, 384, Color.get(0, 200, 500, 533), 0);
	     *screen.render(120, 48, 135, Color.get(-1, 100, 320, 430), 0);
	     */
	}

	private void updateResourcePack() {
		loadedPack = Objects.requireNonNull(menus[0].getCurEntry()).toString();

		ZipFile zipFile = null;
		if (!loadedPack.equals(DEFAULT_RESOURCE_PACK)) {
			try {
				zipFile = new ZipFile(new File(LOCATION, loadedPack));
			} catch (IOException e) {
				Logger.error("Could not load resource pack zip at {}.", LOCATION);
				return;
			}
		}

		updateSheets(zipFile);
		updateLocalization(zipFile);
	}

	private void updateSheets(@Nullable ZipFile zipFile) {
		try {
			SpriteSheet[] sheets = new SpriteSheet[ResourcePackDisplay.ENTRY_NAMES.length];

			if (zipFile == null) {
				// Load default sprite sheet.
				sheets = Renderer.loadDefaultSpriteSheets();
			} else {
				try {
					HashMap<String, HashMap<String, ZipEntry>> resources = getPackFromZip(zipFile);

					// Load textures
					HashMap<String, ZipEntry> textures = resources.get("textures");
					for (int i = 0; i < ResourcePackDisplay.ENTRY_NAMES.length; i++) {
						ZipEntry entry = textures.get(ResourcePackDisplay.ENTRY_NAMES[i]);
						if (entry != null) {
							try (InputStream inputEntry = zipFile.getInputStream(entry)) {
								sheets[i] = new SpriteSheet(ImageIO.read(inputEntry));
							} catch (IOException e) {
								e.printStackTrace();
								Logger.error("Loading sheet {} failed. Aborting.", ResourcePackDisplay.ENTRY_NAMES[i]);
								return;
							}
						} else {
							Logger.debug("Couldn't load sheet {}, ignoring.", ResourcePackDisplay.ENTRY_NAMES[i]);
						}
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Logger.error("Could not load resource pack with name {}.", zipFile.getName());
					return;
				} catch (NullPointerException e) {
					e.printStackTrace();
					return;
				}
			}

			Renderer.screen.setSheets(sheets[0], sheets[1], sheets[2], sheets[3]);
		} catch(NullPointerException e) {
			e.printStackTrace();
			Logger.error("Changing resource pack failed.");
			return;
		}

		Logger.info("Changed resource pack.");
	}

	private void updateLocalization(@Nullable ZipFile zipFile) {
		// Reload all hard-coded loc-files. (also clears old custom loc)
		Localization.reloadLanguages();

		// Load the custom loc as long as this isn't the default pack.
		if (zipFile != null) {
			Localization.getLanguagesFromResourcePack(zipFile);
		}

		// Update the loaded loc.
		Localization.updateLanguage();
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
	public static HashMap<String, HashMap<String, ZipEntry>> getPackFromZip(ZipFile zipFile){
		HashMap<String, HashMap<String, ZipEntry>> resources = new HashMap<>();

		resources.put("textures", new HashMap<>());

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();

			Pattern pattern = Pattern.compile("/(.*?)/");
			Matcher matcher = pattern.matcher(entry.getName());
			//if (!matcher.find()) {
			//	continue;
			//}

			if (entry.isDirectory()) {
				resources.put(matcher.group(1), new HashMap<>());
			} else {
				HashMap<String, ZipEntry> directory = resources.get(matcher.group(1));

				// If this zip file has no folder as its first entry, assume it is an old texture pack.
				if (directory == null) {
					directory = resources.get("textures");
				}

				String[] validSuffixes = { ".json", ".wav", ".png" };
				for (String suffix : validSuffixes) {
					if (entry.getName().endsWith(suffix)) {
						String[] path = entry.getName().split("/");
						String fileName = path[path.length - 1];
						directory.put(fileName, entry);
					}
				}
			}
		}

		return resources;
	}

	public static File getLocation() {
		return LOCATION;
	}

	public static String getLoadedPack() {
		return loadedPack;
	}
}

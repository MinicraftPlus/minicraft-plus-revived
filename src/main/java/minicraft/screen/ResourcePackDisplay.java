package minicraft.screen;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import minicraft.core.io.FileHandler;
import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.saveload.Load;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.util.Logging;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public class ResourcePackDisplay extends Display {
	/* Resource Pack
	 * Current complete structure of resource packs:
	 * <root>
	 * 	├──	pack.json
	 * 	└──	assets
	 * 		├──	textures
	 * 		│	├──	entity
	 * 		│	│	└──	<entity_name>.png
	 * 		│	├──	item
	 * 		│	│	└──	<item_name>.png
	 * 		│	├──	tile
	 * 		│	│	└──	<tile_name>.png
	 * 		│	└──	gui
	 * 		│		├──	font.png
	 * 		│		├──	hud.png
	 * 		│		└──	title.png
	 * 		├──	localization
	 * 		│	└──	<name>_<locale>.json
	 * 		├──	sound
	 * 		│	└──	<name>.wav
	 * 		└──	[Not planned]
	 *
	 * pack.json
	 * ├──	(name) String
	 * ├──	(description) String
	 * └──	pack_format int
	 */


	private static final ArrayList<ResourcePack> resourcePacks = new ArrayList<>();

	private static final String[] SHEET_NAMES = new String[] { "items.png", "tiles.png", "entities.png", "gui.png" };
	private static final int SHEET_DIMENSIONS = 256;

	private static final File FOLDER_LOCATION = new File(FileHandler.getSystemGameDir() + "/" + FileHandler.getLocalGameDir() + "/resourcepacks");

	private static final ResourcePack defaultPack; // Used to check if the resource pack default.
	private static ArrayList<ResourcePack> loadedPacks;

	private WatcherThread fileWatcher;

	static {
		// Add the default pack.
		defaultPack = Objects.requireNonNull(loadPackMetadata(new File(Game.class.getResource("/resources").getFile())));
		loadedPacks.add(defaultPack);
	}

	private static List<ListEntry> getPacksAsEntries() {
		List<ListEntry> resourceList = new ArrayList<>();
		// resourceList.add(new SelectEntry(ResourcePackDisplay.DEFAULT_RESOURCE_PACK, Game::exitDisplay, false));


		return resourceList;
	}

	public ResourcePackDisplay() {
		super(true, true,
				new Menu.Builder(false, 2, RelPos.CENTER, getPacksAsEntries())
				.setSize(48, 64)
				.setPositioning(new Point(Screen.w/2, Screen.h*3/5), RelPos.CENTER)
				.createMenu());

		fileWatcher = new WatcherThread();

		// TODO
		// ListEntry[] ent = menus[0].getEntries();
		// for (int i = 0; i < ent.length; i++) {
		// 	if (ent[i].toString().equals(loadedPack)) {
		// 		menus[0].setSelection(i);
		// 	}
		// }
	}

	private static class WatcherThread extends Thread implements Closeable {
		private WatchService watcher;
		private volatile boolean running;

		WatcherThread() {
			super("Resource Pack File Watcher");
			try {
				watcher = FileSystems.getDefault().newWatchService();
				FOLDER_LOCATION.toPath().register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			} catch (IOException e) {
				CrashHandler.crashHandle(e, new CrashHandler.ErrorInfo("Unable to Watch File", CrashHandler.ErrorInfo.ErrorType.UNHANDLEABLE, "Unable to create file water service."));
			}

			start();
		}

		@Override
		public void run() {
			while (running) {
				try {
					for (WatchEvent<?> event : watcher.take().pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.OVERFLOW)
							continue;

						@SuppressWarnings("unchecked")
						WatchEvent<Path> ev = (WatchEvent<Path>) event;
        				Path filename = ev.context();
						try {
							refreshResourcePacks(List.of(FOLDER_LOCATION.toPath().resolve(filename).toFile().toURI().toURL()));
						} catch (IOException e) {
							e.printStackTrace();
							continue;
						}
					}
				} catch (InterruptedException e) {
					return;
				}
			}

			Logging.RESOURCEHANDLER_RESOURCEPACK.trace("File watcher terminated.");
		}

		@Override
		public void close() {
			running = false;
		}
	}

	@Override
	public void onExit() {
		resourcePacks.removeIf(p -> !loadedPacks.contains(p)); // Releases unloaded packs.
		fileWatcher.close(); // Removes watcher.
		reloadResources();
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Title
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.title"), screen, 16, Color.WHITE);

		// Info text at the bottom.
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.move", Game.input.getMapping("cursor-down"), Game.input.getMapping("cursor-up")), screen, Screen.h - 17, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.select", Game.input.getMapping("SELECT")), screen, Screen.h - 9, Color.DARK_GRAY);

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

	public static class ResourcePack {
		private final URL packRoot;
		/** 0 - before 2.2.0; 1 - 2.2.0-latest */
		private final int packFormat;
		private final String name;
		private final String description;

		private ResourcePack(URL packRoot, int packFormat, String name, String desc) {
			this.packRoot = packRoot;
			this.packFormat = packFormat;
			this.name = name;
			this.description = desc;
		}

		private void refreshPack() {
			// TODO
		}
	}

	public static ResourcePack loadPackMetadata(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				try {
					JSONObject meta = new JSONObject(Load.loadFromFile(file.toPath().resolve("/pack.json").toString(), true));
					return new ResourcePack(file.toURI().toURL(), meta.getInt("pack_format"),
						meta.optString("name", file.getName()), meta.optString("description", "No description"));
				} catch (JSONException | IOException e) {
					CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Cannot Load Resource Pack",
						CrashHandler.ErrorInfo.ErrorType.REPORT, String.format("Unable to load directory resource pack: %s.", file.getPath())));
				}
			} else {
				try (ZipFile zip = new ZipFile(file)) {
					JSONObject meta = new JSONObject(Load.loadFromFile(zip.getEntry("/pack.json").getName(), true));
					return new ResourcePack(file.toURI().toURL(),
						meta.getInt("pack_format"), meta.optString("name", file.getName()), meta.optString("description", "No description"));
				} catch (IOException e) {
					CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Cannot Load Resource Pack",
						CrashHandler.ErrorInfo.ErrorType.REPORT, String.format("Unable to load zipped resource pack: %s.", file.getPath())));
				}
			}
		}

		return null;
	}

	public static void initPacks() {
		// Generate resource packs folder
		if (FOLDER_LOCATION.mkdirs()) {
			Logging.RESOURCEHANDLER_RESOURCEPACK.info("Created resource packs folder at {}.", FOLDER_LOCATION);
		}

		ArrayList<URL> urls = new ArrayList<>();
		// Read and add the .zip file to the resource pack list. Only accept files ending with .zip or directory.
		for (File file : Objects.requireNonNull(FOLDER_LOCATION.listFiles((dur, name) ->  name.endsWith(".zip") || dur.isDirectory()))) {
			try {
				urls.add(file.toPath().toUri().toURL());
			} catch (MalformedURLException e) {
				CrashHandler.errorHandle(e);
			}
		}

		refreshResourcePacks(urls);
	}

	private static ResourcePack findPackByURL(URL url) {
		for (ResourcePack pack : resourcePacks) {
			if (pack.packRoot.equals(url)) {
				return pack;
			}
		}

		return null;
	}

	public static void refreshResourcePacks(List<URL> urls) {
		for (URL url : urls) {
			ResourcePack pack = findPackByURL(url);
			if (pack != null) { // Refresh the current.
				pack.refreshPack();
			} else { // Add new pack.
				pack = loadPackMetadata(new File(url.getFile()));
				if (pack != null) {
					resourcePacks.add(pack);
				}
			}
		}

		resourcePacks.sort((p1, p2) -> p1.name.compareTo(p2.name));
	}

	// public void initResourcePack() {
	// 	if (!loadedPack.equals(DEFAULT_RESOURCE_PACK)) {
	// 		ZipFile zipFile;
	// 		try {
	// 			zipFile = new ZipFile(new File(FOLDER_LOCATION, loadedPack));
	// 		} catch (IOException e) {
	// 			e.printStackTrace();
	// 			Logging.RESOURCEHANDLER_RESOURCEPACK.error("Could not load resource pack zip at {}.", FOLDER_LOCATION + "/" + loadedPack);
	// 			return;
	// 		}

	// 		updateSheets(zipFile);
	// 		updateLocalization(zipFile);
	// 	}
	// }

	// private void updateResourcePack() {
	// 	loadedPack = Objects.requireNonNull(menus[0].getCurEntry()).toString();

	// 	ZipFile zipFile = null;
	// 	if (!loadedPack.equals(DEFAULT_RESOURCE_PACK)) {
	// 		try {
	// 			zipFile = new ZipFile(new File(FOLDER_LOCATION, loadedPack));
	// 		} catch (IOException e) {
	// 			e.printStackTrace();
	// 			Logging.RESOURCEHANDLER_RESOURCEPACK.error("Could not load resource pack zip at {}.", FOLDER_LOCATION + "/" + loadedPack);
	// 			return;
	// 		}
	// 	}

	// 	updateSheets(zipFile);
	// 	updateLocalization(zipFile);
	// }

	private void reloadResources() {

	}

	private void updateSheets(@Nullable ZipFile zipFile) {
		try {
			// Load default sprite sheet.
			SpriteSheet[] sheets = Renderer.loadDefaultSkinSheet();

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
						// paths.add(loadedPack + "/" + entry.getKey());
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

	// public void setLoadedPack(String name) {
	// 	ListEntry[] entries = menus[0].getEntries();
	// 	for (ListEntry entry : entries) {
	// 		// If provided pack exists in list, set it.
	// 		if (entry.toString().equals(name)) {
	// 			loadedPack = name;
	// 		}
	// 	}
	// }

	// public static String getLoadedPack() {
	// 	return loadedPack;
	// }
}

package minicraft.screen;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
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
import minicraft.gfx.SpriteLinker.SpriteType;
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


	private static final ArrayList<ResourcePack> resourcePacks = new ArrayList<>(); // Packs that are not loaded.
	private static final File FOLDER_LOCATION = new File(FileHandler.getSystemGameDir() + "/" + FileHandler.getLocalGameDir() + "/resourcepacks");

	private static final ResourcePack defaultPack; // Used to check if the resource pack default.
	private static final SpriteSheet defaultLogo;
	private static ArrayList<ResourcePack> loadedPacks = new ArrayList<>();
	private static ArrayList<ResourcePack> loadQuery = new ArrayList<>();

	private static final int padding = 10;

	private WatcherThread fileWatcher;
	private ArrayList<ListEntry> entries0;
	private ArrayList<ListEntry> entries1;
	private Menu.Builder builder0;
	private Menu.Builder builder1;

	static {
		// Add the default pack.
		defaultPack = Objects.requireNonNull(loadPackMetadata(Game.class.getProtectionDomain().getCodeSource().getLocation()));
		defaultPack.refreshPack();
		loadedPacks.add(defaultPack);
		try {
			defaultLogo = new SpriteSheet(ImageIO.read(ResourcePackDisplay.class.getResourceAsStream("/resources/default_pack.png")));
		} catch (IOException e) {
			CrashHandler.crashHandle(e);
			throw new RuntimeException();
		}
	}

	public ResourcePackDisplay() {
		super(true, true);

		builder0 = new Menu.Builder(false, 2, RelPos.LEFT)
			.setDisplayLength(8)
			.setPositioning(new Point(0, 60), RelPos.BOTTOM_RIGHT);

		builder1 = new Menu.Builder(false, 2, RelPos.RIGHT)
			.setDisplayLength(8)
			.setPositioning(new Point(Screen.w, 60), RelPos.BOTTOM_LEFT);

		reloadEntries();

		menus = new Menu[] {
			builder0.setEntries(entries0)
				.createMenu(),
			builder1.setEntries(entries1)
				.createMenu()
		};

		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);

		fileWatcher = new WatcherThread();
	}

	@Override
	protected void onSelectionChange(int oldSel, int newSel) {
		super.onSelectionChange(oldSel, newSel);
		if(oldSel == newSel) return; // this also serves as a protection against access to menus[0] when such may not exist.
		int shift = 0;
		if(newSel == 0) shift = padding - menus[0].getBounds().getLeft();
		if(newSel == 1) shift = (Screen.w - padding) - menus[1].getBounds().getRight();
		for(Menu m: menus)
			m.translate(shift, 0);
	}

	private void reloadEntries() {
		entries0.clear();
		for (ResourcePack pack : resourcePacks) {
			entries0.add(new SelectEntry(pack.name, Game::exitDisplay));
		}

		entries1.clear();
		for (ResourcePack pack : loadedPacks) {
			entries1.add(new SelectEntry(pack.name, Game::exitDisplay));
		}
	}

	private void refreshEntries() {
		reloadEntries();
		Menu[] newMenus = new Menu[] {
			builder0.setEntries(entries0)
				.createMenu(),
			builder1.setEntries(entries1)
				.createMenu()
		};

		newMenus[0].setSelection(menus[0].getSelection());
		newMenus[1].setSelection(menus[1].getSelection());

		menus = newMenus;

		menus[selection ^ 1].translate(menus[selection].getBounds().getWidth() + padding, 0);
	}

	private class WatcherThread extends Thread implements Closeable {
		private WatchService watcher;
		private volatile boolean running = true;

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
					ArrayList<URL> urls = new ArrayList<>();
					for (WatchEvent<?> event : watcher.take().pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.OVERFLOW)
							continue;

						@SuppressWarnings("unchecked")
						WatchEvent<Path> ev = (WatchEvent<Path>) event;
        				Path filename = ev.context();
						try {
							urls.add(FOLDER_LOCATION.toPath().resolve(filename).toFile().toURI().toURL());
						} catch (IOException e) {
							e.printStackTrace();
							continue;
						}
					}

					if (urls.size() > 0) {
						refreshResourcePacks(urls);
						refreshEntries();
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
		resourcePacks.clear(); // Releases unloaded packs.
		fileWatcher.close(); // Removes watcher.
		reloadResources();
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Title
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.title"), screen, 6, Color.WHITE);

		// Info text at the bottom.
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.move", Game.input.getMapping("cursor-down"), Game.input.getMapping("cursor-up")), screen, Screen.h - 17, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.select", Game.input.getMapping("SELECT")), screen, Screen.h - 9, Color.DARK_GRAY);


		SpriteSheet logo = selection == 0 ? resourcePacks.get(menus[selection].getSelection()).logo : loadedPacks.get(menus[selection].getSelection()).logo;
		int h = logo.height;
		int w = logo.width;
		int xo = (Screen.w - w * 8) / 2;
		int yo = 28 + (h >= 32 ? -8 : 0);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				// Resource pack logo
				screen.render(xo + x * 8, yo + y * 8, x, y, 0, logo);
			}
		}
	}

	private static class ResourcePack implements Closeable {
		private final URL packRoot;
		/** 0 - before 2.2.0; 1 - 2.2.0-latest */
		private final int packFormat;
		private final String name;
		private final String description;
		private SpriteSheet logo;

		private boolean opened = false;
		private ZipFile zipFile = null;

		private ResourcePack(URL packRoot, int packFormat, String name, String desc) {
			this.packRoot = packRoot;
			this.packFormat = packFormat;
			this.name = name;
			this.description = desc;
		}

		/** This does not include metadata refresh. */
		private void refreshPack() {
			// Refresh pack logo.png.
			try {
				File png = new File(packRoot.getPath()).toPath().resolve("pack.png").toFile();
				if (png.exists()) {
					logo = new SpriteSheet(ImageIO.read(png));

					// Logo size verification.
					int h = logo.height;
					int w = logo.width;
					if (h == 0 || w == 0 || h % 8 != 0 || w % 8 != 0 ||
						h > 32 || w > Screen.w) {
						throw new IOException(String.format("Unacceptable logo size: %s;%s", w, h));
					}
				} else {
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Pack logo not found, loading default logo instead.");
					logo = defaultLogo;
				}

			} catch (IOException e) {
				e.printStackTrace();
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Unable to load logo, loading default logo instead.");
				if (this == defaultPack) {
					try {
						logo = new SpriteSheet(ImageIO.read(getClass().getResourceAsStream("/resources/logo.png")));
					} catch (IOException e1) {
						CrashHandler.crashHandle(e1);
					}
				} else logo = defaultLogo;
			}
		}

		private boolean openStream() {
			try {
				zipFile = new ZipFile(new File(packRoot.toURI()));
				return opened = true;
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
				return opened = false;
			}
		}

		@Override
		public void close() throws IOException {
			if (opened) {
				zipFile.close();
				zipFile = null;
				opened = false;
			}
		}

		private InputStream getResourceAsStream(String path) throws IOException {
			return zipFile.getInputStream(zipFile.getEntry(path));
		}
		private ArrayList<String> getFiles(String path) {
			ArrayList<String> paths = new ArrayList<>();
			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
				ZipEntry entry = e.nextElement();
				Path parent;
				if ((parent = Path.of(entry.getName()).getParent()) != null && parent.equals(Path.of(path))) {
					paths.add(entry.getName());
				}
			}

			return paths;
		}
	}

	public static ResourcePack loadPackMetadata(URL file) {
		try (ZipFile zip = new ZipFile(new File(file.toURI()))) {
			try (InputStream in = zip.getInputStream(zip.getEntry("pack.json"))) {
				JSONObject meta = new JSONObject(new String(in.readAllBytes()));
				return new ResourcePack(file.toURI().toURL(),
					meta.getInt("pack_format"), meta.optString("name", new File(file.toURI()).getName()), meta.optString("description", "No description"));
			}
		} catch (JSONException | IOException | URISyntaxException e) {
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Cannot Load Resource Pack",
				CrashHandler.ErrorInfo.ErrorType.REPORT, String.format("Unable to load resource pack: %s.", file.getPath())));
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
		for (File file : Objects.requireNonNull(FOLDER_LOCATION.listFiles((dur, name) ->  name.endsWith(".zip")))) {
			try {
				urls.add(file.toPath().toUri().toURL());
			} catch (MalformedURLException e) {
				CrashHandler.errorHandle(e);
			}
		}

		refreshResourcePacks(urls);
		reloadResources();
	}

	private static ResourcePack findPackByURL(URL url) {
		for (ResourcePack pack : resourcePacks) {
			if (pack.packRoot.equals(url)) {
				return pack;
			}
		}

		for (ResourcePack pack : loadedPacks) {
			if (pack.packRoot.equals(url)) {
				return pack;
			}
		}

		return null;
	}

	// TODO world-wide resource pack support
	private static void refreshResourcePacks(List<URL> urls) {
		for (URL url : urls) {
			ResourcePack pack = findPackByURL(url);
			if (pack != null) { // Refresh the current.
				pack.refreshPack();
			} else { // Add new pack.
				pack = loadPackMetadata(url);
				pack.refreshPack();
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

	private static void reloadResources() {
		// TODO
		loadQuery.clear();
		loadQuery.addAll(loadedPacks);
		Collections.reverse(loadQuery);
		Renderer.spriteLinker.resetSprites();
		for (ResourcePack pack : loadQuery) {
			if (pack.openStream()) {
				try {
					loadTextrues(pack);
					pack.close();
				} catch (IOException e) {
					CrashHandler.errorHandle(e);
				}
			}
		}

		Renderer.spriteLinker.updateLinkedSheets();
	}

	private static void loadTextrues(ResourcePack pack) throws IOException {
		for (String t : pack.getFiles("assets/textures/")) {
			switch (t) {
				case "assets/textures/entity/": loadTextures(pack, SpriteType.Entity); break;
				case "assets/textures/gui/": loadTextures(pack, SpriteType.Gui); break;
				case "assets/textures/item/": loadTextures(pack, SpriteType.Item); break;
				case "assets/textures/tile/": loadTextures(pack, SpriteType.Tile); break;
			}
		}
	}

	private static void loadTextures(ResourcePack pack, SpriteType type) throws IOException {
		String path = "assets/textures/";
		switch (type) {
			case Entity: path += "entity/"; break;
			case Gui: path += "gui/"; break;
			case Item: path += "item/"; break;
			case Tile: path += "tile/"; break;
		}

		for (String p : pack.getFiles(path)) {
			Renderer.spriteLinker.setSprite(type, p.substring(path.length(), p.length() - 4), new SpriteSheet(ImageIO.read(pack.getResourceAsStream(p))));
		}
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

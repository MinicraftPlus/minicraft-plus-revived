package minicraft.screen;

import java.io.BufferedInputStream;
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
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import minicraft.core.io.ControllerHandler;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.saveload.Save;
import minicraft.screen.entry.ArrayEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.util.BookData;
import minicraft.util.Logging;

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
	@SuppressWarnings("unused")
	private static final int VERSION = 1;

	private static final ResourcePack defaultPack; // Used to check if the resource pack default.
	private static final SpriteSheet defaultLogo;
	private static ArrayList<ResourcePack> loadedPacks = new ArrayList<>();
	private static ArrayList<ResourcePack> loadQuery = new ArrayList<>();

	private static final int padding = 10;

	private WatcherThread fileWatcher;
	private ArrayList<ListEntry> entries0 = new ArrayList<>();
	private ArrayList<ListEntry> entries1 = new ArrayList<>();
	private Menu.Builder builder0;
	private Menu.Builder builder1;
	private boolean changed = false;

	static {
		// Add the default pack.
		defaultPack = Objects.requireNonNull(loadPackMetadata(Game.class.getProtectionDomain().getCodeSource().getLocation()));
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
		initPacks();

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

		if (menus[1].getBounds().getLeft() - menus[0].getBounds().getRight() < padding)
			menus[1].translate(menus[0].getBounds().getRight() - menus[1].getBounds().getLeft() + padding, 0);

		fileWatcher = new WatcherThread();
	}

	@Override
	protected void onSelectionChange(int oldSel, int newSel) {
		super.onSelectionChange(oldSel, newSel);
		if (oldSel == newSel) return; // this also serves as a protection against access to menus[0] when such may not exist.
		menus[0].translate(-menus[0].getBounds().getLeft(), 0);
		menus[1].translate(Screen.w - menus[1].getBounds().getRight(), 0);
		if (newSel == 0) {
			if (menus[1].getBounds().getLeft() - menus[0].getBounds().getRight() < padding)
				menus[1].translate(menus[0].getBounds().getRight() - menus[1].getBounds().getLeft() + padding, 0);
		} else if (newSel == 1) {
			if (menus[1].getBounds().getLeft() - menus[0].getBounds().getRight() < padding)
				menus[0].translate(-(menus[0].getBounds().getRight() - menus[1].getBounds().getLeft() + padding), 0);
		}
	}

	private void reloadEntries() {
		entries0.clear();
		for (ResourcePack pack : resourcePacks) {
			entries0.add(new SelectEntry(pack.name, () -> Game.setDisplay(new PopupDisplay(null, pack.name, pack.description)), false) {
				@Override
				public int getColor(boolean isSelected) {
					if (selection == 1) return SelectEntry.COL_UNSLCT;
					return super.getColor(isSelected);
				}
			});
		}

		entries1.clear();
		for (ResourcePack pack : loadedPacks) {
			entries1.add(new SelectEntry(pack.name, () -> Game.setDisplay(new PopupDisplay(null, pack.name, pack.description)), false) {
				@Override
				public int getColor(boolean isSelected) {
					if (selection == 0) return SelectEntry.COL_UNSLCT;
					return super.getColor(isSelected);
				}
			});
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
			Logging.RESOURCEHANDLER_RESOURCEPACK.debug("WatcherThread started.");
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
						Logging.RESOURCEHANDLER_RESOURCEPACK.debug("Refreshing resource packs.");
						refreshResourcePacks(urls);
						refreshEntries();
					}
				} catch (InterruptedException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace("File watcher terminated.");
					return;
				}

				if (Thread.interrupted()) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace("File watcher terminated.");
					return;
            	}
			}

			Logging.RESOURCEHANDLER_RESOURCEPACK.trace("File watcher terminated.");
		}

		@Override
		public void close() {
			running = false; // This does no effect...
			interrupt();
		}
	}

	@Override
	public void onExit() {
		resourcePacks.clear(); // Releases unloaded packs.
		fileWatcher.close(); // Removes watcher.
		new Save();
		if (changed) reloadResources();
	}

	@Override
	public void tick(InputHandler input, ControllerHandler controlInput) {
		// Overrides the default tick handler.
		if (input.getKey("right").clicked) {
			if (selection == 0) {
				Sound.play("select");
				onSelectionChange(0, 1);
			}

			return;
		} else if (input.getKey("left").clicked) {
			if (selection == 1) {
				Sound.play("select");
				onSelectionChange(1, 0);
			}

			return;
		} else if (input.getKey("shift-right").clicked) {
			if (selection == 0 && resourcePacks.size() > 0) {
				loadedPacks.add(resourcePacks.remove(menus[0].getSelection()));
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		} else if (input.getKey("shift-left").clicked) {
			if (selection == 1 && loadedPacks.get(menus[1].getSelection()) != defaultPack) {
				resourcePacks.add(loadedPacks.remove(menus[1].getSelection()));
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		} else if (input.getKey("shift-up").clicked) {
			if (selection == 1 && menus[1].getSelection() > 0) {
				if (loadedPacks.get(menus[1].getSelection()) == defaultPack) return; // Default pack remains bottom.
				loadedPacks.add(menus[1].getSelection() - 1, loadedPacks.remove(menus[1].getSelection()));
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		} else if (input.getKey("shift-down").clicked) {
			if (selection == 1 && menus[1].getSelection() < loadedPacks.size() - 1) {
				if (loadedPacks.get(menus[1].getSelection() + 1) == defaultPack) return; // Default pack remains bottom.
				loadedPacks.add(menus[1].getSelection() + 1, loadedPacks.remove(menus[1].getSelection()));
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		}

		super.tick(input, controlInput);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Title
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.title"), screen, 6, Color.WHITE);

		// Info text at the bottom.
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.move", Game.input.getMapping("cursor-down"), Game.input.getMapping("cursor-up")), screen, Screen.h - 17, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.select", Game.input.getMapping("SELECT")), screen, Screen.h - 9, Color.DARK_GRAY);

		ArrayList<ResourcePack> packs = selection == 0 ? resourcePacks : loadedPacks;
		if (packs.size() > 0) {
			@SuppressWarnings("resource")
			SpriteSheet logo = packs.get(menus[selection].getSelection()).logo;
			int h = logo.height / 8;
			int w = logo.width / 8;
			int xo = (Screen.w - logo.width) / 2;
			int yo = 36 - logo.height / 2;

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					// Resource pack logo
					screen.render(xo + x * 8, yo + y * 8, x, y, 0, logo);
				}
			}
		}
	}

	private static class ResourcePack implements Closeable {
		private URL packRoot;

		/** 0 - before 2.2.0; 1 - 2.2.0-latest */
		@SuppressWarnings("unused")
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
			refreshPack();
		}

		/** This does not include metadata refresh. */
		private void refreshPack() {
			// Refresh pack logo.png.
			try {
				openStream();
				InputStream in = getResourceAsStream("pack.png");
				if (in != null) {
					logo = new SpriteSheet(ImageIO.read(in));

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
				close();

			} catch (IOException | NullPointerException e) {
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

		@FunctionalInterface
		private static interface FilesFilter {
			public abstract boolean check(Path path, boolean isDir);
		}

		private ArrayList<String> getFiles(String path, FilesFilter filter) {
			ArrayList<String> paths = new ArrayList<>();
			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
				ZipEntry entry = e.nextElement();
				Path parent;
				if ((parent = Path.of(entry.getName()).getParent()) != null && parent.equals(Path.of(path)) &&
						(filter == null || filter.check(Path.of(entry.getName()), entry.isDirectory()))) {
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
		} catch (NullPointerException e) { // pack.json is missing.
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Resource Pack not Supported",
				CrashHandler.ErrorInfo.ErrorType.HANDLED, String.format("Earlier version formatted resource pack detected: %s.", file.getPath())));
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

	public static void releaseUnloadedPacks() {
		resourcePacks.clear(); // Releases unloaded packs.
	}

	public static void changeDefaultPackURL(URL url) {
		defaultPack.packRoot = url;
	}

	public static void loadResourcePacks(String[] names) {
		for (String name : names) {
			for (ResourcePack pack : new ArrayList<>(resourcePacks)) {
				try {
					if (Path.of(pack.packRoot.toURI()).equals(FOLDER_LOCATION.toPath().resolve(name))) {
						resourcePacks.remove(pack);
						loadedPacks.add(loadedPacks.indexOf(defaultPack), pack);
					}
				} catch (URISyntaxException e) {
					e.printStackTrace();
					Logging.RESOURCEHANDLER_RESOURCEPACK.debug("URL invalid.");
				}
			}
		}
	}

	public static ArrayList<String> getLoadedPacks() {
		ArrayList<String> packs = new ArrayList<>();
		for (ResourcePack pack : loadedPacks) {
			if (pack != defaultPack) {
				try {
					packs.add(new File(pack.packRoot.toURI()).getName());
				} catch (URISyntaxException e) {
					CrashHandler.errorHandle(e);
				}
			}
		}

		return packs;
	}

	@SuppressWarnings("unchecked")
	public static void reloadResources() {
		loadQuery.clear();
		loadQuery.addAll(loadedPacks);
		Collections.reverse(loadQuery);
		Renderer.spriteLinker.resetSprites();
		Localization.resetLocalizations();
		BookData.resetBooks();
		Sound.resetSounds();
		for (ResourcePack pack : loadQuery) {
			if (pack.openStream()) {
				try {
					loadTextures(pack);
					loadLocalization(pack);
					loadBooks(pack);
					loadSounds(pack);
					pack.close();
				} catch (IOException e) {
					CrashHandler.errorHandle(e);
				}
			}
		}

		Renderer.spriteLinker.updateLinkedSheets();
		Localization.loadLanguage();
		((ArrayEntry<Localization.LocaleInformation>) Settings.getEntry("language")).setOptions(Localization.getLocales());
	}

	private static void loadTextures(ResourcePack pack) throws IOException {
		for (String t : pack.getFiles("assets/textures/", null)) {
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

		for (String p : pack.getFiles(path, (p, isDir) -> p.toString().endsWith(".png") && !isDir)) {
			Renderer.spriteLinker.setSprite(type, p.substring(path.length(), p.length() - 4), new SpriteSheet(ImageIO.read(pack.getResourceAsStream(p))));
		}
	}

	private static void loadLocalization(ResourcePack pack) {
		JSONObject langJSON = null;
		try {
			langJSON = new JSONObject(new String(pack.getResourceAsStream("pack.json").readAllBytes())).optJSONObject("language");
		} catch (JSONException | IOException e1) {
			e1.printStackTrace();
		}
		if (langJSON != null) {
			for (String loc : langJSON.keySet()) {
				try {
					Locale locale = Locale.forLanguageTag(loc);
					JSONObject info = langJSON.getJSONObject(loc);
					Localization.addLocale(locale, new Localization.LocaleInformation(locale, info.getString("name"), info.getString("region")));
				} catch (JSONException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.debug(e, "Invalid localization configuration in pack: {}", pack.name);
				}
			}
		}

		for (String f : pack.getFiles("assets/localization/", (path, isDir) -> path.toString().endsWith(".json") && !isDir)) {
			String str = Path.of(f).getFileName().toString();
			try {
				Localization.addLocalization(Locale.forLanguageTag(str.substring(0, str.length() - 5)),
					new String(pack.getResourceAsStream(f).readAllBytes()));
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load localization: {} in pack : {}", f, pack.name);
			}
		}
	}

	private static void loadBooks(ResourcePack pack) {
		for (String path : pack.getFiles("assets/books", (path, isDir) -> path.toString().endsWith(".txt") && !isDir))  {
			try {
				switch (path) {
					case "assets/books/about.txt": BookData.about = BookData.loadBook(new String(pack.getResourceAsStream(path).readAllBytes())); break;
					case "assets/books/credits.txt": BookData.credits = BookData.loadBook(new String(pack.getResourceAsStream(path).readAllBytes())); break;
					case "assets/books/instructions.txt": BookData.instructions = BookData.loadBook(new String(pack.getResourceAsStream(path).readAllBytes())); break;
					case "assets/books/antidous.txt": BookData.antVenomBook = BookData.loadBook(new String(pack.getResourceAsStream(path).readAllBytes())); break;
					case "assets/books/story_guide.txt": BookData.storylineGuide = BookData.loadBook(new String(pack.getResourceAsStream(path).readAllBytes())); break;
				}
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load book: {} in pack : {}", path, pack.name);
			}
		}
	}

	private static void loadSounds(ResourcePack pack) {
		for (String f : pack.getFiles("assets/sound/", (path, isDir) -> path.toString().endsWith(".wav") && !isDir)) {
			String name = Path.of(f).getFileName().toString();
			try {
				Sound.loadSound(name.substring(0, name.length() - 4), new BufferedInputStream(pack.getResourceAsStream(f)), pack.name);
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load audio: {} in pack : {}", f, pack.name);
			}
		}
	}

	public static File getFolderLocation() {
		return FOLDER_LOCATION;
	}
}

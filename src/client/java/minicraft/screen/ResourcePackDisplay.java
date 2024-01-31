package minicraft.screen;

import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.util.BookData;
import minicraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.zip.ZipOutputStream;

public class ResourcePackDisplay extends Display {
	/* Resource Pack
	 * Current complete structure of resource packs:
	 * <root>
	 * 	├──	pack.json
	 * 	├──	pack.png
	 * 	└──	assets TODO Restructure the structure with the new IDS later
	 * 		├──	books
	 * 		│	└──	<name>.txt
	 * 		├──	localization
	 * 		│	└──	<locale>.json
	 * 		├──	sound
	 * 		│	└──	<name>.wav
	 * 		└──	textures
	 * 			├──	entity
	 * 			│	└──	<entity_name>.png
	 * 			├──	item
	 * 			│	└──	<item_name>.png
	 * 			├──	tile
	 * 			│	├──	<tile_name>.png
	 * 			│	└──	[<tile_name>.png.json]
	 * 			└──	gui
	 * 				├──	font.png
	 * 				├──	hud.png
	 * 				└──	title.png
	 *
	 * pack.json
	 * 	├──	(name) String
	 * 	├──	(description) String
	 * 	├──	pack_format int
	 * 	└──	(language object)
	 * 		└──	<locale>
	 * 			├──	name String
	 * 			└──	region String
	 */


	private static final ArrayList<ResourcePack> resourcePacks = new ArrayList<>(); // Packs that are not loaded.
	private static final File FOLDER_LOCATION = new File(FileHandler.gameDir + "/resourcepacks");
	@SuppressWarnings("unused")
	private static final int VERSION = 1;

	private static final ResourcePack defaultPack; // Used to check if the resource pack default.
	private static final MinicraftImage defaultLogo;
	private static ArrayList<ResourcePack> loadedPacks = new ArrayList<>();
	private static ArrayList<ResourcePack> loadQuery = new ArrayList<>();

	private static final int padding = 10;

	private WatcherThread fileWatcher;
	private ArrayList<ListEntry> entries0 = new ArrayList<>();
	private ArrayList<ListEntry> entries1 = new ArrayList<>();
	private Menu.Builder builder0;
	private Menu.Builder builder1;
	private boolean changed = false;

	static { // Initializing the default pack and logo.
		// Add the default pack.
		URL defaultPackURL = Game.class.getProtectionDomain().getCodeSource().getLocation();
		if (Game.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith("/")) { // If the source is a directory.
			try {
				File zip = File.createTempFile("resources", ".zip");
				Logging.RESOURCEHANDLER_RESOURCEPACK.info("Created temp zip file: {}", zip.getAbsolutePath());
				if (zip.exists()) zip.delete(); // Delete if exists.
				try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
					ArrayList<String> assets = FileHandler.listAssets();
					assets.add("pack.json");
					assets.add("pack.png");
					for (String name : assets) { // Copy only assets and pack configuration.
						if (name.startsWith("assets/") || name.equals("pack.json") || name.equals("pack.png")) {
							out.putNextEntry(new ZipEntry(name));
							if (!name.endsWith("/")) {
								int b;
								InputStream stream = Game.class.getResourceAsStream("/" + name);
								while ((b = stream.read()) != -1) // Write per byte.
									out.write(b);
							}

							out.closeEntry();
						}
					}
				} catch (IOException e) {
					CrashHandler.crashHandle(e);
				}

				try {
					defaultPackURL = zip.toURI().toURL();
				} catch (MalformedURLException e) {
					CrashHandler.crashHandle(e);
				}
			} catch (IOException e) {
				CrashHandler.crashHandle(e);
			}
		}

		// Default resources
		defaultPack = Objects.requireNonNull(loadPackMetadata(defaultPackURL));
		loadedPacks.add(defaultPack);
		try {
			defaultLogo = new MinicraftImage(ImageIO.read(ResourcePackDisplay.class.getResourceAsStream("/resources/default_pack.png")));
		} catch (IOException e) {
			CrashHandler.crashHandle(e);
			throw new RuntimeException();
		}
	}

	/**
	 * Initializing the Display.
	 */
	public ResourcePackDisplay() {
		super(true, true);
		initPacks();

		// Left Hand Side
		builder0 = new Menu.Builder(false, 2, RelPos.LEFT)
			.setDisplayLength(8)
			.setPositioning(new Point(0, 60), RelPos.BOTTOM_RIGHT);

		// Right Hand Side
		builder1 = new Menu.Builder(false, 2, RelPos.RIGHT)
			.setDisplayLength(8)
			.setPositioning(new Point(Screen.w, 60), RelPos.BOTTOM_LEFT);

		reloadEntries();

		menus = new Menu[]{
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
		if (oldSel == newSel)
			return; // this also serves as a protection against access to menus[0] when such may not exist.
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

	/**
	 * Reloading the entries to refresh the current pack list.
	 */
	private void reloadEntries() {
		entries0.clear(); // First list: unloaded.
		for (ResourcePack pack : resourcePacks) { // First list: all available resource packs.
			entries0.add(new SelectEntry(pack.name, () -> Game.setDisplay(new PopupDisplay(null, pack.name, pack.description)), false) {
				@Override
				public int getColor(boolean isSelected) {
					if (selection == 1) return SelectEntry.COL_UNSLCT;
					return super.getColor(isSelected);
				}
			});
		}

		entries1.clear(); // Second list: to be loaded.
		for (ResourcePack pack : loadedPacks) { // Second List: loaded resource packs.
			entries1.add(new SelectEntry(pack.name, () -> Game.setDisplay(new PopupDisplay(null, pack.name, pack.description)), false) {
				@Override
				public int getColor(boolean isSelected) {
					if (selection == 0) return SelectEntry.COL_UNSLCT;
					return super.getColor(isSelected);
				}
			});
		}
	}

	/**
	 * Applying the reloaded entries into the display.
	 */
	private void refreshEntries() {
		reloadEntries();
		Menu[] newMenus = new Menu[]{
			builder0.setEntries(entries0)
				.createMenu(),
			builder1.setEntries(entries1)
				.createMenu()
		};

		// Reapplying selections.
		newMenus[0].setSelection(menus[0].getSelection());
		newMenus[1].setSelection(menus[1].getSelection());

		menus = newMenus;

		/* Translate position. */
		menus[selection ^ 1].translate(menus[selection].getBounds().getWidth() + padding, 0);
	}

	/**
	 * Watching the directory changes. Allowing hot-loading.
	 */
	private class WatcherThread extends Thread implements Closeable {
		private WatchService watcher;
		private volatile Thread running = this;

		WatcherThread() {
			super("Resource Pack File Watcher");
			try {
				watcher = FileSystems.getDefault().newWatchService();
				FOLDER_LOCATION.toPath().register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			} catch (IOException e) {
				CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Unable to Watch File", CrashHandler.ErrorInfo.ErrorType.UNHANDLEABLE, "Unable to create file water service."));
			}

			start();
			Logging.RESOURCEHANDLER_RESOURCEPACK.debug("WatcherThread started.");
		}

		@Override
		public void run() {
			while (running == this) {
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
			}

			Logging.RESOURCEHANDLER_RESOURCEPACK.trace("File watcher terminated.");
		}

		@Override
		public void close() {
			running = null;
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
	public void tick(InputHandler input) {
		// Overrides the default tick handler.
		if (input.getMappedKey("shift+cursor-right").isClicked()) { // Move the selected pack to the second list.
			if (selection == 0 && resourcePacks.size() > 0) {
				loadedPacks.add(loadedPacks.indexOf(defaultPack), resourcePacks.remove(menus[0].getSelection()));
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		} else if (input.getMappedKey("shift+cursor-left").isClicked()) { // Move the selected pack to the first list.
			if (selection == 1 && loadedPacks.get(menus[1].getSelection()) != defaultPack) {
				resourcePacks.add(loadedPacks.remove(menus[1].getSelection()));
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		} else if (input.getMappedKey("shift+cursor-up").isClicked()) { // Move up the selected pack in the second list.
			if (selection == 1 && menus[1].getSelection() > 0) {
				if (loadedPacks.get(menus[1].getSelection()) == defaultPack) return; // Default pack remains bottom.
				loadedPacks.add(menus[1].getSelection() - 1, loadedPacks.remove(menus[1].getSelection()));
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		} else if (input.getMappedKey("shift+cursor-down").isClicked()) { // Move down the selected pack in the second list.
			if (selection == 1 && menus[1].getSelection() < loadedPacks.size() - 1) {
				if (loadedPacks.get(menus[1].getSelection() + 1) == defaultPack) return; // Default pack remains bottom.
				loadedPacks.add(menus[1].getSelection() + 1, loadedPacks.remove(menus[1].getSelection()));
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		} else if (input.getMappedKey("cursor-right").isClicked()) { // Move cursor to the second list.
			if (selection == 0) {
				Sound.play("select");
				onSelectionChange(0, 1);
			}

			return;
		} else if (input.getMappedKey("cursor-left").isClicked()) { // Move cursor to the first list.
			if (selection == 1) {
				Sound.play("select");
				onSelectionChange(1, 0);
			}

			return;
		}

		super.tick(input);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Title
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.title"), screen, 6, Color.WHITE);

		// Info text at the bottom.
		if (Game.input.anyControllerConnected())
			Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.keyboard_needed"), screen, Screen.h - 33, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.move", Game.input.getMapping("cursor-down"), Game.input.getMapping("cursor-up")), screen, Screen.h - 25, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.select", Game.input.getMapping("SELECT")), screen, Screen.h - 17, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.position"), screen, Screen.h - 9, Color.DARK_GRAY);

		ArrayList<ResourcePack> packs = selection == 0 ? resourcePacks : loadedPacks;
		if (packs.size() > 0) { // If there is any pack that can be selected.
			@SuppressWarnings("resource")
			MinicraftImage logo = packs.get(menus[selection].getSelection()).logo;
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

	/**
	 * The object representation of resource pack.
	 */
	private static class ResourcePack implements Closeable {
		private URL packRoot;

		/**
		 * 0 - before 2.2.0; 1 - 2.2.0-latest
		 */
		@SuppressWarnings("unused")
		private final int packFormat; // The pack format of the pack.
		private final String name; // The name of the pack.
		private final String description; // The description of the pack.
		private MinicraftImage logo; // The logo of the pack.

		private boolean opened = false; // If the zip file stream is opened.
		private ZipFile zipFile = null; // The zip file stream.

		private ResourcePack(URL packRoot, int packFormat, String name, String desc) {
			this.packRoot = packRoot;
			this.packFormat = packFormat;
			this.name = name;
			this.description = desc;
			refreshPack();
		}

		/**
		 * This does not include metadata refresh.
		 */
		public void refreshPack() {
			// Refresh pack logo.png.
			try {
				openStream();
				InputStream in = getResourceAsStream("pack.png");
				if (in != null) {
					logo = new MinicraftImage(ImageIO.read(in));

					// Logo size verification.
					int h = logo.height;
					int w = logo.width;
					if (h == 0 || w == 0 || h % 8 != 0 || w % 8 != 0 ||
						h > 32 || w > Screen.w) {
						throw new IOException(String.format("Unacceptable logo size: %s;%s", w, h));
					}
				} else {
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Pack logo not found in pack: {}, loading default logo instead.", name);
					logo = defaultLogo;
				}
				close();

			} catch (IOException | NullPointerException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load logo in pack: {}, loading default logo instead.", name);
				if (this == defaultPack) {
					try {
						logo = new MinicraftImage(ImageIO.read(getClass().getResourceAsStream("/resources/logo.png")));
					} catch (IOException e1) {
						CrashHandler.crashHandle(e1);
					}
				} else logo = defaultLogo;
			}
		}

		/**
		 * Open the stream of the zip file.
		 *
		 * @return {@code true} if the stream has successfully been opened.
		 */
		private boolean openStream() {
			try {
				zipFile = new ZipFile(new File(packRoot.toURI()));
				return opened = true;
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
				return opened = false;
			}
		}

		/**
		 * Closing the stream of the zip file if opened.
		 */
		@Override
		public void close() throws IOException {
			if (opened) {
				zipFile.close();
				zipFile = null;
				opened = false;
			}
		}

		/**
		 * Getting the stream by the path.
		 *
		 * @param path The path of the entry.
		 * @return The input stream of the specified entry.
		 * @throws IOException if an I/O error has occurred.
		 */
		private InputStream getResourceAsStream(String path) throws IOException {
			try {
				return zipFile.getInputStream(zipFile.getEntry(path));
			} catch (NullPointerException e) {
				throw new IOException(e);
			}
		}

		@FunctionalInterface
		private static interface FilesFilter { // Literally functioned.
			public abstract boolean check(Path path, boolean isDir);
		}

		/**
		 * Getting the subfiles under the specified entry directrory.
		 *
		 * @param path   The directory to be listed.
		 * @param filter The filter to be applied.
		 * @return The filtered (if any) subfile and subfolder list. Empty if not or invalid path.
		 */
		@NotNull
		private ArrayList<String> getFiles(String path, FilesFilter filter) {
			ArrayList<String> paths = new ArrayList<>();
			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
				ZipEntry entry = e.nextElement();
				Path parent;
				if ((parent = Paths.get(entry.getName()).getParent()) != null && parent.equals(Paths.get(path)) &&
					(filter == null || filter.check(Paths.get(entry.getName()), entry.isDirectory()))) {
					paths.add(entry.getName());
				}
			}

			return paths;
		}
	}

	/**
	 * Reading the string from the input stream.
	 *
	 * @param in The input stream to be read.
	 * @return The returned string.
	 */
	public static String readStringFromInputStream(InputStream in) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		return String.join("\n", reader.lines().toArray(String[]::new));
	}

	/**
	 * Loading pack metadata of the pack.
	 *
	 * @param file The path of the pack.
	 * @return The loaded pack with metadata.
	 */
	public static ResourcePack loadPackMetadata(URL file) {
		try (ZipFile zip = new ZipFile(new File(file.toURI()))) {
			try (InputStream in = zip.getInputStream(zip.getEntry("pack.json"))) {
				JSONObject meta = new JSONObject(readStringFromInputStream(in));
				return new ResourcePack(file.toURI().toURL(),
					meta.getInt("pack_format"), meta.optString("name", new File(file.toURI()).getName()), meta.optString("description", "No description"));
			}
		} catch (JSONException | IOException | URISyntaxException e) {
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Cannot Load Resource Pack",
				CrashHandler.ErrorInfo.ErrorType.REPORT, String.format("Unable to load resource pack: %s.", file.getPath())));
		} catch (NullPointerException e) { // pack.json is missing.
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Resource Pack not Supported",
				CrashHandler.ErrorInfo.ErrorType.HANDLED, String.format("Missing pack.json in resource pack: %s.", file.getPath())));
		}

		return null;
	}

	/**
	 * Intializing the packs from directory and loaded.
	 */
	public static void initPacks() {
		// Generate resource packs folder
		if (FOLDER_LOCATION.mkdirs()) {
			Logging.RESOURCEHANDLER_RESOURCEPACK.info("Created resource packs folder at {}.", FOLDER_LOCATION);
		}

		ArrayList<URL> urls = new ArrayList<>();
		// Read and add the .zip file to the resource pack list. Only accept files ending with .zip or directory.
		for (File file : Objects.requireNonNull(FOLDER_LOCATION.listFiles((dur, name) -> name.endsWith(".zip")))) {
			try {
				urls.add(file.toPath().toUri().toURL());
			} catch (MalformedURLException e) {
				CrashHandler.errorHandle(e);
			}
		}

		// Adding all unadded packs for refreshing.
		URL theURL;
		for (ResourcePack pack : resourcePacks) {
			if (!urls.contains(theURL = pack.packRoot)) {
				urls.add(theURL);
			}
		}

		for (ResourcePack pack : loadedPacks) {
			if (pack == defaultPack && !urls.contains(theURL = pack.packRoot)) {
				urls.add(theURL);
			}
		}

		refreshResourcePacks(urls);
	}

	/**
	 * Finding the pack by pack's file URL.
	 *
	 * @param url The url for query.
	 * @return The found resource pack. {@code null} if not found.
	 */
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

	/**
	 * Refreshing the pack list by the urls.
	 *
	 * @param urls The packs' url to be refreshed.
	 */
	private static void refreshResourcePacks(List<URL> urls) {
		for (URL url : urls) {
			ResourcePack pack = findPackByURL(url);
			if (pack != null) { // Refresh the current.
				try {
					if (new File(url.toURI()).exists())
						pack.refreshPack();
					else { // Remove if not exist.
						resourcePacks.remove(pack);
						loadedPacks.remove(pack);
					}
				} catch (URISyntaxException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.debug(e, "Resource pack URL not found.");
					resourcePacks.remove(pack);
					loadedPacks.remove(pack);
				}
			} else { // Add new pack as it should be exist.
				pack = loadPackMetadata(url);
				if (pack != null) {
					resourcePacks.add(pack);
				}
			}
		}

		resourcePacks.sort((p1, p2) -> p1.name.compareTo(p2.name));
	}

	/**
	 * Releasing the unloaded packs.
	 */
	public static void releaseUnloadedPacks() {
		resourcePacks.clear(); // Releases unloaded packs.
	}

	/**
	 * Loading the resource packs when loading preferences. This should only be called by {@link minicraft.saveload.Load}.
	 *
	 * @param names The names of the packs.
	 */
	public static void loadResourcePacks(String[] names) {
		for (String name : names) {
			for (ResourcePack pack : new ArrayList<>(resourcePacks)) {
				try {
					if (Paths.get(pack.packRoot.toURI()).equals(FOLDER_LOCATION.toPath().resolve(name))) {
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

	/**
	 * Getting the names of the loaded packs. This should only be called by {@link minicraft.saveload.Save}.
	 *
	 * @return The names of currently loaded packs.
	 */
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

	/**
	 * Reloading all the resources with the currently packs to be loaded.
	 */
	@SuppressWarnings("unchecked")
	public static void reloadResources() {
		loadQuery.clear();
		loadQuery.addAll(loadedPacks);
		Collections.reverse(loadQuery);

		// Clear all previously loaded resources.
		Renderer.spriteLinker.resetSprites();
		Localization.resetLocalizations();
		BookData.resetBooks();
		Sound.resetSounds();
		SpriteAnimation.resetMetadata();
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

		SpriteAnimation.refreshAnimations();
		Renderer.spriteLinker.updateLinkedSheets();
		Localization.loadLanguage();

		// Refreshing skins
		SkinDisplay.refreshSkins();
		SkinDisplay.releaseSkins();
	}

	/**
	 * Loading the textures of the pack.
	 *
	 * @param pack The pack to be loaded.
	 * @throws IOException if I/O exception occurs.
	 */
	private static void loadTextures(ResourcePack pack) throws IOException {
		for (String t : pack.getFiles("assets/textures/", null)) {
			switch (t) {
				case "assets/textures/entity/":
					loadTextures(pack, SpriteType.Entity);
					break;
				case "assets/textures/gui/":
					loadTextures(pack, SpriteType.Gui);
					break;
				case "assets/textures/item/":
					loadTextures(pack, SpriteType.Item);
					break;
				case "assets/textures/tile/":
					loadTextures(pack, SpriteType.Tile);
					break;
			}
		}
	}

	/**
	 * Loading the categories of textures from the pack.
	 *
	 * @param pack The pack to be loaded.
	 * @param type The category of textures.
	 * @throws IOException if I/O exception occurs.
	 */
	private static void loadTextures(ResourcePack pack, SpriteType type) throws IOException {
		String path = "assets/textures/";
		switch (type) {
			case Entity:
				path += "entity/";
				break;
			case Gui:
				path += "gui/";
				break;
			case Item:
				path += "item/";
				break;
			case Tile:
				path += "tile/";
				break;
		}

		ArrayList<String> pngs = pack.getFiles(path, (p, isDir) -> p.toString().endsWith(".png") && !isDir);
		if (type == SpriteType.Tile) {
			// Loading sprite sheet metadata.
			for (String m : pack.getFiles(path, (p, isDir) -> p.toString().endsWith(".png.json") && !isDir)) {
				try {
					JSONObject obj = new JSONObject(readStringFromInputStream(pack.getResourceAsStream(m)));
					SpriteLinker.SpriteMeta meta = new SpriteLinker.SpriteMeta();
					pngs.remove(m.substring(0, m.length() - 5));
					BufferedImage image = ImageIO.read(pack.getResourceAsStream(m.substring(0, m.length() - 5)));

					// Applying animations.
					MinicraftImage sheet;
					JSONObject animation = obj.optJSONObject("animation");
					if (animation != null) {
						meta.frametime = animation.getInt("frametime");
						meta.frames = image.getHeight() / 16;
						if (meta.frames == 0) throw new IOException(new IllegalArgumentException(String.format(
							"Invalid frames 0 detected with {} in pack: {}", m, pack.name)));
						sheet = new MinicraftImage(image, 16, 16 * meta.frames);
					} else
						sheet = new MinicraftImage(image, 16, 16);
					Renderer.spriteLinker.setSprite(type, m.substring(path.length(), m.length() - 9), sheet);

					JSONObject borderObj = obj.optJSONObject("border");
					if (borderObj != null) {
						meta.border = borderObj.optString("key");
						if (meta.border.isEmpty()) meta.border = null;
						if (meta.border != null) {
							String borderK = path + meta.border + ".png";
							pngs.remove(borderK);
							try {
								Renderer.spriteLinker.setSprite(type, meta.border, new MinicraftImage(ImageIO.read(pack.getResourceAsStream(borderK)), 24, 24));
							} catch (IOException e) {
								Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to read {} with {} in pack: {}", borderK, m, pack.name);
								meta.border = null;
							}
						}

						meta.corner = borderObj.optString("corner");
						if (meta.corner.isEmpty()) meta.corner = null;
						if (meta.corner != null) {
							String cornerK = path + meta.corner + ".png";
							pngs.remove(cornerK);
							try {
								Renderer.spriteLinker.setSprite(type, meta.corner, new MinicraftImage(ImageIO.read(pack.getResourceAsStream(cornerK)), 16, 16));
							} catch (IOException e) {
								Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to read {} with {} in pack: {}", cornerK, m, pack.name);
								meta.corner = null;
							}
						}
					}

					SpriteAnimation.setMetadata(m.substring(path.length(), m.length() - 9), meta);
				} catch (JSONException | IOException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to read {} in pack: {}", m, pack.name);
				}
			}

		}

		// Loading the left pngs.
		for (String p : pngs) {
			try {
				BufferedImage image = ImageIO.read(pack.getResourceAsStream(p));
				MinicraftImage sheet;
				if (type == SpriteType.Item) {
					sheet = new MinicraftImage(image, 8, 8); // Set the minimum tile sprite size.
				} else if (type == SpriteType.Tile) {
					sheet = new MinicraftImage(image, 16, 16); // Set the minimum item sprite size.
				} else {
					sheet = new MinicraftImage(image);
				}

				Renderer.spriteLinker.setSprite(type, p.substring(path.length(), p.length() - 4), sheet);
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Unable to load {} in pack : {}", p, pack.name);
			}
		}
	}

	/**
	 * Loading localization from the pack.
	 *
	 * @param pack The pack to be loaded.
	 */
	private static void loadLocalization(ResourcePack pack) {
		JSONObject langJSON = null;
		try {
			langJSON = new JSONObject(readStringFromInputStream(pack.getResourceAsStream("pack.json"))).optJSONObject("language");
		} catch (JSONException | IOException e1) {
			Logging.RESOURCEHANDLER_RESOURCEPACK.debug(e1, "Unable to load pack.json in pack: {}", pack.name);
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
			String str = Paths.get(f).getFileName().toString();
			try { // JSON verification.
				String json = readStringFromInputStream(pack.getResourceAsStream(f));
				JSONObject obj = new JSONObject(json);
				for (String k : obj.keySet()) {
					obj.getString(k);
				}

				// Add verified localization.
				Localization.addLocalization(Locale.forLanguageTag(str.substring(0, str.length() - 5)), json);
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load localization: {} in pack : {}", f, pack.name);
			} catch (JSONException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Invalid JSON format detected in localization: {} in pack : {}", f, pack.name);
			}
		}
	}

	/**
	 * Loading the books from the pack.
	 *
	 * @param pack The pack to be loaded.
	 */
	private static void loadBooks(ResourcePack pack) {
		for (String path : pack.getFiles("assets/books", (path, isDir) -> path.toString().endsWith(".txt") && !isDir)) {
			try {
				String book = BookData.loadBook(readStringFromInputStream(pack.getResourceAsStream(path)));
				switch (path) {
					case "assets/books/about.txt":
						BookData.about = () -> book;
						break;
					case "assets/books/credits.txt":
						BookData.credits = () -> book;
						break;
					case "assets/books/instructions.txt":
						BookData.instructions = () -> book;
						break;
					case "assets/books/antidous.txt":
						BookData.antVenomBook = () -> book;
						break;
					case "assets/books/game_guide.txt":
						BookData.storylineGuide = () -> book;
						break;
				}
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load book: {} in pack : {}", path, pack.name);
			}
		}
	}

	/**
	 * Loading sounds from the pack.
	 *
	 * @param pack The pack to be loaded.
	 */
	private static void loadSounds(ResourcePack pack) {
		for (String f : pack.getFiles("assets/sound/", (path, isDir) -> path.toString().endsWith(".wav") && !isDir)) {
			String name = Paths.get(f).getFileName().toString();
			try {
				Sound.loadSound(name.substring(0, name.length() - 4), new BufferedInputStream(pack.getResourceAsStream(f)), pack.name);
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load audio: {} in pack : {}", f, pack.name);
			}
		}
	}
}

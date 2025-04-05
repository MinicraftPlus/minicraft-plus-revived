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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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


	private static final ArrayList<ResourcePack> resourcePacks = new ArrayList<>(); // List of all packs, in order of loading priority
	private static final File FOLDER_LOCATION = new File(FileHandler.gameDir + "/resourcepacks");
	@SuppressWarnings("unused")
	private static final int VERSION = 1;

	private static final ResourcePack defaultPack; // Used to check if the resource pack default.
	private static final MinicraftImage defaultLogo;

	private static final int padding = 10;

	private WatcherThread fileWatcher;
	private ArrayList<ListEntry> entries = new ArrayList<>(), toggleIndicators = new ArrayList<>();
	private Menu.Builder builder, indicatorsBuilder;
	private boolean changed = false;

	static { // Initializing the default pack and logo.
		Path defaultPackPath = FileHandler.getJarResourcesPath();
		URL defaultPackURL;
		try {
			defaultPackURL = defaultPackPath.toUri().toURL();
		} catch (MalformedURLException e) {
			CrashHandler.crashHandle(e);
			throw new RuntimeException(e);
		}

		// Default resources
		try {
			defaultPack = Objects.requireNonNull(
				loadPackFromStream(defaultPackURL, defaultPackPath.resolve("pack.json").toUri().toURL().openStream(), "Default")
			);
			defaultPack.enable();
			defaultPack.lock();
			resourcePacks.add(defaultPack);
			defaultLogo = MinicraftImage.createDefaultCompatible(ImageIO.read(Objects.requireNonNull(ResourcePackDisplay.class.getResourceAsStream("/resources/default_pack.png"))));
		} catch (IOException e) {
			CrashHandler.crashHandle(e);
			throw new UncheckedIOException(e);
		} catch (MinicraftImage.MinicraftImageDimensionIncompatibleException | NullPointerException e) {
			CrashHandler.crashHandle(e);
			//noinspection ProhibitedExceptionThrown
			throw new RuntimeException(e);
		}
	}

	/**
	 * Initializing the Display.
	 */
	public ResourcePackDisplay() {
		super(true, true);
		initPacks();

		indicatorsBuilder = new Menu.Builder(false, 2, RelPos.LEFT).setPositioning(new Point(20, 60), RelPos.BOTTOM_RIGHT);
		builder = new Menu.Builder(false, 2, RelPos.LEFT).setPositioning(new Point(20, 60), RelPos.BOTTOM_LEFT);

		reloadEntries();

		menus = new Menu[] {
			indicatorsBuilder.setEntries(toggleIndicators).createMenu(),
			builder.setEntries(entries).createMenu()
		};

		selection = 1;

		if (menus[1].getBounds().getLeft() - menus[0].getBounds().getRight() < padding)
			menus[1].translate(menus[0].getBounds().getRight() - menus[1].getBounds().getLeft() + padding, 0);

		fileWatcher = new WatcherThread();
	}

	@Override
	protected void onSelectionChange(int oldSel, int newSel) {
		super.onSelectionChange(oldSel, newSel);
		if (oldSel == newSel)
			return; // this also serves as a protection against access to menus[0] when such may not exist.
		if (menus[1].getBounds().getLeft() - menus[0].getBounds().getRight() < padding)
			menus[1].translate(menus[0].getBounds().getRight() - menus[1].getBounds().getLeft() + padding, 0);
	}

	/**
	 * Reloading the entries to refresh the current pack list.
	 */
	private void reloadEntries() {
		// Entries will hold selectable resource pack entries,
		// toggleIndicators just indicate whether a pack is enabled
		entries.clear();
		toggleIndicators.clear();
		for (ResourcePack pack : resourcePacks) { // List all available resource packs
			entries.add(new SelectEntry(pack.name, () -> { pack.toggle(); changed = true; }, false));
			toggleIndicators.add(new SelectEntry("✓", () -> {}, false) {
				@Override
				public String toString() {
					if(pack.isEnabled()) return super.toString();
					else return "";
				}

				@Override
				public int getColor(boolean isSelected) {
					if(pack == defaultPack) return COL_UNSLCT;
					else return COL_SLCT;
				}
			});
			toggleIndicators.get(toggleIndicators.size()-1).setSelectable(false);
		}
		entries.get(toggleIndicators.size()-1).setSelectable(false);
	}

	/**
	 * Applying the reloaded entries into the display.
	 */
	private void refreshEntries() {
		reloadEntries();
		Menu[] newMenus = new Menu[] {
			indicatorsBuilder.setEntries(toggleIndicators).createMenu(),
			builder.setEntries(entries).createMenu()
		};

		// Reapplying selections.
		newMenus[0].setSelection(menus[0].getSelection());
		newMenus[1].setSelection(menus[1].getSelection());

		menus = newMenus;

		// Translate position.
		onSelectionChange(selection ^ 1, selection);
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
		releaseUnloadedPacks();
		fileWatcher.close(); // Removes watcher.
		new Save();
		if (changed) reloadResources();
	}

	@Override
	public void tick(InputHandler input) {
		// Overrides the default tick handler.
		if (input.getMappedKey("cursor-right").isClicked()) { // Move the selected pack down.
			if (menus[1].getSelection() < resourcePacks.size() - 2) { // Only if it has space to move down (and stay above default)
				int i = menus[1].getSelection();
				ResourcePack swap = resourcePacks.get(i + 1);
				resourcePacks.set(i + 1, resourcePacks.get(i));
				resourcePacks.set(i, swap);
				menus[1].setSelection(i + 1);
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		} else if (input.getMappedKey("cursor-left").isClicked()) { // Move the selected pack up.
			if (menus[1].getSelection() > 0 && resourcePacks.get(menus[1].getSelection()) != defaultPack) {
				int i = menus[1].getSelection();
				ResourcePack swap = resourcePacks.get(i - 1);
				resourcePacks.set(i - 1, resourcePacks.get(i));
				resourcePacks.set(i, swap);
				menus[1].setSelection(i - 1);
				changed = true;
				refreshEntries();
				Sound.play("select");
			}

			return;
		} else if (input.getMappedKey("menu").isClicked()) {
			ResourcePack pack = resourcePacks.get(menus[1].getSelection());
			Game.setDisplay(new PopupDisplay(null, pack.name, pack.description));
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
			Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.keyboard_needed"), screen, Screen.h - 41, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.move", Game.input.getMapping("cursor-up"), Game.input.getMapping("cursor-down")), screen, Screen.h - 33, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.reorder", Game.input.getMapping("cursor-left"), Game.input.getMapping("cursor-right")), screen, Screen.h - 25, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.select", Game.input.getMapping("select")), screen, Screen.h - 17, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.info", Game.input.getMapping("menu")), screen, Screen.h - 9, Color.DARK_GRAY);

		if (resourcePacks.size() > 0) { // If there is any pack that can be selected.
			@SuppressWarnings("resource")
			MinicraftImage logo = resourcePacks.get(menus[1].getSelection()).logo;
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
		private Path packRootPath;

		/**
		 * 0 - before 2.2.0; 1 - 2.2.0-latest
		 */
		@SuppressWarnings("unused")
		private final int packFormat; // The pack format of the pack.
		private final String name; // The name of the pack.
		private final String description; // The description of the pack.
		private MinicraftImage logo; // The logo of the pack.

		private boolean enabled = false, locked = false;

		private boolean opened = false; // If the zip file stream is opened.
		private ZipFile zipFile = null; // The zip file stream.

		private boolean isZip;

		private ResourcePack(URL packRoot, int packFormat, String name, String desc) throws IOException {
			this.packRoot = packRoot;
			try {
				this.packRootPath = Paths.get(packRoot.toURI());
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
			this.isZip = packRoot.getFile().endsWith(".zip");
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
					logo = MinicraftImage.createDefaultCompatible(ImageIO.read(in));

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

			} catch (IOException | NullPointerException | MinicraftImage.MinicraftImageDimensionIncompatibleException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load logo in pack: {}, loading default logo instead.", name);
				if (this == defaultPack) {
					try {
						logo = new MinicraftImage(ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/resources/logo.png"))));
					} catch (IOException | NullPointerException e1) {
						CrashHandler.crashHandle(e1);
					}
				} else logo = defaultLogo;
			}
		}

		private boolean isEnabled() {
			return enabled;
		}

		private void lock() { locked = true; }
		private void enable() {
			if(locked) return;
			enabled = true;
		}
		private void toggle() {
			if(locked) return;
			enabled = !enabled;
		}

		/**
		 * Open the stream of the zip file.
		 * @return {@code true} if the stream has successfully been opened.
		 */
		private boolean openStream() {
			try {
				if (!this.isZip)
					return Files.exists(this.packRootPath) && Files.isDirectory(this.packRootPath);

				zipFile = new ZipFile(new File(this.packRoot.toURI()));
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
		 * @param path The path of the entry.
		 * @return The input stream of the specified entry.
		 * @throws IOException if an I/O error has occurred.
		 */
		private InputStream getResourceAsStream(String path) throws IOException {
			try {
				if (this.isZip)
					return zipFile.getInputStream(zipFile.getEntry(path));
				else
					return Files.newInputStream(this.packRootPath.resolve(path));
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
		 * @param path The directory to be listed.
		 * @param filter The filter to be applied.
		 * @return The filtered (if any) subfile and subfolder list. Empty if not or invalid path.
		 */
		@NotNull
		private ArrayList<String> getFiles(String path, FilesFilter filter) {
			ArrayList<String> paths = new ArrayList<>();

			if (this.isZip) {
				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
					ZipEntry entry = e.nextElement();
					Path parent;
					if ((parent = Paths.get(entry.getName()).getParent()) != null && parent.equals(Paths.get(path)) &&
						(filter == null || filter.check(Paths.get(entry.getName()), entry.isDirectory()))) {
						String entryName = entry.getName();
						if (!this.packRootPath.resolve(entryName).normalize().startsWith(this.packRootPath)) continue;
						paths.add(entryName);
					}
				}
			} else if (Files.exists(this.packRootPath.resolve(path))) {
				try (Stream<Path> stream = Files.walk(this.packRootPath.resolve(path), 1)) {
					stream.forEach(p -> {
						boolean isDir = Files.isDirectory(p);
						if ((filter == null || filter.check(p, isDir))) {
							paths.add(this.packRootPath.relativize(p).toString().replaceAll("\\\\", "/") + (isDir ? "/" : ""));
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return paths;
		}
	}

	/**
	 * Reading the string from the input stream.
	 * @param in The input stream to be read.
	 * @return The returned string.
	 */
	public static String readStringFromInputStream(InputStream in) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		return String.join("\n", reader.lines().toArray(String[]::new));
	}

	/**
	 * Construct a resource pack from a location, pack.json input stream, and default filename
	 * @param url The source location of the resource pack
	 * @param packMeta The input stream of a pack.json file
	 * @param filename The name to default to if no "name" key found in pack.json
	 */
	public static ResourcePack loadPackFromStream(URL url, InputStream packMeta, String filename) throws IOException {
		JSONObject meta = new JSONObject(readStringFromInputStream(packMeta));
		return new ResourcePack(
			url,
			meta.getInt("pack_format"),
			meta.optString("name", filename),
			meta.optString("description", "No description")
		);
	}

	/**
	 * Loading pack metadata of the pack.
	 * @param fileUrl The path of the pack.
	 * @return The loaded pack with metadata.
	 */
	public static ResourcePack loadPackMetadata(URL fileUrl) {

		try {
			File file = new File(fileUrl.toURI());
			if (file.isDirectory()) {
				try (InputStream in = Files.newInputStream(Paths.get(fileUrl.toURI()).resolve("pack.json"))) {
					return loadPackFromStream(fileUrl, in, file.getName());
				}
			} else {
				try (ZipFile zip = new ZipFile(file)) {
					try (InputStream in = zip.getInputStream(zip.getEntry("pack.json"))) {
						return loadPackFromStream(fileUrl, in, file.getName());
					}
				}
			}
		} catch (JSONException | IOException | URISyntaxException e) {
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Cannot Load Resource Pack",
				CrashHandler.ErrorInfo.ErrorType.REPORT, String.format("Unable to load resource pack: %s.", fileUrl.getPath())));
		} catch (NullPointerException e) { // pack.json is missing.
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Resource Pack not Supported",
				CrashHandler.ErrorInfo.ErrorType.HANDLED, String.format("Missing pack.json in resource pack: %s.", fileUrl.getPath())));
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
		urls.add(defaultPack.packRoot); // Add default pack first

		// Add already recognized resourcePacks
		for (ResourcePack pack : resourcePacks) {
			if (!urls.contains(pack.packRoot)) {
				urls.add(pack.packRoot);
			}
		}

		// Read and add the folder (that contains a pack.json) or .zip file to the resource pack list.
		// Only accept files ending with .zip or directory.
		for (File file : Objects.requireNonNull(FOLDER_LOCATION.listFiles((dur, name) -> {
				if (name.endsWith(".zip")) return true;
				Path path = dur.toPath().resolve(name);
				return Files.isDirectory(path) && Files.exists(path.resolve("pack.json"));
			}))) {
			try {
				URL url = file.toPath().toUri().toURL();
				if(!urls.contains(url))
					urls.add(url);
			} catch (MalformedURLException e) {
				CrashHandler.errorHandle(e);
			}
		}

		refreshResourcePacks(urls);
	}

	/**
	 * Finding the pack by pack's file URL.
	 * @param url The url for query.
	 * @return The found resource pack. {@code null} if not found.
	 */
	private static ResourcePack findPackByURL(URL url) {
		for (ResourcePack pack : resourcePacks) {
			if (pack.packRoot.equals(url)) {
				return pack;
			}
		}

		return null;
	}

	/**
	 * Refreshing the pack list by the urls.
	 * @param urls The packs' url to be refreshed.
	 */
	private static void refreshResourcePacks(List<URL> urls) {
		// Reverse from entry-order (default last) to priority order (default first)
		Collections.reverse(resourcePacks);

		Logging.RESOURCEHANDLER_RESOURCEPACK.debug("Refreshing from " + urls);
		for (URL url : urls) {
			ResourcePack pack = findPackByURL(url);
			if (pack != null) { // Re-check and refresh if it exists
				try {
					if (pack == defaultPack || new File(url.toURI()).exists())
						pack.refreshPack();
					else // Remove if it doesn't exist
						resourcePacks.remove(pack);
				} catch (URISyntaxException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.debug(e, "Resource pack URL not found.");
					resourcePacks.remove(pack);
				}
			} else { // Add as a new pack
				pack = loadPackMetadata(url);
				if (pack != null) {
					resourcePacks.add(pack);
				}
			}
		}

		// Reverse from priority-order (default first) to entry order (default last)
		Collections.reverse(resourcePacks);
	}

	/**
	 * Releasing the unloaded packs.
	 */
	public static void releaseUnloadedPacks() {
		for(int i = 0; i < resourcePacks.size(); i++) {
			if(!resourcePacks.get(i).isEnabled()) {
				resourcePacks.remove(i--); // Releases unloaded packs.
			}
		}
	}

	/**
	 * Loading the resource packs when loading preferences. This should only be called by {@link minicraft.saveload.Load}.
	 * @param names The names of the packs.
	 */
	public static void loadResourcePacks(String[] names) {
		for (String name : names) {
			for (ResourcePack pack : resourcePacks) {
				try {
					if (Paths.get(pack.packRoot.toURI()).equals(FOLDER_LOCATION.toPath().resolve(name))) {
						pack.enable();
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
	 * @return The names of currently loaded packs.
	 */
	public static ArrayList<String> getLoadedPacks() {
		ArrayList<String> packs = new ArrayList<>();
		for (ResourcePack pack : resourcePacks) {
			if (pack != defaultPack && pack.isEnabled()) {
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
		ArrayList<ResourcePack> loadQuery = new ArrayList<>();
		loadQuery.addAll(resourcePacks);
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
					String imgName = m.substring(0, m.length() - 5);
					pngs.remove(imgName);
					BufferedImage image = ImageIO.read(pack.getResourceAsStream(imgName));

					// Applying animations.
					MinicraftImage sheet;
					JSONObject animation = obj.optJSONObject("animation");
					if (animation != null) {
						meta.frametime = animation.getInt("frametime");
						meta.frames = image.getHeight() / 16;
						if (meta.frames == 0) throw new IOException(new IllegalArgumentException(String.format(
							"Invalid frames 0 detected with {} in pack: {}", m, pack.name)));
						validateImageAsset(pack, imgName, image, 16, 16 * meta.frames);
						sheet = new MinicraftImage(image, 16, 16 * meta.frames);
					} else {
						validateImageAsset(pack, imgName, image, 16, 16);
						sheet = new MinicraftImage(image, 16, 16);
					}
					Renderer.spriteLinker.setSprite(type, m.substring(path.length(), m.length() - 9), sheet);

					JSONObject borderObj = obj.optJSONObject("border");
					if (borderObj != null) {
						meta.border = borderObj.optString("key");
						if (meta.border.isEmpty()) meta.border = null;
						if (meta.border != null) {
							String borderK = path + meta.border + ".png";
							pngs.remove(borderK);
							try {
								BufferedImage img = ImageIO.read(pack.getResourceAsStream(borderK));
								validateImageAsset(pack, borderK, img, 24, 24);
								Renderer.spriteLinker.setSprite(type, meta.border, new MinicraftImage(img, 24, 24));
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
								BufferedImage img = ImageIO.read(pack.getResourceAsStream(cornerK));
								validateImageAsset(pack, cornerK, img, 16, 16);
								Renderer.spriteLinker.setSprite(type, meta.corner, new MinicraftImage(img, 16, 16));
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
					validateImageAsset(pack, p, image, 8, 8);
					sheet = new MinicraftImage(image, 8, 8); // Set the minimum tile sprite size.
				} else if (type == SpriteType.Tile) {
					validateImageAsset(pack, p, image, 16, 16);
					sheet = new MinicraftImage(image, 16, 16); // Set the minimum item sprite size.
				} else {
					validateImageAsset(pack, p, image);
					sheet = new MinicraftImage(image);
				}

				Renderer.spriteLinker.setSprite(type, p.substring(path.length(), p.length() - 4), sheet);
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Unable to load {} in pack : {}", p, pack.name);
			}
		}
	}

	private static void validateImageAsset(ResourcePack pack, String key, BufferedImage image) {
		try {
			MinicraftImage.validateImageDimension(image);
		} catch (MinicraftImage.MinicraftImageDimensionIncompatibleException e) {
			Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Potentially incompatible image detected: {} in pack: {}: "+
				"image size ({}x{}) is not in multiple of 8.", key, pack.name, e.getWidth(), e.getHeight());
		}
	}

	private static void validateImageAsset(ResourcePack pack, String key, BufferedImage image, int width, int height) {
		validateImageAsset(pack, key, image);
		try {
			MinicraftImage.validateImageDimension(image, width, height);
		} catch (MinicraftImage.MinicraftImageRequestOutOfBoundsException e) {
			Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Potentially incompatible image detected: {} in pack: {}: "+
				"image size ({}x{}) is smaller than the required ({}x{}).", key, pack.name,
				e.getSourceWidth(), e.getSourceWidth(), e.getRequestedWidth(), e.getRequestedHeight());
		}
	}

	/**
	 * Loading localization from the pack.
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

package minicraft.screen;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.TransferHandler;

import minicraft.core.Initializer;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.saveload.Save;
import minicraft.screen.entry.ArrayEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.BookData;
import minicraft.util.Logging;

import minicraft.util.MyUtils;
import minicraft.util.ResourcePack;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

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
	 *
	 * [<tile_name>.png.json]
	 * 	├──	(animation object)
	 * 	│	└──	frametime int
	 * 	└──	(border object)
	 * 		├──	(key) String
	 * 		└──	(corner) String
	 */

	public static final MinicraftImage defaultLogo;

	private static final File FOLDER_LOCATION = new File(FileHandler.gameDir + "/resourcepacks");
	private static final ArrayList<ResourcePack> loadedPacks = new ArrayList<>(); // All currently loaded packs.
	private static final HashMap<ResourcePack, FileLock> packLocks = new HashMap<>();
	private final ArrayList<ResourcePack> resourcePacks = new ArrayList<>(); // All packs available.
	private final ArrayList<ResourcePack> loadQuery = new ArrayList<>(); // All packs intended to be loaded.
	private final ArrayList<ResourcePack> unloadedPacks = new ArrayList<ResourcePack>() { // Packs that are not loaded.
		private final Comparator<ResourcePack> sorter = (o1, o2) -> {
			if (o1 instanceof ResourcePack.ClassicArtResourcePack) return 1;
			if (o2 instanceof ResourcePack.ClassicArtResourcePack) return -1;
			return o1.getName().compareTo(o2.getName());
		};

		public boolean add(ResourcePack pack) {
			int index = Collections.binarySearch(this, pack, sorter);
			if (index < 0) index = ~index;
			super.add(index, pack);
			return true;
		}
	}; // Reference: https://stackoverflow.com/a/4903642.

	private static final int padding = 10;

	private final WatcherThread fileWatcher;
	private final TransferHandler origTransferHandler;
	private final ArrayList<ListEntry> entries0 = new ArrayList<>();
	private final ArrayList<ListEntry> entries1 = new ArrayList<>();
	private final Menu.Builder builder0;
	private final Menu.Builder builder1;

	static { // Initializing the default pack and logo.
		try {
			defaultLogo = new MinicraftImage(ImageIO.read(ResourcePackDisplay.class.getResourceAsStream("/assets/textures/misc/unknown_pack.png")));
		} catch (IOException e) {
			CrashHandler.crashHandle(e);
			throw new RuntimeException();
		}
	}

	/** Initializing the Display. */
	public ResourcePackDisplay() {
		super(true, true);
		initPacks();
		loadQuery.addAll(loadedPacks);
		Collections.reverse(loadQuery);

		// Left Hand Side
		builder0 = new Menu.Builder(false, 2, RelPos.LEFT)
			.setDisplayLength(8)
			.setPositioning(new Point(0, 60), RelPos.BOTTOM_RIGHT);

		// Right Hand Side
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
		origTransferHandler = Initializer.getFrame().getTransferHandler();
		Initializer.getFrame().setTransferHandler(new FileDropHandler());
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

	/** Reloading the entries to refresh the current pack list. */
	private void reloadEntries() {
		Function<ResourcePack, SelectEntry> getter = pack -> {
			ArrayList<ListEntry> entries = new ArrayList<>(Arrays.asList(StringEntry.useLines(Color.WHITE, false, pack.getName())));
			int compatibility = packCompatibility(pack);
			if (compatibility != 0)
				entries.addAll(Arrays.asList(StringEntry.useLines(Color.RED, compatibility < 0 ? "minicraft.displays.resource_packs.display.pack_made_for_older" :
					"minicraft.displays.resource_packs.display.pack_made_for_newer")));
			entries.addAll(Arrays.asList(StringEntry.useLines(Color.GRAY, false, pack.getDescription())));
			return new SelectEntry(pack.getName(), () -> Game.setDisplay(new PopupDisplay(null, entries.toArray(new ListEntry[0]))), false) {
				@Override
				public int getColor(boolean isSelected) {
					if (pack.getPackFormat() != ResourcePack.PACK_FORMAT) {
						return selection == 1 ? Color.tint(Color.RED, 1, true) : Color.RED;
					} else {
						return selection == 1 ? SelectEntry.COL_UNSLCT : super.getColor(isSelected);
					}
				}
			};
		};

		entries0.clear(); // First list: unloaded.
		for (ResourcePack pack : unloadedPacks) { // First list: all available unloaded resource packs.
			entries0.add(getter.apply(pack));
		}

		entries1.clear(); // Second list: to be loaded.
		for (ResourcePack pack : loadQuery) { // Second List: loaded resource packs.
			entries1.add(getter.apply(pack));
		}
	}

	/**
	 * Getting the compatibility of the specified pack.
	 * @param pack The pack to check.
	 * @return {@code 0} if the pack is fully compatible, which means the pack format is as same as the current supported one.
	 * Negative integer if the pack is made for an older version of Minicraft+.
	 * Positive integer if the pack is made for a newer version of Minicraft+.
	 */
	private static int packCompatibility(ResourcePack pack) {
		return Integer.compare(pack.getPackFormat(), ResourcePack.PACK_FORMAT);
	}

	/** Applying the reloaded entries into the display. */
	private void refreshEntries() {
		reloadEntries();
		Menu[] newMenus = new Menu[] {
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

	/** Reference: https://stackoverflow.com/a/39415436 */
	private static final class FileDropHandler extends TransferHandler {
		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			for (DataFlavor flavor : support.getDataFlavors()) {
				if (flavor.isFlavorJavaFileListType()) {
					return true;
				}
			}
			return false;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!this.canImport(support))
				return false;

			List<File> files;
			try {
				files = (List<File>) support.getTransferable()
					.getTransferData(DataFlavor.javaFileListFlavor);
			} catch (UnsupportedFlavorException | IOException ex) {
				// should never happen (or JDK is buggy)
				return false;
			}

			ArrayList<ListEntry> entries = new ArrayList<>();
			entries.add(new StringEntry("minicraft.displays.resource_packs.popups.display.add_pack_confirm", Color.WHITE));
			files.forEach(f -> entries.add(new StringEntry(f.getName(), Color.GRAY, false)));
			entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
				Localization.getLocalized("minicraft.display.popup.enter_confirm", Game.input.getMapping("select")),
				Localization.getLocalized("minicraft.display.popup.escape_cancel", Game.input.getMapping("exit"))
			)));

			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
				for (File file: files) {
					if (!file.toPath().startsWith(FOLDER_LOCATION.toPath())) { // The file is not in the folder.
						if (file.exists() && !new File(FOLDER_LOCATION, file.getName()).exists()) { // If there is no file originally.
							if (file.isDirectory()) {
								try {
									FileHandler.copyFolderContents(file.toPath(), FOLDER_LOCATION.toPath(), FileHandler.REPLACE_EXISTING, false);
								} catch (IOException e) {
									Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to copy folder {} to resource pack folder.", file.getPath());
								}
							} else if (file.isFile()) {
								try {
									Files.copy(file.toPath(), FOLDER_LOCATION.toPath());
								} catch (IOException e) {
									Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to copy folder {} to resource pack folder.", file.getPath());
								}
							}
						}
					}
				}

				return true;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 0), entries.toArray(new ListEntry[0])));
			return true;
		}
	}

	/** Watching the directory changes. Allowing hot-loading. */
	private final class WatcherThread extends Thread implements Closeable {
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
					ArrayList<File> files = new ArrayList<>();
					for (WatchEvent<?> event : watcher.take().pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.OVERFLOW)
							continue;

						@SuppressWarnings("unchecked")
						WatchEvent<Path> ev = (WatchEvent<Path>) event;
						Path filename = ev.context();
						files.add(FOLDER_LOCATION.toPath().resolve(filename).toFile());
					}

					if (files.size() > 0) {
						Logging.RESOURCEHANDLER_RESOURCEPACK.debug("Refreshing resource packs: {}.", files);
						refreshResourcePacks(files);
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
		fileWatcher.close(); // Removes watcher.
		Initializer.getFrame().setTransferHandler(origTransferHandler);
		Collections.reverse(loadQuery);
		if (!loadQuery.equals(loadedPacks)) // Changes applied.
			reloadResources(this);
		new Save();
	}

	/** Checking if the pack is movable in the menu. The position should be checked as valid. */
	private static boolean isMovable(ResourcePack pack, Direction dir) {
		if (pack instanceof ResourcePack.DefaultResourcePack.WorldBundledDefaultResourcePack)
			return false; // World-bundled pack is fixed.
		if (dir == Direction.DOWN || dir == Direction.UP) {
			return true;
		} else if (dir == Direction.LEFT || dir == Direction.RIGHT) {
			return !(pack instanceof ResourcePack.DefaultResourcePack.GameDefaultResourcePack);
		}

		return false;
	}

	@Override
	public void tick(InputHandler input) {
		// Overrides the default tick handler.
		boolean inputRightClicked = input.getKey("right").clicked;
		boolean inputLeftClicked = input.getKey("left").clicked;

		if (inputRightClicked || inputLeftClicked) {
			boolean successful = false;
			if (inputRightClicked) { // Move cursor to the second list.
				if (selection == 0) {
					onSelectionChange(0, 1);
					successful = true;
				}
			} else { // Move cursor to the first list.
				if (selection == 1) {
					onSelectionChange(1, 0);
					successful = true;
				}
			}

			if (successful) Sound.play("select");
			return;
		}

		boolean inputSRightClicked = input.getKey("shift-right").clicked;
		boolean inputSLeftClicked = input.getKey("shift-left").clicked;
		boolean inputSUpClicked = input.getKey("shift-up").clicked;
		boolean inputSDownClicked = input.getKey("shift-down").clicked;

		if (inputSRightClicked || inputSLeftClicked || inputSUpClicked || inputSDownClicked) {
			boolean successful = false;
			if (inputSRightClicked) { // Move the selected pack from the first to the second list.
				if (selection == 0 && unloadedPacks.size() > 0) {
					ResourcePack pack = unloadedPacks.get(menus[0].getSelection());
					if (isMovable(pack, Direction.RIGHT)) {
						loadQuery.add(0, unloadedPacks.remove(menus[0].getSelection()));
						successful = true;
					}
				}
			} else if (inputSLeftClicked) { // Move the selected pack from the second to the first list.
				if (selection == 1) { // loadQuery is always non-empty; default pack(s) exist(s).
					ResourcePack pack = loadQuery.get(menus[1].getSelection());
					if (isMovable(pack, Direction.LEFT)) {
						unloadedPacks.add(loadQuery.remove(menus[1].getSelection()));
						successful = true;
					}
				}
			} else if (inputSUpClicked) { // Move up the selected pack in the second list.
				if (selection == 1 && menus[1].getSelection() > 0) {
					ResourcePack pack = loadQuery.get(menus[1].getSelection());
					ResourcePack oPack = loadQuery.get(menus[1].getSelection() - 1);
					if (isMovable(pack, Direction.UP) && isMovable(oPack, Direction.DOWN)) {
						Collections.swap(loadQuery, menus[1].getSelection(), menus[1].getSelection() - 1);
						successful = true;
					}
				}
			} else { // Move down the selected pack in the second list.
				if (selection == 1 && menus[1].getSelection() < loadQuery.size() - 1) {
					ResourcePack pack = loadQuery.get(menus[1].getSelection());
					ResourcePack oPack = loadQuery.get(menus[1].getSelection() + 1);
					if (isMovable(pack, Direction.DOWN) && isMovable(oPack, Direction.UP)) {
						Collections.swap(loadQuery, menus[1].getSelection(), menus[1].getSelection() + 1);
						successful = true;
					}
				}
			}

			if (successful) {
				refreshEntries();
				Sound.play("select");
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
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.move", Game.input.getMapping("cursor-down"), Game.input.getMapping("cursor-up")), screen, Screen.h - 25, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.select", Game.input.getMapping("SELECT")), screen, Screen.h - 17, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.position"), screen, Screen.h - 9, Color.DARK_GRAY);

		ArrayList<ResourcePack> packs = selection == 0 ? unloadedPacks : loadQuery;
		if (packs.size() > 0) { // If there is any pack that can be selected.
			MinicraftImage logo = packs.get(menus[selection].getSelection()).getLogo();
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
	 * Loading pack metadata of the pack.
	 * @param file The file of the pack.
	 * @return The loaded pack with metadata. {@code null} if the pack could not be recognized as a valid pack;
	 * the pack is ignored.
	 */
	@Nullable
	public static ResourcePack loadPackMetadata(File file) {
		try {
			if (file.isDirectory()) { // Is a directory.
				File metaF = new File(file, "pack.json");
				if (metaF.isFile()) {
					try (FileInputStream in = new FileInputStream(metaF)) {
						JSONObject meta = new JSONObject(MyUtils.readStringFromInputStream(in));
						return new ResourcePack.DirectoryResourcePack(file, meta.getInt("pack_format"), file.getName(), meta.getString("description"));
					} catch (IOException | UncheckedIOException | NullPointerException | JSONException ignored) {}
				}
			} else if (file.isFile() && file.getName().endsWith(".zip")) { // Is a zip file.
				try (ZipFile zip = new ZipFile(file)) {
					try (InputStream in = zip.getInputStream(zip.getEntry("pack.json"))) {
						JSONObject meta = new JSONObject(MyUtils.readStringFromInputStream(in));
						return new ResourcePack.ZipFileResourcePack(file, meta.getInt("pack_format"), file.getName(), meta.getString("description"));
					}
				} catch (IOException | IllegalStateException | UncheckedIOException | NullPointerException | JSONException ignored) {}
			}
		} catch (SecurityException | NullPointerException ignored) {}

		return null;
	}

	private static void initFolder() {
		// Generate resource packs folder
		if (FOLDER_LOCATION.isFile()) {
			if (FOLDER_LOCATION.delete())
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Non-directory file %s is deleted successfully.", FOLDER_LOCATION);
			else
				throw new RuntimeException(String.format("Non-directory file %s is deleted unsuccessfully.", FOLDER_LOCATION));
		}
		if (FOLDER_LOCATION.mkdirs()) {
			Logging.RESOURCEHANDLER_RESOURCEPACK.info("Created resource packs folder at {}.", FOLDER_LOCATION);
		}
	}

	/**
	 * First initialization on the resource pack system.
	 * This should only be called ONCE only.
	 * Loading the resource packs when loading preferences.
	 * @param names The names of the packs.
	 */
	public static void initLoadedPacks(String[] names) {
		initFolder();

		// Getting the list of all available packs in packs folder.
		ArrayList<ResourcePack> resourcePacks = new ArrayList<>();
		resourcePacks.add(ResourcePack.DefaultResourcePack.DEFAULT_RESOURCE_PACK);
		resourcePacks.add(ResourcePack.ClassicArtResourcePack.CLASSIC_ART_RESOURCE_PACK);
		for (File file : Objects.requireNonNull(FOLDER_LOCATION.listFiles())) {
			ResourcePack pack = loadPackMetadata(file); // Read and add the .zip ZipFile files and directories to the resource pack list.
			if (pack != null) { // If the pack is valid.
				resourcePacks.add(pack);
			}
		}

		for (String name : new HashSet<>(Arrays.asList(names))) {
			for (ResourcePack pack : resourcePacks) {
				if (name.equals(pack.getIdentifier())) {
					loadedPacks.add(pack);
					break;
				}
			}
		}

		reloadResources(null);
	}

	private void initPacks() {
		initFolder();

		// Getting the list of all available packs in packs folder.
		for (File file : Objects.requireNonNull(FOLDER_LOCATION.listFiles())) {
			ResourcePack pack = loadPackMetadata(file); // Read and add the .zip ZipFile files and directories to the resource pack list.
			if (pack != null) { // If the pack is valid.
				resourcePacks.add(pack);
			}
		}

		for (ResourcePack pack : resourcePacks) {
			if (!loadedPacks.contains(pack)) {
				unloadedPacks.add(pack);
			}
		}
	}

	/**
	 * Refreshing the pack list by the urls.
	 * @param files The files under the folder to be refreshed.
	 */
	private void refreshResourcePacks(List<File> files) {
		for (File file : files) {
			ResourcePack pack = resourcePacks.stream().filter(p -> p.getFile().equals(file)).findAny().orElse(null);
			if (pack != null) { // Refresh the current.
				refreshResourcePack(pack);
			} else { // Add new pack as it should be existed.
				pack = loadPackMetadata(file);
				if (pack != null) {
					unloadedPacks.add(pack);
				}
			}
		}
	}

	private void refreshResourcePack(ResourcePack pack) {
		try {
			pack.refreshPack();
		} catch (ResourcePack.PackRestructureNeededException e) {
			loadQuery.remove(pack); // Unregistering the original pack instance.
			unloadedPacks.remove(pack);
			resourcePacks.remove(pack);
			pack = loadPackMetadata(pack.getFile()); // Getting the reidentified pack instance.
			if (pack != null)
				unloadedPacks.add(pack);
		}
	}

	/**
	 * Getting the names of the loaded packs. This should only be called by {@link minicraft.saveload.Save}.
	 * @return The names of currently loaded packs.
	 */
	public static ArrayList<String> getLoadedPacks() {
		ArrayList<String> packs = new ArrayList<>();
		for (ResourcePack pack : loadedPacks) {
			packs.add(pack.getIdentifier());
		}

		return packs;
	}

	/** Reloading all the resources with the currently packs to be loaded. */
	@SuppressWarnings("unchecked")
	public static void reloadResources(@Nullable ResourcePackDisplay display) {

		// Clear all previously loaded resources.
		Renderer.spriteLinker.resetSprites();
		Localization.resetLocalizations();
		BookData.resetBooks();
		Sound.resetSounds();
		SpriteAnimation.resetMetadata();
		for (ResourcePack pack : loadedPacks) {
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
		ArrayList<Localization.LocaleInformation> options = new ArrayList<>(Arrays.asList(Localization.getLocales()));
		options.sort((a, b) -> a.name.compareTo(b.name));
		((ArrayEntry<Localization.LocaleInformation>) Settings.getEntry("language")).setOptions(options.toArray(new Localization.LocaleInformation[0]));

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
				case "assets/textures/entity/": loadTextures(pack, SpriteType.Entity); break;
				case "assets/textures/gui/": loadTextures(pack, SpriteType.Gui); break;
				case "assets/textures/item/": loadTextures(pack, SpriteType.Item); break;
				case "assets/textures/tile/": loadTextures(pack, SpriteType.Tile); break;
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
			case Entity: path += "entity/"; break;
			case Gui: path += "gui/"; break;
			case Item: path += "item/"; break;
			case Tile: path += "tile/"; break;
		}

		ArrayList<String> pngs = pack.getFiles(path, (p, isDir) -> p.toString().endsWith(".png") && !isDir);
		if (type == SpriteType.Tile) {
			// Loading sprite sheet metadata.
			for (String m : pack.getFiles(path, (p, isDir) -> p.toString().endsWith(".png.json") && !isDir)) {
				try {
					JSONObject obj = new JSONObject(MyUtils.readStringFromInputStream(pack.getResourceAsStream(m)));
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
	 * @param pack The pack to be loaded.
	 */
	private static void loadLocalization(ResourcePack pack) {
		JSONObject langJSON = null;
		try {
			langJSON = new JSONObject(MyUtils.readStringFromInputStream(pack.getResourceAsStream("pack.json"))).optJSONObject("language");
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
				String json = MyUtils.readStringFromInputStream(pack.getResourceAsStream(f));
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
		for (String path : pack.getFiles("assets/books", (path, isDir) -> path.toString().endsWith(".txt") && !isDir))  {
			try {
				String book = BookData.loadBook(MyUtils.readStringFromInputStream(pack.getResourceAsStream(path)));
				switch (path) {
					case "assets/books/about.txt": BookData.about = () -> book; break;
					case "assets/books/credits.txt": BookData.credits = () -> book; break;
					case "assets/books/instructions.txt": BookData.instructions = () -> book; break;
					case "assets/books/antidous.txt": BookData.antVenomBook = () -> book; break;
					case "assets/books/story_guide.txt": BookData.storylineGuide = () -> book; break;
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
		for (String f : pack.getFiles("assets/sounds/", (path, isDir) -> path.toString().endsWith(".wav") && !isDir)) {
			String name = Paths.get(f).getFileName().toString();
			try {
				Sound.loadSound(name.substring(0, name.length() - 4), new BufferedInputStream(pack.getResourceAsStream(f)), pack.name);
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load audio: {} in pack : {}", f, pack.name);
			}
		}
	}
}

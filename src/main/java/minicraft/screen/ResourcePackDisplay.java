package minicraft.screen;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import minicraft.saveload.Save;
import minicraft.screen.entry.ArrayEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.BookData;
import minicraft.util.Logging;

import minicraft.util.MyUtils;
import minicraft.util.ResourcePack;
import minicraft.util.ResourcePackLoader;
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
	private static final HashMap<ResourcePack, ResourcePack.PackLock> packLocks = new HashMap<>(); // The pack locks in FileChannel to block other operations.
	private final ArrayList<ResourcePack> resourcePacks = new ArrayList<>(); // All packs available.
	private final ArrayList<ResourcePack> incompatiblePacks = new ArrayList<>(); // All incompatible packs.
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
			defaultLogo = new MinicraftImage(ImageIO.read(Objects.requireNonNull(ResourcePackDisplay.class.getResourceAsStream("/assets/textures/misc/unknown_pack.png"))));
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
		translateMenu(newSel);
	}

	private void translateMenu(int to) {
		menus[0].translate(-menus[0].getBounds().getLeft(), 0);
		menus[1].translate(Screen.w - menus[1].getBounds().getRight(), 0);
		if (to == 0) {
			if (menus[1].getBounds().getLeft() - menus[0].getBounds().getRight() < padding)
				menus[1].translate(menus[0].getBounds().getRight() - menus[1].getBounds().getLeft() + padding, 0);
		} else if (to == 1) {
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
					isSelected = isSelected && (selection == 0 && unloadedPacks.contains(pack) || selection == 1 && loadQuery.contains(pack));
					if (pack.getPackFormat() != ResourcePack.PACK_FORMAT) {
						return isSelected ? Color.tint(Color.RED, 1, true) : Color.RED;
					} else {
						return super.getColor(isSelected);
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
		translateMenu(selection);
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
					HashSet<File> files = new HashSet<>();
					for (WatchEvent<?> event : watcher.take().pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.OVERFLOW)
							continue;

						@SuppressWarnings("unchecked")
						WatchEvent<Path> ev = (WatchEvent<Path>) event;
						Path filename = ev.context();
						files.add(FOLDER_LOCATION.toPath().resolve(filename.getName(0)).toFile());
					}

					if (files.size() > 0) {
						Logging.RESOURCEHANDLER_RESOURCEPACK.debug("Refreshing resource packs: {}.", files);
						refreshResourcePacks(new ArrayList<>(files));
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

	/** Checking entry-interact-able cursor directions based on the current cursor. */
	private boolean isMovable(Direction dir) {
		if (dir == Direction.RIGHT) { // Move the selected pack from the first to the second list.
			if (selection == 0 && unloadedPacks.size() > 0)
				return isMovable(unloadedPacks.get(menus[0].getSelection()), dir);
			return false;
		} else if (dir == Direction.LEFT) { // Move the selected pack from the second to the first list.
			if (selection == 1) // loadQuery is always non-empty; default pack(s) exist(s).
				return isMovable(loadQuery.get(menus[1].getSelection()), dir);
			return false;
		} else if (dir == Direction.UP) { // Move up the selected pack in the second list.
			if (selection == 1 && menus[1].getSelection() > 0)
				return isMovable(loadQuery.get(menus[1].getSelection()), Direction.UP) && // This
					isMovable(loadQuery.get(menus[1].getSelection() - 1), Direction.DOWN); // Upper
		} else if (dir == Direction.DOWN) { // Move down the selected pack in the second list.
			if (selection == 1 && menus[1].getSelection() < loadQuery.size() - 1)
				return isMovable(loadQuery.get(menus[1].getSelection()), Direction.DOWN) && // This
					isMovable(loadQuery.get(menus[1].getSelection() + 1), Direction.UP); // Lower
		}

		return false;
	}

	@Override
	public void tick(InputHandler input) {
		// Overrides the default tick handler.
		boolean inputRightClicked = input.getKey("right").clicked;
		boolean inputLeftClicked = input.getKey("left").clicked;
		if (selection == 0 && unloadedPacks.size() == 0)
			inputRightClicked = true;
		if (selection == 1 && unloadedPacks.size() == 0)
			inputLeftClicked = false;

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
				if (isMovable(Direction.RIGHT)) {
					loadQuery.add(0, unloadedPacks.remove(menus[0].getSelection()));
					successful = true;
				}
			} else if (inputSLeftClicked) { // Move the selected pack from the second to the first list.
				if (isMovable(Direction.LEFT)) { // loadQuery is always non-empty; default pack(s) exist(s).
					unloadedPacks.add(loadQuery.remove(menus[1].getSelection()));
					successful = true;
				}
			} else if (inputSUpClicked) { // Move up the selected pack in the second list.
				if (isMovable(Direction.UP)) {
					Collections.swap(loadQuery, menus[1].getSelection(), menus[1].getSelection() - 1);
					successful = true;
				}
			} else { // Move down the selected pack in the second list.
				if (isMovable(Direction.DOWN)) {
					Collections.swap(loadQuery, menus[1].getSelection(), menus[1].getSelection() + 1);
					successful = true;
				}
			}

			if (successful) {
				refreshEntries();
				Sound.play("select");
			}

			return;
		}

		if (input.getKey("exit").clicked) {
			fileWatcher.close(); // Removes watcher.
			Initializer.getFrame().setTransferHandler(origTransferHandler);
			Collections.reverse(loadQuery);
			if (!loadQuery.equals(loadedPacks)) { // Changes applied.
				loadedPacks.clear();
				loadedPacks.addAll(loadQuery);
				reloadResources(false);
			}

			new Save();
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
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.move"), screen, Screen.h - 9, Color.DARK_GRAY);
		Font.drawCentered(Localization.getLocalized("minicraft.displays.resource_packs.display.help.select", Game.input.getMapping("SELECT")), screen, Screen.h - 17, Color.DARK_GRAY);
		ArrayList<String> help = new ArrayList<>();
		if (isMovable(Direction.LEFT))
			help.add(Localization.getLocalized("minicraft.displays.resource_packs.display.help.set_to_unload", "SHIFT-LEFT"));
		else if (isMovable(Direction.RIGHT))
			help.add(Localization.getLocalized("minicraft.displays.resource_packs.display.help.set_to_load", "SHIFT-RIGHT"));
		if (isMovable(Direction.DOWN))
			help.add(Localization.getLocalized("minicraft.displays.resource_packs.display.help.lower_priority", "SHIFT-DOWN"));
		if (isMovable(Direction.UP))
			help.add(Localization.getLocalized("minicraft.displays.resource_packs.display.help.upper_priority", "SHIFT-UP"));
		for (int i = 0; i < help.size(); i++)
			Font.drawCentered(help.get(i), screen, Screen.h - 25 - 8 * i, Color.DARK_GRAY);

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

		// Adding vanilla pack as the lowest priority if there is no.
		if (!loadedPacks.contains(ResourcePack.DefaultResourcePack.DEFAULT_RESOURCE_PACK)) {
			loadedPacks.add(0, ResourcePack.DefaultResourcePack.DEFAULT_RESOURCE_PACK);
		}

		reloadResources(true);
	}

	private void initPacks() {
		initFolder();

		// Getting the list of all available packs in packs folder.
		resourcePacks.add(ResourcePack.DefaultResourcePack.DEFAULT_RESOURCE_PACK);
		resourcePacks.add(ResourcePack.ClassicArtResourcePack.CLASSIC_ART_RESOURCE_PACK);
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
	private static void reloadResources(boolean isInitial) {
		ResourcePackLoadingDisplay loading = isInitial ? null : new ResourcePackLoadingDisplay();
		if (!isInitial) Game.setDisplay(loading);

		CompletableFuture<Void> future =
			CompletableFuture.runAsync(() -> {
				if (loading != null)
					loading.toProgress(0.05);

				packLocks.forEach((pack, lock) -> {
					try {
						lock.close();
					} catch (IOException ignored) {}
				}); // Releasing all locks.
				packLocks.clear();
				loadedPacks.forEach(pack -> {
					ResourcePack.PackLock lock = pack.lockFile();
					if (lock != null) packLocks.put(pack, lock);
				}); // Relocking current packs.
			});

		future = future.thenRunAsync(() -> {
			if (loading != null)
				loading.toProgress(0.1);

			// Clear all previously loaded resources.
			Renderer.spriteLinker.resetSprites();
			Localization.resetLocalizations();
			BookData.resetBooks();
			Sound.resetSounds();
			SpriteAnimation.resetMetadata();
		});

		for (int i = 0; i < loadedPacks.size(); i++) {
			ResourcePack pack = loadedPacks.get(i);
			int j = i;
			future = future.thenRunAsync(() -> {
				if (loading != null)
					loading.toProgress(0.1 + 0.8 * j / loadedPacks.size());

				ResourcePackLoader loader = ResourcePackLoader.getFormatSuitableResourcePackLoader(pack.getPackFormat());
				try (ResourcePack.PackResourceStream stream = pack.loadPack()) {
					loader.loadResources(stream);
				} catch (IOException | IllegalStateException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load pack {}, the loaded contents might not be complete; skipping...", pack.getName());
				}
			});
		}

		future = future.thenRunAsync(() -> {
			if (loading != null)
				loading.toProgress(0.95);

			SpriteAnimation.refreshAnimations();
			Renderer.spriteLinker.updateLinkedSheets();
			Localization.loadLanguage();
			ArrayList<Localization.LocaleInformation> options = new ArrayList<>(Arrays.asList(Localization.getLocales()));
			options.sort(Comparator.comparing(Localization.LocaleInformation::toString));
			((ArrayEntry<Localization.LocaleInformation>) Settings.getEntry("language")).setOptions(options.toArray(new Localization.LocaleInformation[0]));

			// Refreshing skins.
			SkinDisplay.refreshSkins();
			SkinDisplay.releaseSkins();

			if (loading != null)
				loading.toProgress(1);
		});

		if (loading == null) {
			while (true)
				try {
					future.get();
					break;
				} catch (InterruptedException | ExecutionException ignored) {}
		} else {
			CompletableFuture<Void> completableFuture = future;
			executorService.submit(() -> {
				Thread.sleep(100);
				completableFuture.join();
				return null;
			});
		}
	}

	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	private static class ResourcePackLoadingDisplay extends Display {
		private double progress = 0;
		private double displaying = 0;
		private double prevDisplay = 0;

		ResourcePackLoadingDisplay() {
			super(true, false, new Menu.Builder(true, 0, RelPos.CENTER)
				.setSize(31 * 8, 16)
				.createMenu());
		}

		@Override
		public void render(Screen screen) {
			super.render(screen);
			int offset = Screen.w/2 - 15 * 8 + (Screen.h/2 - 4) * Screen.w;
			for (int i = 0; i < 31 * 8; i++) {
				for (int j = 0; j < 16; j++) {
					screen.pixels[offset - 4 + i + (j - 4) * Screen.w] = Color.BLUE;
				}
			}

			for (int i = 0; i < displaying * 30 * 8; i++) {
				for (int j = 0; j < 8; j++)
					screen.pixels[offset + i + j * Screen.w] = 0x1FFFFFF;
			}
		}

		@Override
		public void tick(InputHandler input) {
			if (progress > displaying) {
				double diff = progress - prevDisplay;
				double chge = displaying - prevDisplay;
				displaying += -(chge+diff/4)*(chge-diff); // Quadratic curve.
				displaying = new BigDecimal(displaying).setScale(5, RoundingMode.HALF_EVEN).doubleValue();
				if (displaying > progress)
					displaying = progress;
			}

			if (displaying == 1) {
				executorService.submit(() -> {
					Game.exitDisplay(); // Completed.
					try {
						Thread.sleep(50);
					} catch (InterruptedException ignored) {}
					Game.exitDisplay();
				});
			}
		}

		private void toProgress(double progress) {
			prevDisplay = displaying;
			this.progress = progress;
		}
	}
}

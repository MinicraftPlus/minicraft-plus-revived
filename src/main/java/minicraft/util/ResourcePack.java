package minicraft.util;

import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.core.io.FileHandler;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Screen;
import minicraft.screen.ResourcePackDisplay;
import minicraft.screen.WorldSelectDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.image.ImagingOpException;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

/**
 * The object representation of a resource pack.
 */
public abstract class ResourcePack {
	/**
	 * The current resource pack format.
	 * 0 - before 2.2.0 OR INVALID;
	 * 1 - 2.2.0-(fill-in the blank); TODO fill-in the game version.
	 * 2 - (fill-in the blank)-latest;
	 */
	public static final int PACK_FORMAT = 2;

	protected final File file;
	protected int packFormat; // The pack format of the pack.
	protected String name; // The name of the pack.
	protected String description; // The description of the pack.
	protected MinicraftImage logo; // The logo of the pack.

	protected ResourcePack(File file, int packFormat, @NotNull String name, String desc) {
		this.file = file;
		this.packFormat = packFormat;
		this.name = name;
		this.description = desc;
		reloadLogo();
	}

	/**
	 * Getting the pack format of the resource pack.
	 * @return The pack format of the pack.
	 */
	public int getPackFormat() {
		return packFormat;
	}

	/**
	 * Getting the display name of the resource pack.
	 * @return The display name of the pack.
	 */
	@NotNull
	public String getName() {
		return name;
	}

	/**
	 * Getting the pack file of the resource pack.
	 * @return The file of the pack.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Getting the identifier used for identifying resource pack in options.
	 * @return The pack identifier.
	 */
	public String getIdentifier() {
		return "file/" + name;
	}

	/**
	 * Getting the description of the resource pack.
	 * @return The description of the pack.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Getting the logo of the resource pack.
	 * @return The logo of the pack.
	 */
	public MinicraftImage getLogo() {
		return logo;
	}

	/**
	 * Reloading the logo of the resource pack.
	 */
	protected abstract void reloadLogo();

	/**
	 * Refreshing the pack metadata in the resource pack menu.
	 * @throws PackRestructureNeededException if the pack is needed to be reloaded by classes again.
	 * @see ResourcePackDisplay#loadPackMetadata(File)
	 */
	public void refreshPack() throws PackRestructureNeededException {
		reloadMetadata();
		reloadLogo();
	}

	/**
	 * Refreshing the pack metadata in the resource pack menu.
	 * @throws PackRestructureNeededException if the pack is needed to be reloaded by classes again.
	 * @see ResourcePackDisplay#loadPackMetadata(File)
	 */
	protected abstract void reloadMetadata() throws PackRestructureNeededException;

	/**
	 * This is thrown if the pack is needed to be reloaded by classes again in {@link #refreshPack()}.
	 */
	public static class PackRestructureNeededException extends Exception {}

	/**
	 * Getting the pack resource collector.
	 * @return The pack resource collector stream.
	 * {@code null} if there is any error occurs.
	 */
	public abstract PackResourceStream loadPack();

	/**
	 * The resource collector of a resource pack.
	 */
	public static abstract class PackResourceStream implements AutoCloseable {
		private final ResourcePack pack;
		protected boolean closeRequested = false;

		protected PackResourceStream(ResourcePack pack) {
			this.pack = pack;
		}

		public ResourcePack getPack() {
			return pack;
		}

		@FunctionalInterface
		public interface FilesFilter { // Literally functioned.
			boolean check(Path path, boolean isDir);
		}

		/** Representing a file entry inside a resource pack.
		 * The path is relative to the root of the pack. */
		public static class PackEntry {
			private Path parent = null; // null if this entry is at the root.
			private boolean isParentSet = false;
			private String name = null; // This should not be null since the root is not used here.
			private Path fullPath = null;
			private final boolean isDir;

			public PackEntry(@Nullable Path parent, @NotNull String name, boolean isDir) {
				this.parent = parent;
				isParentSet = true;
				this.name = name;
				this.isDir = isDir;
			}
			public PackEntry(@NotNull Path path, boolean isDir) {
				fullPath = path;
				this.isDir = isDir;
			}

			public @Nullable Path getParent() {
				if (!isParentSet)
					return parent = fullPath.getParent();
				return parent;
			}
			public @NotNull String getFilename() {
				if (name == null)
					return name = fullPath.getFileName().toString();
				return name;
			}
			public @NotNull Path getFullPath() {
				if (fullPath == null)
					return fullPath = parent == null ? Paths.get(name) : parent.resolve(name);
				return fullPath;
			}
			public boolean isDir() { return isDir; }
		}

		/**
		 * Getting the subfiles under the specified path.
		 * @param path The entry directory path to be listed.
		 * @param filter The filter to be applied.
		 * @return The filtered (if any) subfile and subfolder list. Empty if not or invalid path.
		 * @throws IllegalStateException if this collector has been closed.
		 */
		@NotNull
		public abstract ArrayList<PackEntry> getFiles(@NotNull Path path, @Nullable FilesFilter filter) throws IllegalStateException;

		/**
		 * Getting the subfiles under the specified path.
		 * @param path The entry directory path to be listed.
		 * @param filter The filter to be applied.
		 * @return The filtered (if any) subfile and subfolder list. Empty if not or invalid path.
		 * @throws IllegalStateException if this collector has been closed.
		 */
		@NotNull
		public abstract ArrayList<PackEntry> getFiles(@NotNull String path, @Nullable FilesFilter filter) throws IllegalStateException;

		/**
		 * Getting whether the path is a directory inside the pack.
		 * @param path The relative path.
		 * @return {@code null} if the path is unknown or not existed.
		 * {@code true} if the path is a directory.
		 * @throws IllegalStateException if this collector has been closed.
		 */
		@Nullable
		@SuppressWarnings("unused") // Keeping for possibly later use.
		public abstract Boolean isDir(String path) throws IllegalStateException;

		/**
		 * Getting the input stream associated with the relative path.
		 * @param path The relative path of the resource pack root.
		 * @return The input stream by the relative path in the pack.
		 * {@code null} if there is not resource associated with the path or I/O error occurs.
		 * @throws IllegalStateException if this collector has been closed.
		 */
		public abstract InputStream getInputStream(String path) throws IllegalStateException;

		/**
		 * Closing this instance and relinquishing the underlying resources.
		 * After closing this instance, no more resources can be collected.
		 * @throws IOException if this resource cannot be closed.
		 */
		@Override
		public abstract void close() throws IOException;

		protected final void ensureOpen() throws IllegalStateException {
			if (closeRequested)
				throw new IllegalStateException("pack resource collector closed");
		}
	}

	/**
	 * Getting a valid lock on the associated resource pack file.
	 * @return The file lock on the pack.
	 * {@code null} if creating the lock failed.
	 */
	public PackLock lockFile() {
		// Reference: https://www.baeldung.com/java-lock-files#2-exclusive-locks-using-a-randomaccessfile.
		try {
			return new PackLock(file);
		} catch (IOException e) {
			return null;
		}
	}

	public static class PackLock implements Closeable {
		private final File file;
		private final RandomAccessFile randomAccessFile;
		private final FileChannel channel;
		private final FileLock lock;
		private boolean closeRequested = false;

		public PackLock(File file) throws FileNotFoundException {
			this.file = file;
			if (file.isFile()) {
				this.randomAccessFile = new RandomAccessFile(file, "rw");
				channel = randomAccessFile.getChannel();
				try {
					lock = channel.lock();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				randomAccessFile = null;
				channel = null;
				lock = null;
				file.setWritable(false, false);
			}
		}

		/**
		 * Checking whether the file lock is still valid.
		 * @return {@code true} if the lock is valid.
		 */
		public boolean isLockValid() {
			return lock.isValid();
		}

		@Override
		public void close() throws IOException {
			if (!closeRequested) {
				if (randomAccessFile != null) {
					lock.close();
					channel.close();
					randomAccessFile.close();
				} else {
					file.setWritable(true, false);
				}
			}
		}
	}

	public static class ZipFileResourcePack extends ResourcePack {
		/**
		 * The resource pack should be validated before constructing the instance.
		 * @param file The zip file of the resource pack.
		 * @param packFormat The pack format of the pack.
		 * @param filename The filename of the file.
		 * @param desc The description of the pack.
		 */
		public ZipFileResourcePack(File file, int packFormat, String filename, String desc) {
			super(file, packFormat, filename, desc);
		}

		@Override
		protected void reloadLogo() {
			try (ZipFile zipFile = new ZipFile(file)) {
				InputStream in = zipFile.getInputStream(zipFile.getEntry("pack.png"));
				if (in != null) {
					logo = new MinicraftImage(ImageIO.read(in));

					// Logo size verification.
					int h = logo.height;
					int w = logo.width;
					if (h == 0 || w == 0 || h % 8 != 0 || w % 8 != 0 ||
						h > 32 || w > Screen.w) {
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Unable to load logo in pack: {}: Unacceptable logo size: {};{}; loading default logo instead.", name, w, h);
						throw new ImagingOpException("");
					}

					return;
				} else {
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Pack logo not found in pack: {}; loading default logo instead.", name);
				}
			} catch (IOException | NullPointerException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load logo in pack: {}; loading default logo instead.", name);
			} catch (ImagingOpException ignored) {}

			// Using the default logo instead.
			logo = ResourcePackDisplay.defaultLogo;
		}

		@Override
		protected void reloadMetadata() throws PackRestructureNeededException {
			// Refresh pack format, name and description.
			if (file.exists()) {
				if (file.isFile())
					try (ZipFile zipFile = new ZipFile(file)) {
						try (InputStream in = zipFile.getInputStream(zipFile.getEntry("pack.json"))) {
							JSONObject meta = new JSONObject(MyUtils.readStringFromInputStream(in));
							packFormat = meta.getInt("pack_format");
							description = meta.getString("description");
						}

						return; // Reloaded successfully.
					} catch (IOException | UncheckedIOException | JSONException e) {
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load pack.json in pack: {}.", name);
					} catch (NullPointerException e) {
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "The file pack.json is no longer existed in pack: {}.", name);
					}
				else {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn("The pack is no longer a regular file: {}.", name);
				}
			} else {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn("The pack is no longer existed: {}.", name);
			}

			throw new PackRestructureNeededException();
		}

		@Override
		public PackResourceStream loadPack() {
			try {
				return new ZipFilePackResourceStream(file, this);
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load pack: {}; skipping...", name);
				return null;
			}
		}

		public static class ZipFilePackResourceStream extends PackResourceStream {
			private final ZipFile zipFile;

			private ZipFilePackResourceStream(File file, ResourcePack pack) throws IOException {
				super(pack);
				zipFile = new ZipFile(file);
			}

			@Override
			public @NotNull ArrayList<PackEntry> getFiles(@NotNull Path path, @Nullable FilesFilter filter) throws IllegalStateException {
				return getFiles(path.toString(), path, filter);
			}

			@Override
			public @NotNull ArrayList<PackEntry> getFiles(@NotNull String path, @Nullable FilesFilter filter) throws IllegalStateException {
				return getFiles(path, Paths.get(path), filter);
			}

			private @NotNull ArrayList<PackEntry> getFiles(@NotNull String pathStr, @NotNull Path path, @Nullable FilesFilter filter) throws IllegalStateException {
				ensureOpen();
				ArrayList<PackEntry> paths = new ArrayList<>();
				try {
					if (zipFile.getEntry(pathStr.replace('\\', '/')).isDirectory())
						for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
							ZipEntry entry = e.nextElement();
							Path parent;
							Path f = Paths.get(entry.getName());
							if ((parent = f.getParent()) != null && parent.equals(path) &&
								(filter == null || filter.check(f, entry.isDirectory()))) {
								paths.add(new PackEntry(f, entry.isDirectory()));
							}
						}
				} catch (IllegalStateException ignored) {}

				return paths;
			}

			@Override
			public @Nullable Boolean isDir(String path) throws IllegalStateException {
				ensureOpen();
				ZipEntry entry = zipFile.getEntry(path.replace('\\', '/'));
				if (entry == null) return null;
				return entry.isDirectory();
			}

			@Override
			public InputStream getInputStream(String path) throws IllegalStateException {
				ensureOpen();
				try {
					return zipFile.getInputStream(zipFile.getEntry(path.replace('\\', '/')));
				} catch (NullPointerException | IOException | IllegalStateException e) {
					return null;
				}
			}

			@Override
			public void close() throws IOException {
				if (!closeRequested) {
					zipFile.close();
					closeRequested = true;
				}
			}
		}
	}

	public static class DirectoryResourcePack extends ResourcePack {
		/**
		 * The resource pack should be validated before constructing the instance.
		 * @param file The directory file of the resource pack.
		 * @param packFormat The pack format of the pack.
		 * @param filename The filename of the file.
		 * @param desc The description of the pack.
		 */
		public DirectoryResourcePack(File file, int packFormat, String filename, String desc) {
			super(file, packFormat, filename, desc);
		}

		@Override
		protected void reloadLogo() {
			File logoF = new File(file, "pack.png");
			if (logoF.isFile()) {
				try (FileInputStream in = new FileInputStream(logoF)) {
					logo = new MinicraftImage(ImageIO.read(in));

					// Logo size verification.
					int h = logo.height;
					int w = logo.width;
					if (h == 0 || w == 0 || h % 8 != 0 || w % 8 != 0 ||
						h > 32 || w > Screen.w) {
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Unable to load logo in pack: {}: Unacceptable logo size: {};{}; loading default logo instead.", name, w, h);
						throw new ImagingOpException("");
					}

					return;
				} catch (IOException | SecurityException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load logo in pack: {}; loading default logo instead.", name);
				} catch (ImagingOpException ignored) {}
			}

			// Using the default logo instead.
			logo = ResourcePackDisplay.defaultLogo;
		}

		@Override
		protected void reloadMetadata() throws PackRestructureNeededException {
			// Refresh pack format, name and description.
			if (file.exists()) {
				if (file.isDirectory()) {
					File metaF = new File(file, "pack.json");
					if (metaF.exists()) {
						if (metaF.isFile()) {
							try (FileInputStream in = new FileInputStream(metaF)) {
								JSONObject meta = new JSONObject(MyUtils.readStringFromInputStream(in));
								packFormat = meta.getInt("pack_format");
								description = meta.getString("description");
								return; // Reloaded successfully.
							} catch (IOException | UncheckedIOException | NullPointerException | JSONException e) {
								Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load pack.json in pack: {}.", name);
							}
						} else {
							Logging.RESOURCEHANDLER_RESOURCEPACK.warn("The pack.json is no longer a regular file: {}.", name);
						}
					} else {
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("The pack.json is no longer existed: {}.", name);
					}
				} else {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn("The pack is no longer a directory: {}.", name);
				}
			} else {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn("The pack is no longer existed: {}.", name);
			}

			throw new PackRestructureNeededException();
		}

		@Override
		public PackResourceStream loadPack() {
			if (file.exists()) {
				if (file.isDirectory()) {
					return new DirectoryPackResourceStream(file, this);
				} else {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn("The pack is no longer a directory: {}; skipping...", name);
				}
			} else {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn("The pack is no longer existed: {}; skipping...", name);
			}

			return null;
		}

		public static class DirectoryPackResourceStream extends PackResourceStream {
			private final File file;

			private DirectoryPackResourceStream(File file, ResourcePack pack) {
				super(pack);
				this.file = file;
			}

			@Override
			public @NotNull ArrayList<PackEntry> getFiles(@NotNull Path path, @Nullable FilesFilter filter) throws IllegalStateException {
				return getFiles(file.toPath().resolve(path).toFile(), path, filter);
			}

			@Override
			public @NotNull ArrayList<PackEntry> getFiles(@NotNull String path, @Nullable FilesFilter filter) throws IllegalStateException {
				return getFiles(new File(file, path), Paths.get(path), filter);
			}

			private @NotNull ArrayList<PackEntry> getFiles(File file, @NotNull Path path, @Nullable FilesFilter filter) throws IllegalStateException {
				ensureOpen();
				ArrayList<PackEntry> paths = new ArrayList<>();
				if (file != null) try {
					String[] files;
					if (filter == null)
						files = file.list();
					else
						files = file.list((f, s) -> {
							File fl;
							return filter.check((fl = new File(f, s)).toPath(), fl.isDirectory());
						});
					if (files != null) {
						for (String f : files) {
							paths.add(new PackEntry(path, f, new File(file, f).isDirectory()));
						}
					}
				} catch (SecurityException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace(e, "Unable to list files in directory pack: {}.", file);
				} catch (IllegalStateException ignored) {}

				return paths;
			}

			@Override
			public @Nullable Boolean isDir(String path) throws IllegalStateException {
				ensureOpen();
				File f = new File(file, path);
				if (f.exists()) {
					if (f.isDirectory()) return true;
					if (f.isFile()) return false;
				}

				return null;
			}

			@Override
			public InputStream getInputStream(String path) throws IllegalStateException {
				ensureOpen();
				try {
					return Files.newInputStream(new File(file, path).toPath());
				} catch (NullPointerException | IOException | IllegalArgumentException | UnsupportedOperationException | SecurityException e) {
					return null;
				}
			}

			@Override
			public void close() {
				if (!closeRequested) {
					closeRequested = true;
				}
			}
		}
	}

	public static class DefaultResourcePack {
		/** The default game resource pack, other than the world-bundled one, in directory or in jar. */
		public static final GameDefaultResourcePack DEFAULT_RESOURCE_PACK;

		/** @see FileHandler#listResources() */
		private static final Path sourceResourceRootPath;
		private static final File sourceResourceRoot;
		private static final boolean isSourceJar;

		static {
			Path sourceResourceRootPathTemp = null;
			File sourceResourceRootTemp = null;
			boolean isSourceJarTemp = false;
			try {
				// Copied from FileHandler.
				CodeSource src = Game.class.getProtectionDomain().getCodeSource();
				if (src != null) {
					URL jar = src.getLocation();
					ZipInputStream zip = new ZipInputStream(jar.openStream());
					if (zip.available() == 1) {
						if (zip.getNextEntry() != null) { // Valid jar
							sourceResourceRootPathTemp = Paths.get(jar.toURI());
							sourceResourceRootTemp = sourceResourceRootPathTemp.toFile();
							isSourceJarTemp = true;
						} else { // Running from an IDE
							URL fUrl = Game.class.getResource("/");
							if (fUrl == null) {
								throw new IllegalStateException("Unexpected error: Unable to find the root of resources.");
							}

							Path p = Paths.get(fUrl.toURI());
							File f = p.toFile();
							//noinspection ReassignedVariable,ConstantConditions
							isSourceJarTemp = false;
							if (new File(f, "resources").exists()) {
								sourceResourceRootPathTemp = p;
								sourceResourceRootTemp = f;
							} else { // Providing a simple resolution when the previous location is invalid, the gradle build path is used.
								sourceResourceRootPathTemp = p.resolve("../../../resources/main");
								sourceResourceRootTemp = sourceResourceRootPathTemp.toFile();
							}
						}
					} else
						throw new IllegalStateException("Unexpected error: The code source is empty.");
				} else {
					throw new IllegalStateException("Unable to get code source.");
				}
			} catch (IOException | IllegalStateException | URISyntaxException e) {
				CrashHandler.crashHandle(e, new CrashHandler.ErrorInfo("Invalid Game Code Source",
					CrashHandler.ErrorInfo.ErrorType.UNEXPECTED, true, "Unable to get code source in order to get default resources."));
			}

			sourceResourceRootPath = Objects.requireNonNull(sourceResourceRootPathTemp);
			sourceResourceRoot = Objects.requireNonNull(sourceResourceRootTemp);
			isSourceJar = isSourceJarTemp;
			DEFAULT_RESOURCE_PACK = new GameDefaultResourcePack();
		}

		/**
		 * Constructing a new instance with the world-bundled default resource pack.
		 * @param worldName The world name associated with.
		 * @return The world-bundled default resource pack. {@code null} if there is no associated pack or world.
		 */
		@Nullable
		public static WorldBundledDefaultResourcePack newWorldBundledDefaultResourcePack(@Nullable String worldName) {
			if (worldName == null) return null;
			if (!WorldSelectDisplay.getWorldNames().contains(worldName)) return null; // There is no world.

			File file = new File(Game.gameDir + "/saves/" + worldName, "resources.zip");
			if (file.isFile()) {
				try (ZipFile zipFile = new ZipFile(file)) {
					try (InputStream in = zipFile.getInputStream(zipFile.getEntry("pack.json"))) {
						JSONObject meta = new JSONObject(MyUtils.readStringFromInputStream(in));
						return new WorldBundledDefaultResourcePack(file, meta.getInt("pack_format"), meta.has("description") ? meta.getString("description") + " (world)" : "(world)");
					}
				} catch (IOException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load world-bundled pack in world: {}.", worldName);
					return null;
				} catch (JSONException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load pack.json in world-bundled pack in world: {}.", worldName);
					return null;
				}
			} else
				return null;
		}

		/**
		 * The default resources of Minicraft+ with a pack control.
		 */
		public static class GameDefaultResourcePack extends ResourcePack {
			public static final String IDENTIFIER = "vanilla";

			private ResourcePack childHandler = null;

			private GameDefaultResourcePack() {
				super(sourceResourceRoot, PACK_FORMAT, "Default", "The default look and feel of Minicraft+ (built-in)");
			}

			// Initializing childHandler here but not in <init>, because other methods may be called first.
			private void init() {
				if (childHandler == null) {
					if (isSourceJar)
						childHandler = new ZipFileResourcePack(file, packFormat, name, description);
					else
						childHandler = new DirectoryResourcePack(file, packFormat, name, description);
				}
			}

			@Override
			public PackLock lockFile() {
				return null;
			}

			@Override
			public String getIdentifier() {
				return IDENTIFIER;
			}

			@Override
			public MinicraftImage getLogo() {
				return childHandler.getLogo();
			}

			@Override
			protected void reloadLogo() {
				init();
				childHandler.reloadLogo();
			}

			@Override
			public void refreshPack() throws PackRestructureNeededException {
				childHandler.refreshPack();
			}

			@Override
			protected void reloadMetadata() throws PackRestructureNeededException {
				init();
				childHandler.reloadMetadata();
			}

			@Override
			public PackResourceStream loadPack() {
				init();
				return childHandler.loadPack();
			}
		}

		/**
		 * The resource pack control for world specific resources.
		 * This pack has the most priority and is fixed and loaded before the world loading.
		 */
		public static class WorldBundledDefaultResourcePack extends ZipFileResourcePack {
			private WorldBundledDefaultResourcePack(File file, int packFormat, String desc) {
				super(file, packFormat, "World Specific Resources", desc);
			}

			@Override
			public String getIdentifier() {
				return null;
			}
		}
	}

	/**
	 * The classic textures of Minicraft+ in pack.
	 * This class could be simplified if loading external assets is possible.
	 */
	public static class ClassicArtResourcePack extends ZipFileResourcePack {
		public static final String IDENTIFIER = "classic_art";
		public static final ClassicArtResourcePack CLASSIC_ART_RESOURCE_PACK;

		private static final FileSystem fs;
		private static final Path path;

		static {
			FileSystem fsTemp;
			try {
				fsTemp = FileSystems.newFileSystem(DefaultResourcePack.sourceResourceRootPath, (ClassLoader) null);
			} catch (IOException e) {
				CrashHandler.crashHandle(e, new CrashHandler.ErrorInfo("Classic Art Resource Pack Invalid",
					CrashHandler.ErrorInfo.ErrorType.UNEXPECTED, true, "Unable to load the built-in classic resource pack."));
				fsTemp = null;
			}
			fs = fsTemp;
			assert fs != null;

			Path pathTemp;
			try {
				pathTemp = fs.getPath("assets/resourcepacks/classic_art.zip");
			} catch (InvalidPathException e) {
				CrashHandler.crashHandle(e, new CrashHandler.ErrorInfo("Classic Art Resource Pack Not Found",
					CrashHandler.ErrorInfo.ErrorType.UNEXPECTED, true, "Unable to get the classic resource pack by the path."));
				pathTemp = null;
			}
			path = pathTemp;
			assert path != null;

			ClassicArtResourcePack temp;
			try {
				temp = new ClassicArtResourcePack();
			} catch (URISyntaxException | UnsupportedOperationException | IllegalArgumentException |
					 FileSystemNotFoundException | SecurityException e) {
				CrashHandler.crashHandle(e, new CrashHandler.ErrorInfo("Classic Art Resource Pack Invalid",
					CrashHandler.ErrorInfo.ErrorType.UNEXPECTED, true, "Unable to load the built-in classic resource pack."));
				temp = null;
			}
			CLASSIC_ART_RESOURCE_PACK = temp;
		}

		private ClassicArtResourcePack() throws URISyntaxException {
			// Placeholder file.
			super(DefaultResourcePack.sourceResourceRoot, PACK_FORMAT, "Classic Art", "The classic look of Minicraft+ (built-in)");
		}

		@Override
		public String getIdentifier() {
			return IDENTIFIER;
		}

		@Override
		public PackLock lockFile() {
			return null;
		}

		// Assuming the file always exists and is a file.
		// Reference: https://stackoverflow.com/a/14603620.

		@Override
		protected void reloadLogo() {
			try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(path))) {
				InputStream in = null;
				for (ZipEntry e; (e = zin.getNextEntry()) != null;) {
					if (e.getName().equals("pack.png")) {
						byte[] bytes = new byte[(int) e.getSize()];
						int res = zin.read(bytes);
						if (res == e.getSize()) {
							in = new ByteArrayInputStream(bytes);
						} // Incomplete bytes read.
					}
				}

				if (in != null) {
					logo = new MinicraftImage(ImageIO.read(in));

					// Logo size verification.
					int h = logo.height;
					int w = logo.width;
					if (h == 0 || w == 0 || h % 8 != 0 || w % 8 != 0 ||
						h > 32 || w > Screen.w) {
						Logging.RESOURCEHANDLER_RESOURCEPACK.warn("Unable to load logo in pack: {}: Unacceptable logo size: {};{}; loading default logo instead.", name, w, h);
						throw new ImagingOpException("");
					}

					return;
				} else {
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Pack logo not found in pack: {}; loading default logo instead.", name);
				}
			} catch (IOException | NullPointerException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load logo in pack: {}; loading default logo instead.", name);
			} catch (ImagingOpException ignored) {}

			// Using the default logo instead.
			logo = ResourcePackDisplay.defaultLogo;
		}

		@Override
		protected void reloadMetadata() {
			// Assuming no metadata is needed to be reloaded.
		}

		@Override
		public PackResourceStream loadPack() {
			try {
				return new ClassicArtPackResourceStream(this);
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load pack: {}; skipping...", name);
				return null;
			}
		}

		private static class ClassicArtPackResourceStream extends PackResourceStream {
			private final ZipInputStream zin;
			private final HashMap<Path, ZipEntry> entries = new HashMap<>();

			protected ClassicArtPackResourceStream(ResourcePack pack) throws IOException {
				super(pack);
				zin = new ZipInputStream(Files.newInputStream(path));
				// Searching and indexing first.
				for (ZipEntry e; (e = zin.getNextEntry()) != null;) {
					entries.put(fs.getPath(e.getName()), e);
				}
			}

			@Override
			public @NotNull ArrayList<PackEntry> getFiles(@NotNull Path path, @Nullable FilesFilter filter) throws IllegalStateException {
				ensureOpen();
				ArrayList<PackEntry> paths = new ArrayList<>();

				try {
					for (Path p : entries.keySet()) {
						Path parent;
						if ((parent = p.getParent()) != null && parent.equals(path) &&
							(filter == null || filter.check(p, entries.get(p).isDirectory()))) {
							paths.add(new PackEntry(p, entries.get(p).isDirectory()));
						}
					}
				} catch (SecurityException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace(e, "Unable to list files in directory pack: {}.", getPack().getName());
				} catch (IllegalStateException ignored) {}

				return paths;
			}

			@Override
			public @NotNull ArrayList<PackEntry> getFiles(@NotNull String path, @Nullable FilesFilter filter) throws IllegalStateException {
				try {
					return getFiles(fs.getPath(path), filter);
				} catch (InvalidPathException e) {
					return new ArrayList<>();
				}
			}

			@Override
			public @Nullable Boolean isDir(String path) throws IllegalStateException {
				try {
					ZipEntry e = entries.get(fs.getPath(path));
					if (e == null) return null;
					return e.isDirectory();
				} catch (InvalidPathException e) {
					return null;
				}
			}

			@Override
			public InputStream getInputStream(String path) throws IllegalStateException {
				try {
					Path p = fs.getPath(path);
					if (entries.containsKey(p)) {
						zin.reset();
						for (ZipEntry entry; (entry = zin.getNextEntry()) != null;) {
							if (fs.getPath(entry.getName()).equals(p)) {
								byte[] bytes = new byte[(int) entry.getSize()];
								int res = zin.read(bytes);
								if (res == entry.getSize()) {
									return new ByteArrayInputStream(bytes);
								} // Incomplete bytes read.
							}
						}
					}
				} catch (InvalidPathException | IOException ignored) {}

				return null;
			}

			@Override
			public void close() throws IOException {
				if (!closeRequested) {
					entries.clear();
					zin.close();
				}
			}
		}
	}
}

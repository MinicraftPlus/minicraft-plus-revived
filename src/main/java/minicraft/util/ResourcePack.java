package minicraft.util;

import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.core.io.FileHandler;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Screen;
import minicraft.screen.ResourcePackDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
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
	 * @see ResourcePackDisplay#loadPackMetadata(URL)
	 */
	public void refreshPack() throws PackRestructureNeededException {
		reloadMetadata();
		reloadLogo();
	}

	/**
	 * Refreshing the pack metadata in the resource pack menu.
	 * @throws PackRestructureNeededException if the pack is needed to be reloaded by classes again.
	 * @see ResourcePackDisplay#loadPackMetadata(URL)
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
		protected boolean closeRequested = false;

		protected PackResourceStream() {}

		@FunctionalInterface
		public interface FilesFilter { // Literally functioned.
			boolean check(Path path, boolean isDir);
		}

		/**
		 * Getting the subfiles under the specified path.
		 * @param path The entry directory path to be listed.
		 * @param filter The filter to be applied.
		 * @return The filtered (if any) subfile and subfolder list. Empty if not or invalid path.
		 * @throws IllegalStateException if this collector has been closed.
		 */
		@NotNull
		public abstract ArrayList<String> getFiles(String path, @Nullable FilesFilter filter) throws IllegalStateException;

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
		 * @throws Exception if this resource cannot be closed.
		 */
		@Override
		public abstract void close() throws Exception;

		protected final void ensureOpen() throws IllegalStateException {
			if (closeRequested)
				throw new IllegalStateException("pack resource collector closed");
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
				return new ZipFilePackResourceStream(file);
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load pack: {}; skipping...", name);
				return null;
			}
		}

		public static class ZipFilePackResourceStream extends PackResourceStream {
			private final ZipFile zipFile;

			private ZipFilePackResourceStream(File file) throws IOException {
				zipFile = new ZipFile(file);
			}

			@Override
			public @NotNull ArrayList<String> getFiles(String path, @Nullable FilesFilter filter) throws IllegalStateException {
				ensureOpen();
				ArrayList<String> paths = new ArrayList<>();
				try {
					for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
						ZipEntry entry = e.nextElement();
						Path parent;
						if ((parent = Paths.get(entry.getName()).getParent()) != null && parent.equals(Paths.get(path)) &&
							(filter == null || filter.check(Paths.get(entry.getName()), entry.isDirectory()))) {
							paths.add(entry.getName());
						}
					}
				} catch (IllegalStateException ignored) {}

				return paths;
			}

			@Override
			public InputStream getInputStream(String path) throws IllegalStateException {
				ensureOpen();
				try {
					return zipFile.getInputStream(zipFile.getEntry(path));
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
					return new DirectoryPackResourceStream(file);
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

			private DirectoryPackResourceStream(File file) {
				this.file = file;
			}

			@Override
			public @NotNull ArrayList<String> getFiles(String path, @Nullable FilesFilter filter) throws IllegalStateException {
				ensureOpen();
				ArrayList<String> paths = new ArrayList<>();
				try {
					String[] files;
					if (filter == null)
						files = file.list();
					else
						files = file.list((f, s) -> {
							File fl;
							return filter.check((fl = new File(f, s)).toPath(), fl.isDirectory());
						});
					if (files != null)
						paths.addAll(Arrays.asList(files));
				} catch (SecurityException e) {
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace(e, "Unable to list files in directory pack: {}.", file);
				} catch (IllegalStateException ignored) {}

				return paths;
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

	/**
	 * The default resources of Minicraft+ with a pack control.
	 */
	public static class DefaultResourcePack extends ZipFileResourcePack {
		private static final ResourcePack DEFAULT_RESOURCE_PACK = new DefaultResourcePack();
		/** @see FileHandler#listResources() */
		private static final URL gameJar;
		static {
		}

		public static ResourcePack getGameDefaultResourcePack() {
			return DEFAULT_RESOURCE_PACK;
		}
		public static ResourcePack getWorldDefaultResourcePack() {

		}

		private DefaultResourcePack() {
			super(, PACK_FORMAT, "Default", "The default look and feel of Minicraft+ (built-in)");
		}
	}

	/**
	 * The classic textures of Minicraft+ in pack.
	 * This class could be simplified if loading external assets is possible.
	 */
	public static class ClassicArtResourcePack extends ResourcePack {
		public static final ClassicArtResourcePack CLASSIC_ART_RESOURCE_PACK = new ClassicArtResourcePack();

		private ClassicArtResourcePack() {
			super(, PACK_FORMAT, "Classic Art", "The classic look of Minicraft+ (built-in)");
		}
	}
}

package minicraft.util;

import minicraft.core.CrashHandler;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Screen;
import minicraft.screen.ResourcePackDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

	/**
	 * Checking if the provided pack format is incompatible to the current system.
	 * @param packFormat The pack format to check.
	 * @return {@code true} if this is compatible.
	 */
	public static boolean isIncompatible(int packFormat) {
		if (packFormat <= 0) return false; // Incompatible before 1 or unknown format.
		if (packFormat > PACK_FORMAT) return false; // Incompatible for future format (unknown).
		if (packFormat == PACK_FORMAT) return true; // Always compatible since the format is same.

		// TODO The following should be checked and updated to be compatible with the updated pack format.
		return true;
	}

	private final int packFormat; // The pack format of the pack.
	private final String filename; // The name of the pack.
	private final String description; // The description of the pack.
	private MinicraftImage logo; // The logo of the pack.

	public ResourcePack(int packFormat, String name, String desc) {
		this.packFormat = packFormat;
		this.filename = name;
		this.description = desc;
		refreshPack();
	}

	/**
	 * Getting the display name of the resource pack.
	 * @return the display name of the pack.
	 */
	@NotNull
	public String getName() {
		return filename;
	}

	/**
	 * Refreshing the pack metadata in the resource pack menu.
	 * @throws PackRestructureNeededException if the pack is needed to be reloaded by classes again.
	 * @see ResourcePackDisplay#loadPackMetadata(URL)
	 */
	public abstract void refreshPack() throws PackRestructureNeededException;

	/**
	 * This is thrown if the pack is needed to be reloaded by classes again in {@link #refreshPack()}.
	 */
	public static class PackRestructureNeededException extends Exception {}

	/**
	 * Getting the pack resource collector.
	 */
	public abstract void loadPack();

	/**
	 * The resource collector of a resource pack.
	 */
	public static abstract class PackResourceStream implements AutoCloseable {
		@FunctionalInterface
		public interface FilesFilter { // Literally functioned.
			boolean check(Path path, boolean isDir);
		}

		/**
		 * Getting the subfiles under the specified path.
		 * @param path The entry directory path to be listed.
		 * @param filter The filter to be applied.
		 * @return The filtered (if any) subfile and subfolder list. Empty if not or invalid path.
		 */
		@NotNull
		public abstract ArrayList<String> getFiles(String path, FilesFilter filter);

		/**
		 * Getting the input stream associated with the relative path.
		 * @param path The relative path of the resource pack root.
		 * @return The input stream by the relative path in the pack.
		 * {@code null} if there is not resource associated with the path.
		 */
		public abstract InputStream getResourceAsStream(String path);
	}

	public static class UnknownResourcePack extends ResourcePack {
		public UnknownResourcePack(int packFormat, String filename, String desc) {
			super(packFormat, filename, desc);
		}

		@Override
		public void refreshPack() {

		}
	}

	public static class ZipFileResourcePack extends ResourcePack {
		private URL packRoot;

		private final String filename; // The name of the pack.
		private final String description; // The description of the pack.
		private MinicraftImage logo; // The logo of the pack.

		private boolean opened = false; // If the zip file stream is opened.
		private ZipFile zipFile = null; // The zip file stream.

		public ResourcePack(URL packRoot, int packFormat, String name, String desc) {
			this.packRoot = packRoot;
			this.packFormat = packFormat;
			this.filename = name;
			this.description = desc;
			refreshPack();
		}

		/**
		 * This does not include metadata refresh.
		 */
		public void refreshPack() {
			// Refresh pack logo.png, name and description.
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
					Logging.RESOURCEHANDLER_RESOURCEPACK.trace("Pack logo not found in pack: {}, loading default logo instead.", filename);
					logo = ResourcePackDisplay.defaultLogo;
				}
				close();

			} catch (IOException | NullPointerException e) {
				Logging.RESOURCEHANDLER_RESOURCEPACK.warn(e, "Unable to load logo in pack: {}, loading default logo instead.", filename);
				if (this == ResourcePackDisplay.defaultPack) {
					try {
						logo = new MinicraftImage(ImageIO.read(getClass().getResourceAsStream("/resources/logo.png")));
					} catch (IOException e1) {
						CrashHandler.crashHandle(e1);
					}
				} else logo = ResourcePackDisplay.defaultLogo;
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
		private ArrayList<String> getFiles(String path, ResourcePack.FilesFilter filter) {
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
}

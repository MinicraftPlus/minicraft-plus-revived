package minicraft.util.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import minicraft.gfx.MinicraftImage;

public abstract class ResourcePack implements AutoCloseable {
	private static MinicraftImage DEFAULT_LOGO;

	private final String name;

	@Nullable
	protected ResourcePackMetadata packMetadata;

	public ResourcePack(String name) {
		this.name = name;
	}

	/**
	 * @param path The path of the file
	 * @return The InputStream of the file or {@code null} if the file does not exist
	 * @throws IOException
	 */
	@Nullable
	abstract public InputStream getFile(String path) throws IOException;

	abstract public void getAllFiles(String beginPath, Predicate<String> filePathPredicate, FindResultConsumer consumer);

	public static interface FindResultConsumer extends BiConsumer<Path, InputStream> {}

	/**
	 * Returns the name of the resource pack
	 * @return The name of the resource pack
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the resource pack metadata, with includes the logo, display name, description and packFormat.
	 *
	 * @return {@link ResourcePackMetadata} or {@code null} if the it failed to load the metadata
	 */
	@Nullable
	public ResourcePackMetadata getResourcePackMetadata() {
		if (this.packMetadata == null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.getFile("pack.json")));
				JSONObject object = new JSONObject(String.join("\n", reader.lines().toArray(String[]::new)));

				String mName = object.optString("name", this.getName());
				String mDescription = object.optString("description", "");
				int mPackFormat = object.getInt("pack_format");

				this.packMetadata = new ResourcePackMetadata(this.loadLogo(), mName, mDescription, mPackFormat);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		return this.packMetadata;
	}

	@Nullable
	private MinicraftImage loadLogo() {
		if (DEFAULT_LOGO == null) {
			try {
				DEFAULT_LOGO = new MinicraftImage(ImageIO.read(ResourcePack.class.getResourceAsStream("/resources/default_pack.png")));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		try {
			MinicraftImage logo = new MinicraftImage(ImageIO.read(this.getFile("pack.png")));

			if (logo.width > 0 && logo.height > 0 && logo.width % 8 == 0 && logo.height % 8 == 0) {
				return logo;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}

		return DEFAULT_LOGO;
	}

	@Override
	public void close() {
	}

	public class ResourcePackMetadata {
		private final MinicraftImage logo;
		private final String name;
		private final String description;
		private final int packFormat;

		ResourcePackMetadata(MinicraftImage logo, String name, String description, int packFormat) {
			this.logo = logo;
			this.name = name;
			this.description = description;
			this.packFormat = packFormat;
		}

		public MinicraftImage logo() {
			return this.logo;
		}

		public String name() {
			return this.name;
		}

		public String description() {
			return this.description;
		}

		public int packFormat() {
			return this.packFormat;
		}
	}

	@Nullable
	public static ResourcePack getFromPath(Path path) {
		if (Files.isDirectory(path, new LinkOption[0]) && Files.isRegularFile(path.resolve("pack.json"), new LinkOption[0])) {
			return new FolderResourcePack(path.getFileName().toString(), path);
		} else if (path.getFileName().toString().endsWith(".zip") && Files.isRegularFile(path, new LinkOption[0])) {
			return new ZipResourcePack(path.getFileName().toString(), path.toFile());
		}

		return null;
	}
}

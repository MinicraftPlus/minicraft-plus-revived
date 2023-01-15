package minicraft.util.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Resource {
	private final InputStream inputStream;
	private final ResourcePack pack;
	private final Path path;

	public Resource(ResourcePack pack, Path path, InputStream inputStream) {
		this.pack = pack;
		this.path = path;
		this.inputStream = inputStream;
	}

	/**
	 * Returns the resource pack where this resource was found.
	 *
	 * @return The {@link ResourcePack} where this resource was found
	 */
	public ResourcePack getResourcePack() {
	  	return this.pack;
	}

	/**
	 * Returns the name of the resource pack where this resource was found.
	 *
	 * @return The name of the {@link ResourcePack} where this resource was found
	 */
	public String getResourcePackName() {
	  	return this.pack.getName();
	}

	/**
	 * Returns the name of the file with the extension.
	 *
	 * @return The name of the file with the extension
	 */
	public String getName() {
		return this.path.getFileName().toString();
	}

	/**
	 * Returns the path of the resource relative to the resource pack root.
	 *
	 * @return the path of the resource relative to the resource pack root
	 */
	public Path getPath() {
	  	return this.path;
	}

	/**
	 * Returns the resource content as an {@link InputStream}
	 *
	 * @return The resource content as an {@link InputStream}
	 * @throws IOException
	 */
	public InputStream getAsInputStream() throws IOException {
		return this.inputStream;
	}

	/**
	 * Returns the resource content as a {@link BufferedReader}.
	 * Useful shorthand since most of the time a reader is more useful that just the raw input stream.
	 *
	 * @return The resource content as a {@link BufferedReader}
	 * @throws IOException
	 */
	public BufferedReader getAsReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.inputStream, StandardCharsets.UTF_8));
	}

	/**
	 * Returns the resource content as text
	 *
	 * @return The resource content as text
	 * @throws IOException
	 */
	public String getAsString() throws IOException {
		return String.join("\n", this.getAsReader().lines().toArray(String[]::new));
	}
}

package minicraft.util.resource;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

public interface ResourceManager {
	/**
	 * Gets a file as a Resource from the topmost resource pack.
	 *
	 * @param path The path of the file
	 * @return The file as a {@link Resource} or {@code null} if the file was not found in any resource pack
	 */
	@Nullable
	public Resource getResource(String path);

	/**
	 * Gets all files that start the beginPath and satisfy the filePathPredicate.
	 * It searches from topmost to bottommost until it finds a resource.
	 *
	 * @param beginPath How the path has to start with
	 * @param filePathPredicate Predicate for the path
	 * @return The list of files as {@link Resource}
	 */
	public List<Resource> getResources(String beginPath, Predicate<String> filePathPredicate);

	/**
	 * The first resource pack in the list is the bottommost.
	 *
	 * @return The list of resource packs
	 */
	public List<ResourcePack> getResourcePacks();
}

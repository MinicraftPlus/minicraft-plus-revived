package minicraft.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

public class SyncReloadableResourceManager implements ResourceManager, AutoCloseable {
	private final List<SyncReloader> reloaders = new ArrayList<>();
	private List<ResourcePack> packs = new ArrayList<>();

	public void registerReloader(SyncReloader reloader) {
		this.reloaders.add(reloader);
	}

	/**
	 * Reloads everything synchronously with the provived resource packs
	 *
	 * @param packs The list of resource packs
	 */
	public void reload(List<ResourcePack> packs) {
		this.closePacks();
		this.packs = packs;

		for (SyncReloader reloader : this.reloaders) {
			reloader.reload(this);
		}
	}

	@Nullable
	public Resource getResource(String path) {
		for (int i = this.packs.size() - 1; i >= 0; --i) {
			ResourcePack pack = this.packs.get(i);

			InputStream inputStream;
			try {
				if ((inputStream = pack.getFile(path)) != null) {
					return new Resource(pack, path, inputStream);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public List<Resource> getResources(String path) {
		return getResources(path, p -> p.endsWith(path));
	}

	public List<Resource> getResources(String beginPath, Predicate<String> filePathPredicate) {
		List<Resource> list = new ArrayList<>();

		for (int i = this.packs.size() - 1; i >= 0; --i) {
			ResourcePack pack = this.packs.get(i);

			pack.getAllFiles(beginPath, filePathPredicate, (p, s) -> {
				list.add(new Resource(pack, p, s));
			});
		}

		return list;
	}

	public List<SyncReloader> getReloaders() {
	  return this.reloaders;
	}

	public List<ResourcePack> getResourcePacks() {
	  return this.packs;
	}

	private void closePacks() {
		this.packs.forEach(ResourcePack::close);
	}

	@Override
	public void close() {
		this.closePacks();
	}

	public interface SyncReloader {
		void reload(ResourceManager manager);
	}
}

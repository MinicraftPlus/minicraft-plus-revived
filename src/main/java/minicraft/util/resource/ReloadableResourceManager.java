package minicraft.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import minicraft.util.MyUtils;
import minicraft.util.resource.ResourceLoader.OnComplete;
import minicraft.util.resource.reloader.ResourceReloader;

public class ReloadableResourceManager implements ResourceManager, AutoCloseable {
	private final List<ResourceReloader> reloaders = new ArrayList<>();
	private List<ResourcePack> packs = new ArrayList<>();

	public void registerReloader(ResourceReloader reloader) {
		this.reloaders.add(reloader);
	}

	/**
	 * Reloads everything with the provived resource packs
	 *
	 * @param packs The list of resource packs
	 */
	public ResourceLoader reload(List<ResourcePack> packs, OnComplete onComplete) {
		this.closePacks();
		this.packs = packs;

		return new ResourceLoader(this.reloaders, this, onComplete);
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

		return MyUtils.reverseList(list);
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
}

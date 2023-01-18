package minicraft.util.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minicraft.core.io.FileHandler;
import minicraft.util.resource.provider.ResourcePackProvider;

public class ResourcePackManager {
	private final Map<String, ResourcePack> packs = new HashMap<>();
	private final List<ResourcePack> enabledPacks = new ArrayList<>();
	private final List<ResourcePackProvider> providers = new ArrayList<>();

	public ResourcePackManager() {
	}

	public void addProvider(ResourcePackProvider provider) {
		this.providers.add(provider);
	}

	public void findPacks() {
		packs.clear();

		this.providers.forEach(provider -> {
			provider.provide(pack -> {
				this.packs.put(pack.getName(), pack);
			});
		});

		File file = new File(FileHandler.gameDir, "resourcepacks");

		for (File f : file.listFiles()) {
			ResourcePack pack = ResourcePack.getFromPath(f.toPath());

			if (pack != null) {
				packs.put(pack.getName(), pack);
			}
		}
	}

	public void setEnabledPacks(List<String> enabledPacks) {
		this.enabledPacks.clear();

		for (Map.Entry<String, ResourcePack> pack : packs.entrySet()) {
			if (enabledPacks.contains(pack.getKey())) {
				this.enabledPacks.add(pack.getValue());
			}
		}
	}

	public List<ResourcePack> getEnabledPacks() {
	 	return this.enabledPacks;
	}

}

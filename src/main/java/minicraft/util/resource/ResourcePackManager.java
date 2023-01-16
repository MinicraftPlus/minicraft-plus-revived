package minicraft.util.resource;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import minicraft.core.io.FileHandler;

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

	public interface ResourcePackProvider {
		void provide(Consumer<ResourcePack> consumer);
	}

	public static class DefaultResourcePackProvider implements ResourcePackProvider {
		private final ResourcePack defaultResourcePack;

		public DefaultResourcePackProvider(ResourcePack defaultResourcePack) {
			this.defaultResourcePack = defaultResourcePack;
		}

		@Override
		public void provide(Consumer<ResourcePack> consumer) {
			consumer.accept(this.defaultResourcePack);

			defaultResourcePack.getAllFiles("assets/resources/", p -> p.endsWith(".zip"), (p, s) -> {
			});
		}
	}

	public static abstract class WorldResourcePackProvider implements ResourcePackProvider  {
		private final File worldResourcePacks;

		public WorldResourcePackProvider(Path worldPath) {
			this.worldResourcePacks = worldPath.resolve("resourcepacks").toFile();
		}

		@Override
		public void provide(Consumer<ResourcePack> consumer) {
			for (File file : this.worldResourcePacks.listFiles()) {
				ResourcePack pack = ResourcePack.getFromPath(file.toPath());

				if (pack != null) {
					consumer.accept(pack);
				}
			}
		}
	}
}

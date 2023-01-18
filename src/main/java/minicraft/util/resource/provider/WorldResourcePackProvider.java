package minicraft.util.resource.provider;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

import minicraft.util.resource.ResourcePack;

public class WorldResourcePackProvider implements ResourcePackProvider  {
	private final File worldResourcePacks;

	public WorldResourcePackProvider(Path worldPath) {
		this.worldResourcePacks = worldPath.resolve("resourcepacks").toFile();
	}

	@Override
	public void provide(Consumer<ResourcePack> consumer) {
		if (!this.worldResourcePacks.exists()) return;

		for (File file : this.worldResourcePacks.listFiles()) {
			ResourcePack pack = ResourcePack.getFromPath(file.toPath());

			if (pack != null) {
				consumer.accept(pack);
			}
		}
	}
}

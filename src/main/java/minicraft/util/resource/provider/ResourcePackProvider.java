package minicraft.util.resource.provider;

import java.util.function.Consumer;

import minicraft.util.resource.ResourcePack;

public interface ResourcePackProvider {
	void provide(Consumer<ResourcePack> consumer);
}

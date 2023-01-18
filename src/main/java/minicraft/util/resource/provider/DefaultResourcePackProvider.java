package minicraft.util.resource.provider;

import java.util.function.Consumer;

import minicraft.util.resource.ResourcePack;

public class DefaultResourcePackProvider implements ResourcePackProvider {
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

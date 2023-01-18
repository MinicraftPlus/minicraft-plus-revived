package minicraft.util.resource.reloader;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import minicraft.util.resource.ResourceManager;

public abstract class SyncResourceReloader implements ResourceReloader {
	@Override
	public final CompletableFuture<Void> reload(Executor executor, ResourceManager manager) {
	  return CompletableFuture.runAsync(() -> this.reload(manager), executor);
	}

	protected abstract void reload(ResourceManager manager);
}

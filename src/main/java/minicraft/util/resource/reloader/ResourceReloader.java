package minicraft.util.resource.reloader;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import minicraft.util.resource.ResourceManager;

public interface ResourceReloader {
	CompletableFuture<Void> reload(Executor executor, ResourceManager manager);
}

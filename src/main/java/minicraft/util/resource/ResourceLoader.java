package minicraft.util.resource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import minicraft.util.MyUtils;
import minicraft.util.resource.reloader.ResourceReloader;

public class ResourceLoader {
	private final AtomicInteger completed;
	private final int total;

	public ResourceLoader(List<ResourceReloader> reloaders, ResourceManager manager, OnComplete onComplete) {
		this.completed = new AtomicInteger(); // We don't want racing.
		this.total = reloaders.size();

		CompletableFuture<?>[] futures = new CompletableFuture[reloaders.size()];

		for (int i = 0; i < this.total; i++) {
			futures[i] = reloaders.get(i).reload(MyUtils.RELOADER_WORKER, manager).whenComplete((_v0, _v1) -> {
				this.completed.incrementAndGet();
			});
		}

		CompletableFuture.allOf(futures).whenComplete((_v0, _v1) -> {
			try {
				Thread.sleep(1000); // Just to see the progress bar at 100% :)
			} catch (InterruptedException ignored) {}

			onComplete.call();
		});
	}

	public float getProgress() {
		return this.completed.get() / (float)this.total;
	}

	public interface OnComplete {
		void call();
	}
}

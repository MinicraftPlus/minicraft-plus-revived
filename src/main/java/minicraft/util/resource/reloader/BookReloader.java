package minicraft.util.resource.reloader;

import java.io.IOException;

import minicraft.util.BookData;
import minicraft.util.Logging;
import minicraft.util.resource.Resource;
import minicraft.util.resource.ResourceManager;
import minicraft.util.resource.SyncReloadableResourceManager;

public class BookReloader implements SyncReloadableResourceManager.SyncReloader {
	@Override
	public void reload(ResourceManager manager) {
		for (Resource resource : manager.getResources("assets/books/", p -> p.endsWith(".txt"))) {
			try {
				String book = BookData.loadBook(resource.getAsString());

				switch (resource.getPath().toString()) {
					case "assets/books/about.txt": BookData.about = () -> book; break;
					case "assets/books/credits.txt": BookData.credits = () -> book; break;
					case "assets/books/instructions.txt": BookData.instructions = () -> book; break;
					case "assets/books/antidous.txt": BookData.antVenomBook = () -> book; break;
					case "assets/books/story_guide.txt": BookData.storylineGuide = () -> book; break;
				}
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load book: {} in pack : {}", resource.getName(), resource.getResourcePackName());
			}
		}
	}
}

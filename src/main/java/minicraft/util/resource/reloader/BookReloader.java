package minicraft.util.resource.reloader;

import java.io.BufferedReader;
import java.io.IOException;

import minicraft.util.BookData;
import minicraft.util.Logging;
import minicraft.util.MyUtils;
import minicraft.util.resource.Resource;
import minicraft.util.resource.ResourceManager;
import minicraft.util.resource.SyncReloadableResourceManager;

public class BookReloader implements SyncReloadableResourceManager.SyncReloader {
	@Override
	public void reload(ResourceManager manager) {
		BookData.resetBooks();

		for (Resource resource : manager.getResources("assets/books/", p -> p.endsWith(".txt"))) {
			try (BufferedReader reader = resource.getAsReader()) {
				String book = BookData.loadBook(MyUtils.readAsString(reader));

				switch (resource.getPath()) {
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

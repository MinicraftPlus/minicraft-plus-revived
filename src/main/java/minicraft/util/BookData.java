package minicraft.util;

import java.io.BufferedReader;
import java.io.IOException;

import minicraft.item.BookItem;
import minicraft.util.resource.Resource;
import minicraft.util.resource.ResourceManager;
import minicraft.util.resource.reloader.ResourceReloader;
import minicraft.util.resource.reloader.SyncResourceReloader;

public class BookData {
	public static BookItem.BookContent about;
	public static BookItem.BookContent credits;
	public static BookItem.BookContent instructions;
	public static BookItem.BookContent antVenomBook;
	public static BookItem.BookContent storylineGuide;

	public static final ResourceReloader reloader = new SyncResourceReloader() {
		protected void reload(ResourceManager manager) {
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
	};

	public static void resetBooks() {
		about = null;
		credits = null;
		instructions = null;
		antVenomBook = null;
		storylineGuide = null;
	}

	public static String loadBook(String content) {
		return content.replaceAll("\\\\0", "\0");
	}

	public static void saveBook(String bookTitle) {
	}
}

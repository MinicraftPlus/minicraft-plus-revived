package minicraft.util;

import minicraft.item.BookItem;

import java.util.HashMap;

public class BookData {

	public static BookItem.BookContent about;
	public static BookItem.BookContent credits;
	public static BookItem.BookContent instructions;
	public static BookItem.BookContent antVenomBook;
	public static BookItem.BookContent storylineGuide;

	private static final HashMap<String, LoreBook> loreBooks = new HashMap<>();

	public static final LoreBookKey lore1 = new LoreBookKey("lore1");
	public static final LoreBookKey lore2 = new LoreBookKey("lore2");

	public static class LoreBook {
		public final String title; // First line is the title.
		public final BookItem.BookContent content; // The text left is the content.

		public LoreBook(String title, String content) {
			this.title = title;
			this.content = () -> content;
		}
	}

	public static class LoreBookKey {
		private final String key;

		private LoreBookKey(String key) {
			this.key = key;
		}

		public final LoreBook getBook() {
			return loreBooks.get(key);
		}

		public final void loadBook(LoreBook book) {
			loreBooks.put(key, book);
		}
	}

	public static void resetBooks() {
		about = null;
		credits = null;
		instructions = null;
		antVenomBook = null;
		storylineGuide = null;
		loreBooks.clear();
	}

	public static String loadBook(String content) {
		return content.replaceAll("\\\\0", "\0");
	}

	public static void saveBook(String bookTitle) {

	}
}

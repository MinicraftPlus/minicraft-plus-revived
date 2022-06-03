package minicraft.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import minicraft.core.io.Localization;
import minicraft.saveload.Load;

public class BookData {
	public static final BookData about = loadStaticBook("about", "About");
	public static final BookData credits = loadStaticBook("credits", "Credits");
	public static final BookData instructions = loadStaticBook("instructions", "Instructions");
	public static final BookData antVenomBook = loadStaticBook("antidous", "Antidious Venomi");
	public static final BookData storylineGuide = loadStaticBook("story_guide", "Storyline Guide");

	public String title;
	public String content;
	public String author;

	public BookData(String title, String content, String author) {
		this.title = title;
		this.content = content;
		this.author = author;
	}

	public static class EditableBookData {
		public String title = "New Book";
		public String content = "";

		public String toString() { return title + "\0" + content.replaceAll(",", "\u0000").replaceAll("\0", "\u0001"); }
		public EditableBookData fromData(String data) {
			String[] dt = data.split("\0", 2);
			EditableBookData book = new EditableBookData();
			book.title = dt[0];
			book.content = dt[1].replaceAll("\u0000", ",").replaceAll("\u0001", "\0");
			return book;
		}
	}

	private static BookData loadStaticBook(String bookName, String title) {
		JSONObject locs;
		try {
			locs = new JSONObject(String.join("\n", Load.loadFile("/resources/books/" + bookName + ".json")).replaceAll("\u0000", "\0"));
		} catch (IOException | JSONException ex) {
			ex.printStackTrace();
			locs = new JSONObject();
		}

		HashMap<Locale, String> localizations = new HashMap<>();
		for (String k : locs.keySet()) {
			localizations.put(Locale.forLanguageTag(k.substring(k.lastIndexOf("_")+1)), locs.getString(k));
		}
		if (!localizations.containsKey(Localization.DEFAULT_LOCALE)) localizations.put(Localization.DEFAULT_LOCALE, "");

		if (!localizations.containsKey(Localization.getSelectedLocale())) {
			return new BookData(title, localizations.get(Localization.DEFAULT_LOCALE), "Minicraft+");
		} else {
			return new BookData(title, localizations.get(Localization.getSelectedLocale()), "Minicraft+");
		}
	}

	public String toString() {
		return title + "\0" + author + "\0" + content.replaceAll(",", "\u0000").replaceAll("\0", "\u0001");
	}

	public static BookData fromData(String data) {
		String[] dt = data.split("\0", 3);
		return new BookData(dt[0], dt[2], dt[1].replaceAll("\u0000", ",").replaceAll("\u0001", "\0"));
	}
}

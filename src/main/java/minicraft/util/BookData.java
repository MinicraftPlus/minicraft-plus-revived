package minicraft.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.tinylog.Logger;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.WorldSelectDisplay;

public class BookData {
	private static Random random = new Random();

	public enum StaticBook {
		about ("about"),
		credits ("credits"),
		instructions("instructions"),
		antVenomBook ("antidous"),
		storylineGuide ("story_guide");

		private HashMap<Locale, String> localizations = new HashMap<>();

		StaticBook(String bookName) {
			JSONObject locs;
			try {
				locs = new JSONObject(String.join("\n", Load.loadFile("/resources/books/" + bookName + ".json")).replaceAll("\u0000", "\0"));
			} catch (IOException | JSONException ex) {
				ex.printStackTrace();
				locs = new JSONObject();
			}
			for (String k : locs.keySet()) {
				localizations.put(Locale.forLanguageTag(k.substring(k.lastIndexOf("_")+1)), locs.getString(k));
			}
			if (!localizations.containsKey(Localization.DEFAULT_LOCALE)) localizations.put(Localization.DEFAULT_LOCALE, "");
		}

		public String getLocalization(Locale lang) {
			if (!localizations.containsKey(lang)) return localizations.get(Localization.DEFAULT_LOCALE);
			else return localizations.get(lang);
		}
	}

	private static String saveDir;

	public String title;
	public String content;
	public String author;
	public final String id;

	public BookData(String id, String title, String content, String author) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.author = author;
	}
	public BookData(JSONObject data) {
		this(data.getString("id"), data.getString("title"), data.getString("content"), data.getString("author"));
	}

	public static class EditableBookData {
		public String title = "New Book";
		public String content = "";
	}

	public static BookData loadBook(String bookID) {
		try {
			updateSaveDir();
			return new BookData(new JSONObject(Load.loadFromFile(saveDir + bookID + ".book", true)));
		} catch (IOException e) {
			Logger.error("Cannot load book: "+bookID);
			if (!new File(saveDir + bookID + ".book").exists()) {
				Logger.warn("Book "+bookID+" does not exist, creating new empty book.");
				return new BookData(bookID, "", "", "");
			}
			e.printStackTrace();
			return new BookData(bookID, "", "", "");
		}
	}

	private static void updateSaveDir() {
		saveDir = Game.gameDir + "/saves/" + WorldSelectDisplay.getWorldName() + "/books/";
	}

	public static String genNewID() {
		updateSaveDir();
		File dir = new File(saveDir);
		dir.mkdirs();
		List<String> existIDs = Arrays.asList(dir.list((file, name) -> name.endsWith(".book"))).stream().map(b -> b.substring(0, 8)).collect(Collectors.toList());
		boolean valid = false;
		String id = "";
		while (!valid) {
			id = String.format("%08d", random.nextInt(99999999));
			if (!existIDs.contains(id)) valid = true;
		}
		return id;
	}

	public static String intIDToString(int intID) {
		return String.format("%08d", intID);
	}

	public static void saveBook(BookData book) {
		updateSaveDir();
		new File(saveDir).mkdirs();
		JSONObject json = new JSONObject();
		json.put("id", book.id);
		json.put("title", book.title);
		json.put("content", book.content);
		json.put("author", book.author);

		try {
			Save.writeJSONToFile(Game.gameDir + "/saves/" + WorldSelectDisplay.getWorldName() + "/books/" + book.id + ".book", json.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

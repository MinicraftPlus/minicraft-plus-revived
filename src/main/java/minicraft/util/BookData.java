package minicraft.util;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import minicraft.core.io.Localization;
import minicraft.saveload.Load;

public class BookData {

	public static final BookData about = new BookData("about");
	public static final BookData credits = new BookData("credits");
	public static final BookData instructions = new BookData("instructions");
	public static final BookData antVenomBook = new BookData("antidous");
	public static final BookData storylineGuide = new BookData("story_guide");

	private String id;
	private String data;

	private BookData(String id) {
		this.id = id;
		this.data = "";
	}

	public void load() {
		String book = "";
		JSONObject bookData;

		try {
			bookData = Load.loadJsonFile("/resources/books/" + this.id + ".json");

			if (bookData.has("lines")) {
				JSONArray lines = bookData.getJSONArray("lines");

				for (int i = 0; i < lines.length(); i++) {
					book += Localization.getLocalized(lines.getString(i));

					if (i + 1 < lines.length()) {
						book += "\n";
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			book = "";
		}

		this.data = book;
	}

	public String getData() {
		if (this.data.isEmpty()) {
			this.load();
		}
		return this.data;
	}

	public static void saveBook(String bookTitle) {

	}
}

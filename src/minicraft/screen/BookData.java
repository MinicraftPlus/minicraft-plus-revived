package minicraft.screen;

import java.io.IOException;

import minicraft.saveload.Load;

public class BookData {
	
	public static final String about = loadBook("about");
	public static final String instructions = loadBook("instructions");
	public static final String antVenomBook = loadBook("antidous");
	public static final String storylineGuide = loadBook("story_guide");
	
	private static final String loadBook(String bookTitle) {
		String book;
		try {
			book = String.join("\n", Load.loadFile("/resources/books/" + bookTitle + ".txt"));
			book = book.replaceAll("\\\\0", "\0");
		} catch (IOException ex) {
			ex.printStackTrace();
			book = "";
		}
		
		return book;
	}

	public static void saveBook(String bookTitle) {

	}
}

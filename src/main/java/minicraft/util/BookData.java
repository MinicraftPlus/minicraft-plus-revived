package minicraft.util;

public class BookData {

	public static String about;
	public static String credits;
	public static String instructions;
	public static String antVenomBook;
	public static String storylineGuide;

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

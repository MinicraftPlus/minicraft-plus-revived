package minicraft.screen;

import java.io.IOException;

import minicraft.saveload.Load;

public class Displays {
	
	public static final String about = "This is the about page.\0There is not much to see here.";
	public static final String instructions = "This is the instructions page.\0I am going to put more here at a later point in time.\n\nThis\nis to test\nl\ni\nn\ne\ns.";
	
	public static final String antVenomBook;
	static {
		String book;
		try {
			book = String.join("\n", Load.loadFile("/resources/antidous.txt"));
			book = book.replaceAll("\\\\0", "\0");
		} catch (IOException ex) {
			ex.printStackTrace();
			book = "";
		}
		
		antVenomBook = book;
	}
}

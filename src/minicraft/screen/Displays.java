package minicraft.screen;

import java.io.IOException;

import minicraft.saveload.Load;

public class Displays {
	
	public static final String about = "This is the about page.\0There is not much to see here.";
	public static final String instructions = "This is the instructions page.\0I hope to put more\nhere\nlater.";
	
	public static final String antVenomBook;
	static {
		String book;
		try {
			book = String.join("\n", Load.loadFile("/resources/antidous.txt"));
		} catch (IOException ex) {
			ex.printStackTrace();
			book = "";
		}
		
		antVenomBook = book;
	}
}

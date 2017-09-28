package minicraft.screen2;

import java.io.IOException;

import minicraft.saveload.Load;

public class Displays {
	
	public static final String about = "This is the about page.";
	public static final String instructions = "This is the instructions page.";
	
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

package minicraft.screen;

import java.io.IOException;

import minicraft.saveload.Load;

public class BookData {
	
	public static final String about = "Modded by David.b and +Dillyg10+ until 1.8, then taken over by Chris J. Our goal is to expand Minicraft to be more fun and continuous.\nMinicraft was originally made by Markus Perrson for ludum dare 22 competition.";
	
	public static final String instructions = "With the default controls...\n\nMove your character with arrow keys or WSAD. Press C to attack and X to open the inventory, and to use items. Pickup furniture and torches with V. Select an item in the inventory to equip it.\n\nThe Goal: Defeat the air wizard!";
	
	public static final String antVenomBook = loadBook("antidous");
	public static final String storylineGuide = loadBook("story_guide");

	public static final String theStoryOfPaul = "One day There was a man named Paul.\0He was dropped on an island with no hope of escape, and told to defeat the only other person there.\0Paul followed his orders, and fullfilled them.\0But he was only one...\0There were many others, sent to other islands, other tests.\0And the best of them?\0They would be used in the next wave of wars.\0They were the warriors who would go on to face dangers, challenges, fear.\0Many would lose their lives.\0But not Paul.\0You are PÃ&¡A%ÑU/Í?L.";
	
	private static final String loadBook(String bookTitle) {
		String book;
		try {
			book = String.join("\n", Load.loadFile("/resources/"+bookTitle+".txt"));
			book = book.replaceAll("\\\\0", "\0");
		} catch (IOException ex) {
			ex.printStackTrace();
			book = "";
		}
		
		return book;
	}
}

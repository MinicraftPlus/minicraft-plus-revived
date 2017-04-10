package com.mojang.ld22.gfx;

public class Font {
	
	// These are all the characters that will be translated to the screen. (The spaces are important)
	private static String chars = "" + //
                       "ABCDEFGHIJKLMNOPQRSTUVWXYZ      " + //
                       "0123456789.,!?'\"-+=/\\%()<>:;^@bcdefghijklmnopqrstuvwxyz";//
	
	/* The order of the letters in the chars string is represented in the order that they appear in the sprite-sheet. */
	
	/* Note: I am thinking of changing this system in the future so that it's much simpler -David */
	
	/** Draws the message to the x & y coordinates on the screen. */
	public static void draw(String msg, Screen screen, int x, int y, int col) {
		msg = msg.toUpperCase(); //makes all letters uppercase.
		for (int i = 0; i < msg.length(); i++) { // Loops through all the characters that you typed
			int ix = chars.indexOf(msg.charAt(i)); // the current letter in the message loop
			if (ix >= 0) {
				// if that character's position is larger than or equal to 0, then render the character on the screen.
				screen.render(x + i * 8, y, ix + 30 * 32, col, 0);
			}
		}
	}
	
	/** This renders the blue frame you see when you open up the crafting/inventory menus.
	 *  The width & height are based on 4 points (Staring x & y positions (0), and Ending x & y positions (1)). */
	
	/// This method... is TERRIBLY inefficient...I must fix it...
	public static void renderFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		for (int y = y0; y <= y1; y++) { // loop through the height of the frame
			for (int x = x0; x <= x1; x++) { // loop through the width of the frame
				if (x == x0 && y == y0) //if both x and y are at start pos...
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 5, 555), 0);
				else if (x == x1 && y == y0) //if x is at end, and y is at start...
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 1);
				else if (x == x0 && y == y1) //x at start, y at end..
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 2);
				else if (x == x1 && y == y1) //x and y at end...
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 3);
				///SET 2: x and/or y not at a corner
				else if (y == y0)//y at start, render top endpoint
					screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 5, 445), 0);
				else if (y == y1)//y at end, render bottom endpoint
					screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 5, 445), 2);
				else if (x == x0)//x at start, render left endpoint
					screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 5, 445), 0);
				else if (x == x1)// x at end, render right endpoint
					screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 5, 445), 1);
				else //if anything else, render a blue square.
					screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(5, 5, 5, 5), 1);
			}
		}

		draw(title, screen, x0 * 8 + 8, y0 * 8, Color.get(5, 5, 5, 550));
	}
	
	/// renders crafting menu frame.
	public static void rendercraftFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				if (x == x0 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 300, 400), 0);
				else if (x == x1 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 300, 400), 1);
				else if (x == x0 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 300, 400), 2);
				else if (x == x1 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 300, 400), 3);
				else if (y == y0) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 300, 400), 0);
				else if (y == y1) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 300, 400), 2);
				else if (x == x0) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 300, 400), 0);
				else if (x == x1) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 300, 400), 1);
				else screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(300, 300, 300, 300), 1);
			}
		}

		draw(title, screen, x0 * 8 + 8, y0 * 8, Color.get(300, 300, 300, 555));
	}
	
	/// self-explanitory; renders book frame.
	public static void renderFrameBook(Screen screen, String title, int x0, int y0, int x1, int y1) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				if (x == x0 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 554, 554), 0);
				else if (x == x1 && y == y0)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 554, 554), 1);
				else if (x == x0 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 554, 554), 2);
				else if (x == x1 && y == y1)
					screen.render(x * 8, y * 8, 0 + 13 * 32, Color.get(-1, 1, 554, 554), 3);
				else if (y == y0) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 554, 554), 0);
				else if (y == y1) screen.render(x * 8, y * 8, 1 + 13 * 32, Color.get(-1, 1, 554, 554), 2);
				else if (x == x0) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 554, 554), 0);
				else if (x == x1) screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(-1, 1, 554, 554), 1);
				else screen.render(x * 8, y * 8, 2 + 13 * 32, Color.get(554, 554, 554, 554), 1);
			}

			draw(title, screen, x0 * 8 + 8, y0 * 8, Color.get(-1, 222, 222, 222));
		}
	}
	
	public static int textWidth(String text) {
		return text.length() * 8;
	}
	
	public static void drawCentered(String msg, Screen screen, int y, int color) {
		draw(msg, screen, screen.centertext(msg), y, color);
	}
}

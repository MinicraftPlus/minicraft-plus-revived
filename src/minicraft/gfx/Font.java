package minicraft.gfx;

import java.text.StringCharacterIterator;

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
	} /// draws with a shadowed effect:
	public static void draw(String msg, Screen screen, int x, int y, int colMain, int colShadow) {
		draw(msg, screen, x+1, y+1, colShadow);
		draw(msg, screen, x, y, colMain);
	}
	
	/** This renders the blue frame you see when you open up the crafting/inventory menus.
	 *  The width & height are based on 4 points (Staring x & y positions (0), and Ending x & y positions (1)). */
	
	public static void renderFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		renderMenuFrame(screen, title, x0, y0, x1, y1, Color.get(-1, 1, 5, 445), Color.get(005, 005), Color.get(5, 5, 5, 550));
	}
	public static void renderMenuFrame(Screen screen, String title, int x0, int y0, int x1, int y1, int sideColor, int midColor, int titleColor) {
		for (int y = y0; y <= y1; y++) { // loop through the height of the frame
			for (int x = x0; x <= x1; x++) { // loop through the width of the frame
				
				boolean xend = x == x0 || x == x1;
				boolean yend = y == y0 || y == y1;
				int spriteoffset = (xend && yend ? 0 : (yend ? 1 : 2)); // determines which sprite to use
				int mirrors = ( x == x1 ? 1 : 0 ) + ( y == y1 ? 2 : 0 ); // gets mirroring
				//if(!xend && !yend) mirror = 1; // a pattern I noticed.
				
				int color = xend || yend ? sideColor : midColor;//sideColor; // gets the color; slightly different in upper right corner, and middle is all blue.
				//if(x == x0 && y == y0) color = Color.get(-1, 1, 5, 555);
				//if(!xend && !yend) color = midColor;
				
				screen.render(x * 8, y * 8, spriteoffset + 13 * 32, color, mirrors);
				/*
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
				*/
			}
		}

		draw(title, screen, x0 * 8 + 8, y0 * 8, titleColor);
	}
	
	/// renders crafting menu frame.
	public static void rendercraftFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		renderMenuFrame(screen, title, x0, y0, x1, y1, Color.get(-1, 1, 300, 400), Color.get(300, 300), Color.get(300, 300, 300, 555));
		/*
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
		*/
	}
	
	/// self-explanitory; renders book frame.
	public static void renderFrameBook(Screen screen, String title, int x0, int y0, int x1, int y1) {
		renderMenuFrame(screen, title, x0, y0, x1, y1, Color.get(-1, 1, 554, 554), Color.get(554, 554), Color.get(-1, 222));
		/*
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
		}*/
	}
	
	public static int textWidth(String text) {
		return text.length() * 8;
	}
	public static int textHeight() {return 8;}
	
	public static int centerX(String msg, int minX, int maxX) {
		return (maxX + minX) / 2 - textWidth(msg) / 2;
	}
	
	/// centered
	public static void drawCentered(String msg, Screen screen, int minX, int maxX, int y, int color) {
		int x = centerX(msg, minX, maxX);
		draw(msg, screen, x, y, color);
	}
	public static void drawCentered(String msg, Screen screen, int y, int color) {
		drawCentered(msg, screen, 0, screen.w, y, color);
	}
	/// centered, shadowed.
	public static void drawCentered(String msg, Screen screen, int minX, int maxX, int y, int colMain, int colShadow) {
		int x = centerX(msg, minX, maxX);
		draw(msg, screen, x, y, colMain, colShadow);
	}
	public static void drawCentered(String msg, Screen screen, int y, int colMain, int colShadow) {
		drawCentered(msg, screen, 0, screen.w, y, colMain, colShadow);
	}
	
	/// draw a paragraph within a given rectangle.
	/// this one assumes the screen width, minus a given padding.
	public static String drawParagraph(String para, Screen screen, int padding, int y, boolean centered, int lineSpacing, int color) {return drawParagraph(para, screen, padding, y, screen.w-padding*2, screen.h - y, centered, lineSpacing, color);}
	public static String drawParagraph(String para, Screen screen, int x, int y, int w, int h, boolean centered, int lineSpacing, int color) {
		int curPos = 0, curY = y;
		while(curPos < para.length() && curY < y + h) { // continue until we run out of characters, or lines.
			String line = "", nextWord = "";
			while(textWidth(line) + textWidth(nextWord) <= w) { // if the next word will fit...
				line += nextWord; // append it to the line
				curPos += nextWord.length(); // advance past the word (including space)
				
				if(curPos >= para.length()) break; // skip the rest, break from the loop, if we've run out of characters to process.
				if(para.charAt(curPos) == '\n') { // skip to next line on line break
					curPos++;
					break;
				}
				
				nextWord = line.equals("")?"":" "; // space this word from the previous word, if there is one.
				StringCharacterIterator text = new StringCharacterIterator(para, curPos);
				for(char c = text.current(); c != StringCharacterIterator.DONE && c != ' ' && c != '\n'; c = text.next()) {
					nextWord += String.valueOf(c); // iterate through text from curPos, until the end of the text, or a space.
				}
				if(text.current() == ' ' && line.equals("")) curPos++; // if we ended on a space, advance past the space.
			}
			
			if(centered) drawCentered(line, screen, x, x + w, curY, color); // draw centered in the rectangle
			else draw(line, screen, x, curY, color); // draw left-justified in the rectangle
			
			curY += textHeight() + lineSpacing; // move the y position down one line.
		}
		
		if(curPos < para.length())
			return para.substring(curPos); // return any text from the string that didn't fit in the given rectangle.
		else
			return ""; // all the text fit.
	}
}

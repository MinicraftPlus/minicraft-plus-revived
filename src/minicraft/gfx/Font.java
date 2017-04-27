package minicraft.gfx;

import java.text.StringCharacterIterator;
import java.util.ArrayList;

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
	
	public static void renderMenuFrame(Screen screen, String title, int x0, int y0, int x1, int y1, int sideColor, int midColor, int titleColor) {
		for (int y = y0; y <= y1; y++) { // loop through the height of the frame
			for (int x = x0; x <= x1; x++) { // loop through the width of the frame
				
				boolean xend = x == x0 || x == x1;
				boolean yend = y == y0 || y == y1;
				int spriteoffset = (xend && yend ? 0 : (yend ? 1 : 2)); // determines which sprite to use
				int mirrors = ( x == x1 ? 1 : 0 ) + ( y == y1 ? 2 : 0 ); // gets mirroring
				
				int color = xend || yend ? sideColor : midColor;//sideColor; // gets the color; slightly different in upper right corner, and middle is all blue.
				
				screen.render(x * 8, y * 8, spriteoffset + 13 * 32, color, mirrors);
			}
		}

		draw(title, screen, x0 * 8 + 8, y0 * 8, titleColor);
	}
	
	public static void renderFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		renderMenuFrame(screen, title, x0, y0, x1, y1, Color.get(-1, 1, 5, 445), Color.get(005, 005), Color.get(5, 5, 5, 550));
	}
	
	/// renders crafting menu frame.
	public static void rendercraftFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		renderMenuFrame(screen, title, x0, y0, x1, y1, Color.get(-1, 1, 300, 400), Color.get(300, 300), Color.get(300, 300, 300, 555));
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
	
	/// this draws a paragraph from an array of lines (or a string, at which point it calls getLines()), with the specified properties.
	public static String drawParagraph(String[] lines, Screen screen, int x, int y, int w, int h, boolean centered, int lineSpacing, int color) {
		for(int i = 0; i < lines.length-1; i++) {
			int curY = y + i*textHeight() + i*lineSpacing;
			if(centered) drawCentered(lines[i], screen, x, x + w, curY, color); // draw centered in the rectangle
			else draw(lines[i], screen, x, curY, color); // draw left-justified in the rectangle
		}
		return lines[lines.length-1]; // this is where the rest of the string that there wasn't space for is stored.
	}
	public static String drawParagraph(String para, Screen screen, int x, int y, int w, int h, boolean centered, int lineSpacing, int color) {
		String[] lines = getLines(para, w, h, lineSpacing);
		return drawParagraph(lines, screen, x, y, w, h, centered, lineSpacing, color);
	}
	
	public static String[] getLines(String para, int w, int h, int lineSpacing) {
		ArrayList<String> lines = new ArrayList<String>();
		int curPos = 0, curY = 0;
		//System.out.println("got new string: " + para + "\nseperating the " + para.length() + "-char string to lines of width " + w + "... max y: " + h);
		while(curPos < para.length() && curY < h) { // continue until we run out of characters, or lines.
			String line = "", nextWord = "";
			//System.out.println("starting new line...");
			while(textWidth(line) + textWidth(nextWord) <= w) { // if the next word will fit...
				line += nextWord; // append it to the line
				curPos += nextWord.length(); // advance past the word (including space)
				//System.out.println("added word \"" + nextWord + "\" and upped curPos to " + curPos);
				if(curPos >= para.length()) break; // skip the rest, break from the loop, if we've run out of characters to process.
				if(para.charAt(curPos) == '\n') { // skip to next line on line break
					curPos++;
					//System.out.println("encountered newline.");
					break;
				}
				
				nextWord = line.equals("")?"":" "; // space this word from the previous word, if there is one.
				StringCharacterIterator text = new StringCharacterIterator(para, curPos);
				//System.out.println("building word...");
				for(char c = text.current(); c != StringCharacterIterator.DONE && c != ' ' && c != '\n'; c = text.next()) {
					//System.out.println("adding char '" + c + "'");
					nextWord += String.valueOf(c); // iterate through text from curPos, until the end of the text, or a space, or special char.
				}
				//System.out.println("terminated word \"" + nextWord + "\" on '" + text.current() + "'");
				if(text.current() == ' ' && line.equals("")) curPos++; // if we ended on a space, advance past the space.
				int tw = textWidth(line) + textWidth(nextWord);
				//System.out.println("next text width: " + tw + "; max width: " + w);
			}
			lines.add(line); // add the finished line to the list
			curY += textHeight() + lineSpacing; // move the y position down one line.
			//System.out.println("finished line. new y pos: " + curY);
		}
		
		//System.out.println("finished paragraph at text pos " + curPos + ", and y pos " + curY);
		
		String leftover = "";
		if(curPos < para.length())
			leftover = para.substring(curPos); // get any text from the string that didn't fit in the given rectangle.
		
		//System.out.println("adding leftover lines: " + leftover);
		lines.add(leftover);
		
		return lines.toArray(new String[0]);
	}
}

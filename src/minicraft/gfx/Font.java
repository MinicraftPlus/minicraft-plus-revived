package minicraft.gfx;

import java.text.StringCharacterIterator;
import java.util.ArrayList;

public class Font {
	/// this is, for now, a sort of "static" class; everything is static, becuase there is only one font. However, if more fonts are ever added, this may change.
	
	// These are all the characters that will be translated to the screen. (The spaces are important)
	private static String chars = "" + //
                       "ABCDEFGHIJKLMNOPQRSTUVWXYZ      " + //
                       "0123456789.,!?'\"-+=/\\%()<>:;^@bcdefghijklmnopqrstuvwxyz";//
	
	/* The order of the letters in the chars string is represented in the order that they appear in the sprite-sheet. */
	
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
		while(curPos < para.length() && curY < h) { // continue until we run out of characters, or lines.
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
					nextWord += String.valueOf(c); // iterate through text from curPos, until the end of the text, or a space, or special char.
				}
				if(text.current() == ' ' && line.equals("")) curPos++; // if we ended on a space, advance past the space.
				int tw = textWidth(line) + textWidth(nextWord);
			}
			lines.add(line); // add the finished line to the list
			curY += textHeight() + lineSpacing; // move the y position down one line.
		}
		
		String leftover = "";
		if(curPos < para.length())
			leftover = para.substring(curPos); // get any text from the string that didn't fit in the given rectangle.
		
		lines.add(leftover);
		
		return lines.toArray(new String[0]);
	}
}

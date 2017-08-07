package minicraft.gfx;

import java.text.StringCharacterIterator;
import java.util.ArrayList;

public class Font {
	// These are all the characters that will be translated to the screen. (The spaces are important)
	private static String chars = "" + //
                       "ABCDEFGHIJKLMNOPQRSTUVWXYZ      " + //
                       "0123456789.,!?'\"-+=/\\%()<>:;^@bcdefghijklmnopqrstuvwxyz";//
	
	/* The order of the letters in the chars string is represented in the order that they appear in the sprite-sheet. */
	
	/** Draws the message to the x & y coordinates on the screen. */
	public static void draw(String msg, Screen screen, int x, int y, int col) {
		msg = msg.toUpperCase(java.util.Locale.ENGLISH); //makes all letters uppercase.
		for (int i = 0; i < msg.length(); i++) { // Loops through all the characters that you typed
			int ix = chars.indexOf(msg.charAt(i)); // the current letter in the message loop
			if (ix >= 0) {
				// if that character's position is larger than or equal to 0, then render the character on the screen.
				screen.render(x + i * textWidth(msg.substring(i, i+1)), y, ix + 30 * 32, col, 0);
			}
		}
	}
	
	public static int textWidth(String text) {
		return text.length() * 8;
	}
	public static int textHeight() {return 8;}
	
	public static int centerX(String msg, int minX, int maxX) {
		return (maxX + minX) / 2 - textWidth(msg) / 2;
	}
	
	public static int centerY(String msg, int minY, int maxY) {
		return (maxY + minY) / 2 - textHeight() / 2;
	}
	
	public static void drawCentered(String msg, Screen screen, int y, int color) {
		new FontStyle(color).setYPos(y).draw(msg, screen);
	}
	
	public static void drawCentered(String msg, Screen screen, int x, int y, int color) {
		new FontStyle(color).xCenterBounds(x-(Screen.w-x), Screen.w).setYPos(y).draw(msg, screen);
	}
	
	/// these draws a paragraph from an array of lines (or a string, at which point it calls getLines()), with the specified properties.
	
	/// this one assumes the screen width, minus a given padding.
	public static String drawParagraph(String para, Screen screen, int paddingX, boolean centerPaddingX, int paddingY, boolean centerPaddingY, FontStyle style, int lineSpacing) {
		
		style.xCenterBounds(paddingX, Screen.w - (centerPaddingX?paddingX:0));
		style.yCenterBounds(paddingY, Screen.h - (centerPaddingY?paddingY:0));
		
		return drawParagraph(para, screen, style.centerMaxX - style.centerMinX, style.centerMaxY - style.centerMinY, style, centerPaddingX, lineSpacing);
	}
	
	/// note: the y centering values in the FontStyle object will be used as a paragraph y centering value instead.
	public static String drawParagraph(String para, Screen screen, int w, int h, FontStyle style, boolean centered, int lineSpacing) {
		//int w = style.centerMaxX - style.centerMinX;
		//int h = style.centerMaxY - style.centerMinY;
		
		String[] lines = getLines(para, w, h, lineSpacing);
		//System.out.println("lines: " + java.util.Arrays.toString(lines));
		
		if (centered) style.xPosition = -1;
		//else style.xPosition = (Screen.w - w) / 2;
		
		return drawParagraph(lines, screen, style, lineSpacing);
	}
	
	/// all the other drawParagraph() methods end up calling this one.
	public static String drawParagraph(String[] lines, Screen screen, FontStyle style, int lineSpacing) {
		for(int i = 0; i < lines.length-1; i++)
			style.drawParagraphLine(lines, i, lineSpacing, screen);
		
		return lines[lines.length-1]; // this is where the rest of the string that there wasn't space for is stored.
	}
	
	
	public static String[] getLines(String para, int w, int lineSpacing) { return getLines(para, w, 0, lineSpacing); }
	public static String[] getLines(String para, int w, int h, int lineSpacing) {
		ArrayList<String> lines = new ArrayList<String>();
		int curPos = 0, curY = 0;
		while(curPos < para.length() && (h <= 0 || curY < h)) { // continue until we run out of characters, or lines.
			String line = "", nextWord = "";
			while(textWidth(line) + textWidth(nextWord) <= w) { // if the next word will fit...
				line += nextWord; // append it to the line
				curPos += nextWord.length(); // advance past the word (including space)
				if(curPos >= para.length()) {
					//System.out.println("no more chars");
					break; // skip the rest, break from the loop, if we've run out of characters to process.
				}
				if(para.charAt(curPos) == '\n') { // skip to next line on line break
					curPos++;
					//System.out.println("newline");
					break;
				}
				
				nextWord = line.equals("")?"":" "; // space this word from the previous word, if there is one.
				StringCharacterIterator text = new StringCharacterIterator(para, curPos);
				for(char c = text.current(); c != StringCharacterIterator.DONE && c != ' ' && c != '\n'; c = text.next()) {
					nextWord += String.valueOf(c); // iterate through text from curPos, until the end of the text, or a space, or special char.
				}
				if(text.current() != ' ' && curPos+nextWord.length() < para.length())
					curPos--; // if we didn't end on a space, and this is going to loop again, then take away one from curPos (b/c we are adding a space that wasn't there before, and so without this we would skip a character).
				if(text.current() == ' ' && line.equals(""))
					curPos++; // if we ended on a space, advance past the space.
			}
			//System.out.println("adding line " + line);
			lines.add(line); // add the finished line to the list
			curY += textHeight() + lineSpacing; // move the y position down one line.
		}
		
		String leftover = "";
		if(curPos < para.length())
			leftover = para.substring(curPos); // get any text from the string that didn't fit in the given rectangle.
		
		if(h > 0 || leftover.length() > 0)
			lines.add(leftover);
		
		return lines.toArray(new String[0]);
	}
}

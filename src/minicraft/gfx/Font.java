package minicraft.gfx;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.screen.RelPos;

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
	
	public static int textWidth(String text) { return text.length() * 8; }
	public static int textWidth(String[] para) {
		// this returns the maximum length of all the lines.
		if(para == null || para.length == 0) return 0;
		
		int max = textWidth(para[0]);
		
		for(int i = 1; i < para.length; i++)
			max = Math.max(max, textWidth(para[i]));
		
		return max;
	}
	
	public static int textHeight() {//noinspection SuspiciousNameCombination
		return SpriteSheet.boxWidth;
	}
	
	/*public static int centerX(String msg, int minX, int maxX) {
		return (maxX + minX) / 2 - textWidth(msg) / 2;
	}*/
	
	/*public static int centerY(String msg, int minY, int maxY) {
		return (maxY + minY) / 2 - textHeight() / 2;
	}*/
	
	public static void drawCentered(String msg, Screen screen, int y, int color) {
		new FontStyle(color).setYPos(y).draw(msg, screen);
	}
	
	/*public static void drawCentered(String msg, Screen screen, int x, int y, int color) {
		new FontStyle(color).xCenterBounds(x-(Screen.w-x), Screen.w).setYPos(y).draw(msg, screen);
	}*/
	
	/// these draws a paragraph from an array of lines (or a string, at which point it calls getLines()), with the specified properties.
	
	/// this one assumes the screen width, minus a given padding.
	/*public static void drawParagraph(String para, Screen screen, int paddingX, boolean mirrorPaddingX, int paddingY, boolean mirrorPaddingY, FontStyle style, int lineSpacing) {
		
		//style.xCenterBounds(paddingX, Screen.w - (mirrorPaddingX?paddingX:0));
		//style.yCenterBounds(paddingY, Screen.h - (mirrorPaddingY?paddingY:0));
		if(mirrorPaddingX) {
			style.setXPos(Screen.w / 2);
			style.setParaJustify(RelPos.TOP);
		}
		if(mirrorPaddingY)
			style.setYPos(Screen.h/2);
		
		drawParagraph(para, screen, Screen.w-paddingX*(mirrorPaddingX?2:1), Screen.h-paddingY*(mirrorPaddingY?2:1), style, lineSpacing);
	}*/
	
	/// note: the y centering values in the FontStyle object will be used as a paragraph y centering value instead.
	public static void drawParagraph(String para, Screen screen, FontStyle style, int lineSpacing) { drawParagraph(para, screen, Screen.w, Screen.h, style, lineSpacing); }
	public static void drawParagraph(String para, Screen screen, int w, int h, FontStyle style, int lineSpacing) {
		String[] lines = getLines(para, w, h, lineSpacing);
		drawParagraph(lines, screen, style, lineSpacing);
	}
	
	/// all the other drawParagraph() methods end up calling this one.
	public static void drawParagraph(String[] lines, Screen screen, FontStyle style, int lineSpacing) {
		for(int i = 0; i < lines.length; i++)
			style.drawParagraphLine(lines, i, lineSpacing, screen);
		
		//return lines[lines.length-1]; // this is where the rest of the string that there wasn't space for is stored.
	}
	
	//public static String[] getLines(String para, int w, int lineSpacing) { return getLines(para, w, 0, lineSpacing); }
	/*public static String[] getLines(String para, int w, int h, int lineSpacing) {
		ArrayList<String> lines = new ArrayList<>();
		int curPos = 0, curY = 0;
		while(curPos < para.length() && curY < h) { // continue until we run out of characters, or lines.
			StringBuilder line = new StringBuilder();
			StringBuilder nextWord = new StringBuilder();
			while(textWidth(line.toString()) + textWidth(nextWord.toString()) <= w) { // if the next word will fit...
				line.append(nextWord); // append it to the line
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
				
				nextWord = new StringBuilder(line.length() == 0 ? "" : " "); // space this word from the previous word, if there is one.
				StringCharacterIterator text = new StringCharacterIterator(para, curPos);
				for(char c = text.current(); c != StringCharacterIterator.DONE && c != ' ' && c != '\n'; c = text.next()) {
					nextWord.append(String.valueOf(c)); // iterate through text from curPos, until the end of the text, or a space, or special char.
				}
				if(text.current() != ' ' && curPos+nextWord.length() < para.length())
					curPos--; // if we didn't end on a space, and this is going to loop again, then take away one from curPos (b/c we are adding a space that wasn't there before, and so without this we would skip a character).
				if(text.current() == ' ' && line.length() == 0)
					curPos++; // if we ended on a space, advance past the space.
			}
			//System.out.println("adding line " + line);
			lines.add(line.toString()); // add the finished line to the list
			curY += textHeight() + lineSpacing; // move the y position down one line.
		}
		
		String leftover = "";
		if(curPos < para.length())
			leftover = para.substring(curPos); // get any text from the string that didn't fit in the given rectangle.
		
		if(h > 0 || leftover.length() > 0)
			lines.add(leftover);
		
		return lines.toArray(new String[lines.size()]);
	}*/
	
	public static String[] getLines(String para, int w, int h, int lineSpacing) {
		//para = para.replaceAll("-", "- "); // I'll try this later when the current system works.
		ArrayList<String> lines = new ArrayList<>();
		
		// So, I have a paragraph. I give it to getLine, and it returns an index. Cut the string at that index, and add it to the lines list.
		// check if the index returned by getLine is less than para.length(), and is a space, and if so skip the space character.
		// then I reset the para String at the index, and do it again until para is an empty string.
		
		int height = textHeight();
		while(para.length() > 0) {
			int splitIndex = getLine(para, w);
			lines.add(para.substring(0, splitIndex));
			
			if(splitIndex < para.length() && para.substring(splitIndex, splitIndex+1).matches(" |\n"))
				splitIndex++;
			para = para.substring(splitIndex);
			
			height += lineSpacing + textHeight();
			if(height > h)
				break;
		}
		
		lines.add(para); // add remainder
		
		return lines.toArray(new String[lines.size()]);
	}
	
	// this returns the position index at which the given string should be split so that the first part is the longest line possible.
	private static int getLine(String text, int maxWidth) {
		text = text.replaceAll(" ?\n ?", " \n ");
		
		String[] words = text.split(" ", -1);
		int curWidth = textWidth(words[0]);
		
		int i;
		for(i = 1; i < words.length; i++) {
			if(words[i].equals("\n")) break;
			
			curWidth += textWidth(" "+words[i]);
			if(curWidth > maxWidth)
				break;
		}
		// i now contains the number of words that fit on the line.
		
		String line = String.join(" ", Arrays.copyOfRange(words, 0, i));
		return line.length();
	}
}

package minicraft.gfx;

import minicraft.core.Renderer;
import minicraft.gfx.SpriteLinker.SpriteType;

import java.util.ArrayList;
import java.util.List;

public class Font {
	// These are all the characters that will be translated to the screen. (The spaces are important)
	private static final String chars =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ012345" +
			"6789.,!?'\"-+=/\\%()<>:;^@ÁÉÍÓÚÑ¿¡" +
			"ÃÊÇÔÕĞÇÜİÖŞÆØÅŰŐ[]#|{}_АБВГДЕЁЖЗ" +
			"ИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯÀÂÄÈÎÌÏÒ" +
			"ÙÛÝ*«»£$&€§ªºabcdefghijklmnopqrs" +
			"tuvwxyzáàãâäéèêëíìîïóòõôöúùûüçñý" +
			"ÿабвгдеёжзийклмнопрстуфхцчшщъыьэ" +
			"юяışő✓";

	/* The order of the letters in the chars string is represented in the order that they appear in the sprite-sheet. */

	public static void draw(String msg, Screen screen, int x, int y) {
		draw(msg, screen, x, y, -1);
	}

	/**
	 * Draws the message to the x & y coordinates on the screen.
	 */
	public static void
	draw(String msg, Screen screen, int x, int y, int whiteTint) {
		int cursor = x;
		for (int i = 0; i < msg.length();) {
			int codePoint = msg.codePointAt(i);
			int ix = codePoint <= Character.MAX_VALUE ? chars.indexOf((char) codePoint) : -1;
			if (ix >= 0) {
				screen.render(cursor, y, ix % 32, ix / 32, 0, Renderer.spriteLinker.getSheet(SpriteType.Gui, "font"), whiteTint);
			} else if (!Character.isWhitespace(codePoint)) {
				screen.render(cursor, y, 0, 0, 0, UnicodeFont.glyph(codePoint), whiteTint);
			}
			cursor += 8;
			i += Character.charCount(codePoint);
		}
	}

	public static void drawColor(String message, Screen screen, int x, int y) {
		if (message.isEmpty()) return;
		// Set default color message if it doesn't have initially
		if (message.charAt(0) != Color.COLOR_CHAR) {
			message = Color.WHITE_CODE + message;
		}

		int leading = 0;
		for (String data : message.split(String.valueOf(Color.COLOR_CHAR))) {
			if (data.isEmpty()) {
				continue;
			}

			String text;
			String color;

			try {
				text = data.substring(4);
				color = data.substring(0, 4); // ARGB value
			} catch (IndexOutOfBoundsException ignored) {
				// Bad formatted colored string
				text = data;
				color = Color.WHITE_CODE;
			}

			Font.draw(text, screen, x + leading, y, Color.get(color));
			leading += Font.textWidth(text);
		}
	}

	public static void drawBackground(String msg, Screen screen, int x, int y) {
		drawBackground(msg, screen, x, y, -1);
	}

	public static void drawBackground(String msg, Screen screen, int x, int y, int whiteTint) {
		for (int offset = 0; offset < textWidth(msg); offset += 8) {
			screen.render(x + offset, y, 5, 2, 0, Renderer.spriteLinker.getSheet(SpriteType.Gui, "hud"));
		}
		draw(msg, screen, x, y, whiteTint);
	}

	public static int textWidth(String text) {
		int width = 0;
		for (int i = 0; i < text.length();) {
			if (text.charAt(i) == Color.COLOR_CHAR && i + 4 < text.length()) {
				i += 5;
			} else {
				i += Character.charCount(text.codePointAt(i));
				width += 8;
			}
		}
		return width;
	}

	public static int textWidth(String[] para) {
		// This returns the maximum length of all the lines.
		if (para == null || para.length == 0) return 0;

		int max = textWidth(para[0]);

		for (int i = 1; i < para.length; i++)
			max = Math.max(max, textWidth(para[i]));

		return max;
	}

	public static int textHeight() {//noinspection SuspiciousNameCombination
		return MinicraftImage.boxWidth;
	}

	public static void drawCentered(String msg, Screen screen, int y, int color) {
		new FontStyle(color).setYPos(y).draw(msg, screen);
	}


	/// note: the y centering values in the FontStyle object will be used as a paragraph y centering value instead.
	public static void drawParagraph(String para, Screen screen, FontStyle style, int lineSpacing) {
		drawParagraph(para, screen, Screen.w, Screen.h, style, lineSpacing);
	}

	public static void drawParagraph(String para, Screen screen, int w, int h, FontStyle style, int lineSpacing) {
		drawParagraph(screen, style, lineSpacing, getLines(para, w, h, lineSpacing));
	}

	/// all the other drawParagraph() methods end up calling this one.
	public static void drawParagraph(List<String> lines, Screen screen, FontStyle style, int lineSpacing) {
		drawParagraph(screen, style, lineSpacing, lines.toArray(new String[0]));
	}

	public static void drawParagraph(Screen screen, FontStyle style, int lineSpacing, String... lines) {
		for (int i = 0; i < lines.length; i++)
			style.drawParagraphLine(lines, i, lineSpacing, screen);
	}

	public static String[] getLines(String para, int w, int h, int lineSpacing) {
		return getLines(para, w, h, lineSpacing, false);
	}

	public static String[] getLines(String para, int w, int h, int lineSpacing, boolean keepEmptyRemainder) {
		ArrayList<String> lines = new ArrayList<>();

		// So, I have a paragraph. I give it to getLine, and it returns an index. Cut the string at that index, and add it to the lines list.
		// check if the index returned by getLine is less than para.length(), and is a space, and if so skip the space character.
		// then I reset the para String at the index, and do it again until para is an empty string.

		int height = textHeight();
		while (para.length() > 0) { // continues to loop as long as there are more characters to parse.

			int splitIndex = getLine(para, w); // determine how many letters can be fit on to this line.
			lines.add(para.substring(0, splitIndex)); // add the specified number of characters.

			if (splitIndex < para.length() && para.substring(splitIndex, splitIndex + 1).matches("[ \n]"))
				splitIndex++; // if there are more characters to do, and the next character is a space or newline, skip it (because the getLine() method will always break before newlines, and will usually otherwise break before spaces.
			para = para.substring(splitIndex); // remove the characters that have now been added on to the line

			height += lineSpacing + textHeight(); // move y pos down a line
			if (height > h)
				break; // If we've run out of space to draw lines, then there's no point in parsing more characters, so we should break out of the loop.
		}

		if (para.length() > 0 || keepEmptyRemainder)
			lines.add(para); // add remainder, but don't add empty lines unintentionally.

		return lines.toArray(new String[0]);
	}

	// this returns the position index at which the given string should be split so that the first part is the longest line possible.
	// note, the index returned is exclusive; it should not be included in the line.
	private static int getLine(String text, int maxWidth) {
		if (maxWidth <= 0) throw new IllegalArgumentException("Text width must be positive");
		int width = 0;
		int lastSpace = -1;
		for (int i = 0; i < text.length();) {
			int codePoint = text.codePointAt(i);
			if (codePoint == '\n') return i;
			if (codePoint == Color.COLOR_CHAR && i + 4 < text.length()) {
				i += 5;
				continue;
			}
			if (codePoint == ' ') lastSpace = i;
			if (width + 8 > maxWidth) {
				if (lastSpace > 0) return lastSpace;
				// A long word or Chinese sentence can wrap between code points.
				return width == 0 ? i + Character.charCount(codePoint) : i;
			}
			width += 8;
			i += Character.charCount(codePoint);
		}
		return text.length();
	}
}

package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.ClipboardHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Dimension;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignDisplay extends Display {
	public static final int MAX_TEXT_LENGTH = 20;
	public static final int MAX_ROW_COUNT = 4;

	// TODO make this into an attached attribute of a sign tile.
	private static final HashMap<Map.Entry<Integer, Point>, List<String>> signTexts = new HashMap<>(); // The lines of signs should be immutable when stored.

	public static void resetSignTexts() {
		signTexts.clear();
	}

	public static void loadSignTexts(Map<Map.Entry<Integer, Point>, List<String>> signTexts) {
		SignDisplay.signTexts.clear();
		signTexts.forEach((pt, texts) -> SignDisplay.signTexts.put(pt, Collections.unmodifiableList(new ArrayList<>(texts))));
	}

	public static Map<Map.Entry<Integer, Point>, List<String>> getSignTexts() {
		return new HashMap<>(signTexts);
	}

	public static void updateSign(int levelDepth, int x, int y, List<String> lines) {
		signTexts.put(new AbstractMap.SimpleImmutableEntry<>(levelDepth, new Point(x, y)), Collections.unmodifiableList(new ArrayList<>(lines)));
	}

	public static void removeSign(int levelDepth, int x, int y) {
		if (signTexts.remove(new AbstractMap.SimpleImmutableEntry<>(levelDepth, new Point(x, y))) == null)
			Logging.WORLDNAMED.warn("Sign at ({}, {}) does not exist to be removed.", x, y);
	}

	public static @Nullable List<String> getSign(int levelDepth, int x, int y) {
		return signTexts.get(new AbstractMap.SimpleImmutableEntry<>(levelDepth, new Point(x, y)));
	}

	private final int levelDepth, x, y;

	private final SignEditor editor;

	public SignDisplay(Level level, int x, int y) {
		super(false, new Menu.Builder(true, 3, RelPos.CENTER)
			.setPositioning(new Point(Screen.w / 2, 6), RelPos.BOTTOM)
			.setMenuSize(new Dimension(MinicraftImage.boxWidth * (MAX_TEXT_LENGTH + 2), MinicraftImage.boxWidth * (MAX_ROW_COUNT + 2)))
			.setSelectable(false)
			.createMenu());
		this.levelDepth = level.depth;
		this.x = x;
		this.y = y;
		editor = new SignEditor(getSign(levelDepth, x, y));
	}

	private class SignEditor {
		private final ClipboardHandler clipboard = new ClipboardHandler();
		private final ArrayList<StringBuilder> rows = new ArrayList<>();
		private int cursorX = 0, cursorY = 0;
		private int caretFrameCountDown = 60;
		private boolean caretShown = true;

		public SignEditor(@Nullable List<String> lines) {
			if (lines != null) lines.forEach(l -> rows.add(new StringBuilder(l)));
			if (rows.isEmpty()) rows.add(new StringBuilder());
		}

		public List<String> getLines() {
			ArrayList<String> lines = new ArrayList<>();
			rows.forEach(r -> {
				// Reference: https://www.baeldung.com/java-string-split-every-n-characters#using-the-stringsubstring-method
				int length = r.length();
				for (int i = 0; i < length; i += MAX_TEXT_LENGTH) {
					lines.add(r.substring(i, Math.min(length, i + MAX_TEXT_LENGTH)));
				}
			});
			return lines;
		}

		public void tick(InputHandler input) {
			if (caretFrameCountDown-- == 0) { // Caret flashing animation
				caretFrameCountDown = 30;
				caretShown = !caretShown;
			}

			insertChars(input.getKeysTyped(null));
			if (input.getKey("PAGE-UP").clicked) {
				cursorX = 0;
				cursorY = 0;
				updateCaretAnimation();
			} else if (input.getKey("PAGE-DOWN").clicked) {
				cursorY = rows.size() - 1;
				cursorX = rows.get(cursorY).length();
				updateCaretAnimation();

			} else if (input.getKey("HOME").clicked) {
				cursorX = (cursorX - 1) / MAX_TEXT_LENGTH * MAX_TEXT_LENGTH; // Rounding down
				updateCaretAnimation();
			} else if (input.getKey("END").clicked) {
				cursorX = Math.min((cursorX + MAX_TEXT_LENGTH - 1) / MAX_TEXT_LENGTH * MAX_TEXT_LENGTH, rows.get(cursorY).length()); // Rounding up
				updateCaretAnimation();

			// Cursor navigating
			// As lines are centered, the character above in rendering would not always be the one in indices.
			// The position is set to the beginning of line when the cursor moved upward or downward.
			} else if (input.inputPressed("CURSOR-UP")) {
				cursorX = 0;
				if (cursorY > 0) {
					cursorY--;
				}

				updateCaretAnimation();
			} else if (input.inputPressed("CURSOR-DOWN")) {
				cursorX = rows.get(cursorY < rows.size() - 1 ? ++cursorY : cursorY).length();
				updateCaretAnimation();
			} else if (input.inputPressed("CURSOR-LEFT")) {
				if (cursorX > 0) cursorX--;
				else if (cursorY > 0) {
					cursorX = rows.get(--cursorY).length();
				}

				updateCaretAnimation();
			} else if (input.inputPressed("CURSOR-RIGHT")) {
				if (cursorX == rows.get(cursorY).length()) { // The end of row
					if (cursorY < rows.size() - 1) { // If it is not in the last row
						cursorX = 0;
						cursorY++;
					}
				} else {
					cursorX++;
				}

				updateCaretAnimation();

			// Clipboard operations
			} else if (input.getKey("CTRL-X").clicked) {
				cursorX = 0;
				cursorY = 0;
				clipboard.setClipboardContents(String.join("\n", rows));
				rows.clear();
				rows.add(new StringBuilder());
				updateCaretAnimation();
			} else if (input.getKey("CTRL-C").clicked) {
				clipboard.setClipboardContents(String.join("\n", rows));
				updateCaretAnimation();
			} else if (input.getKey("CTRL-V").clicked) {
				insertChars(clipboard.getClipboardContents());
			}
		}

		public void render(Screen screen) {
			Rectangle bounds = menus[0].getBounds();
			int yPos = bounds.getTop() + MinicraftImage.boxWidth; // Upper border
			int centeredX = bounds.getLeft() + bounds.getWidth() / 2;
			for (StringBuilder row : rows) {
				// See #getLines
				String r = row.toString();
				int length = r.length();
				for (int j = 0; j < length; j += MAX_TEXT_LENGTH) { // For each line
					Font.drawCentered(r.substring(j, Math.min(length, j + MAX_TEXT_LENGTH)), screen, yPos, Color.WHITE);
					//noinspection SuspiciousNameCombination
					yPos += MinicraftImage.boxWidth;
				}
			}

			// Cursor rendering
			if (caretShown) {
				int lineWidth = getLineWidthOfRow(cursorX, cursorY) * MinicraftImage.boxWidth;
				int displayX = calculateDisplayX(cursorX) * MinicraftImage.boxWidth;
				int displayY = calculateDisplayY(cursorX, cursorY) * MinicraftImage.boxWidth;
				int lineBeginning = centeredX - lineWidth / 2;
				int cursorX = lineBeginning + displayX;
				int cursorY = bounds.getTop() + MinicraftImage.boxWidth + displayY;
				int cursorRenderY = cursorY + MinicraftImage.boxWidth - 2;
				for (int i = 0; i < MinicraftImage.boxWidth; i++) { // 1 pixel high and 8 pixel wide
					int idx = cursorX + i + cursorRenderY * Screen.w;
					screen.pixels[idx] = Color.getLightnessFromRGB(screen.pixels[idx]) >= .5 ? Color.BLACK : Color.WHITE;
				}
			}
		}

		private void updateCaretAnimation() {
			caretShown = true;
			caretFrameCountDown = 120;
		}

		private int calculateDisplayX(int x) {
			return x > MAX_TEXT_LENGTH ? x - (x - 1) / MAX_TEXT_LENGTH * MAX_TEXT_LENGTH : x;
		}

		private int getLineWidthOfRow(int x, int y) {
			int length = rows.get(y).length();
			return (x == 0 ? MAX_TEXT_LENGTH : (x + MAX_TEXT_LENGTH - 1) / MAX_TEXT_LENGTH * MAX_TEXT_LENGTH) > length
				? length - (x - 1) / MAX_TEXT_LENGTH * MAX_TEXT_LENGTH : MAX_TEXT_LENGTH;
		}

		private int calculateDisplayY(int x, int y) {
			int count = 0;
			for (int i = 0; i <= y; i++) {
				if (i != y) count += calculateNumberOfOccupiedLinesOfRow(i);
				else count += (x - 1) / MAX_TEXT_LENGTH; // A new line is regarded when the current line exceeds MAX_TEST_LENGTH.
			}

			return count;
		}

		private boolean checkNewLineAvailable() {
			int maxY = rows.size() - 1;
			return calculateDisplayY(rows.get(maxY).length(), maxY) < MAX_ROW_COUNT - 1;
		}

		private boolean checkRowLineCapacity(int y) {
			int len = rows.get(y).length();
			// If the current row is not enough to fill in a line or the existing new line
			return len < MAX_TEXT_LENGTH || len % MAX_TEXT_LENGTH != 0;
		}

		private int calculateNumberOfOccupiedLinesOfRow(int y) {
			int length = rows.get(y).length();
			return length == 0 ? 1 : (length + MAX_TEXT_LENGTH - 1) / MAX_TEXT_LENGTH;
		}

		private void insertChars(String chars) {
			chars = InputHandler.handleBackspaceChars(chars, true); // Reduce the number of unnecessary operations.
			if (chars.isEmpty()) return;
			updateCaretAnimation();
			for (int i = 0; i < chars.length(); i++) {
				char c = chars.charAt(i);
				if (!insertChar(c)) break; // Terminates the processing of characters if no more character can be proceeded.
			}
		}

		/**
		 * Inserts a character to be inserted at the current cursor position. Cursor position is handled when needed.
		 * This controls whether the procedure should be terminated depending on how the characters are handled.
		 * @param c A printable or line break (line feed) or backspace {@code \b} character to be inserted. Regex: {@code [\p{Print}\b\n]+}
		 * @return {@code true} if the char is handled and valid to be continuing processing the following chars;
		 * otherwise, the procedure of processing characters is terminated.
		 */
		private boolean insertChar(char c) {
			if (c == '\b') { // Backspace
				if (cursorX == 0 && cursorY == 0) return true; // No effect; valid behaviour; handled
				else if (cursorX > 0) {
					rows.get(cursorY).deleteCharAt(--cursorX); // Remove the char in front of the cursor.
				} else { // cursorY > 0 && cursorX == 0
					// Combining the current row to the row above. Remove the current line and decrement cursorY by 1.
					rows.get(cursorY - 1).append(rows.remove(cursorY--));
				}

				return true; // A backspace is always not limited by the line count limit, and it could reduce characters when appropriate.
			} else if (c == '\n') { // Line break
				StringBuilder curRow = rows.get(cursorY);
				int rowLen = curRow.length();
				// If the row occupies more than 1 line and the string of the cursor until the end of line plus the last line
				//   does not contain enough chars to require a new line, so that the line break does not affect the line count.
				// The cursor should be within the row so that it does not create an empty new line, which breaks the case mentioned above.
				// One of the cases is that the cursor is at the end of line.
				if (rowLen > MAX_TEXT_LENGTH && cursorX > 0 && cursorX < rowLen &&
						cursorX <= rowLen - rowLen % MAX_TEXT_LENGTH && // The cursor should be in the line before the last line.
						MAX_TEXT_LENGTH - cursorX + rowLen % MAX_TEXT_LENGTH <= MAX_TEXT_LENGTH ||
						checkNewLineAvailable()) { // For other cases, a new line would always be required.
					rows.add(++cursorY, new StringBuilder(curRow.substring(cursorX))); // Create new builder from the split point.
					curRow.delete(cursorX, rowLen); // Remove string part starts with the split point.
					cursorX = 0; // A pointer to the new line after the break.
					return true;
				} else return false; // No line break; the char is discarded; no effect; unhandled
			} else {
				// If the current line has spaces to expand, or else a new line would be required.
				if (checkRowLineCapacity(cursorY) || checkNewLineAvailable()) {
					rows.get(cursorY).insert(cursorX++, c);
					return true;
				} else return false; // No more chars are accepted to expand at the current cursor position.
			}
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
		Rectangle bounds = menus[0].getBounds();
		Font.drawCentered(Localization.getLocalized("Use SHIFT-ENTER to confirm input."), screen, bounds.getBottom() + 8, Color.GRAY);
		editor.render(screen);
	}

	@Override
	public void tick(InputHandler input) {
		if (input.inputPressed("exit") || input.getKey("SHIFT-ENTER").clicked) {
			updateSign(levelDepth, x, y, editor.getLines());
			Game.exitDisplay();
			return;
		}

		editor.tick(input);
	}
}

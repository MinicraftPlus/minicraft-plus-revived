package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
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
import java.util.stream.Collectors;

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
		onScreenKeyboardMenu = OnScreenKeyboardMenu.checkAndCreateMenu();
		if (onScreenKeyboardMenu != null)
			onScreenKeyboardMenu.setVisible(false);
	}

	private class SignEditor {
		private final ClipboardHandler clipboard = new ClipboardHandler();
		private final ArrayList<StringBuilder> rows = new ArrayList<>();
		private int cursorX = 0, cursorY = 0;
		private int caretFrameCountDown = 60;
		private boolean caretShown = true;

		public SignEditor(@Nullable List<String> lines) {
			if (lines != null) lines.forEach(l -> rows.add(new StringBuilder(l)));
			while (rows.size() < MAX_ROW_COUNT)
				rows.add(new StringBuilder());
		}

		public List<String> getLines() {
			return rows.stream().map(StringBuilder::toString).collect(Collectors.toList());
		}

		public void tick(InputHandler input) {
			if (caretFrameCountDown-- == 0) { // Caret flashing animation
				caretFrameCountDown = 30;
				caretShown = !caretShown;
			}

			insertChars(input.getKeysTyped(null));
			if (input.getMappedKey("PAGE-UP").isClicked()) {
				cursorX = rows.get(cursorY = 0).length();
				updateCaretAnimation();
			} else if (input.getMappedKey("PAGE-DOWN").isClicked()) {
				cursorX = rows.get(cursorY = rows.size() - 1).length();
				updateCaretAnimation();

			} else if (input.getMappedKey("HOME").isClicked()) {
				cursorX = 0;
				updateCaretAnimation();
			} else if (input.getMappedKey("END").isClicked()) {
				cursorX = rows.get(cursorY).length();
				updateCaretAnimation();

			// Cursor navigating
			// As lines are centered, the character above in rendering would not always be the one in indices.
			// The position is set to the end of line when the cursor moved upward or downward.
			} else if (input.inputPressed("CURSOR-UP")) {
				cursorX = rows.get(cursorY == 0 ? cursorY = rows.size() - 1 : --cursorY).length();
				updateCaretAnimation();
			} else if (input.inputPressed("CURSOR-DOWN") || input.getMappedKey("ENTER").isClicked()) {
				cursorX = rows.get(cursorY == rows.size() - 1 ? cursorY = 0 : ++cursorY).length();
				updateCaretAnimation();
			} else if (input.inputPressed("CURSOR-LEFT")) {
				if (cursorX > 0) cursorX--;
				updateCaretAnimation();
			} else if (input.inputPressed("CURSOR-RIGHT")) {
				if (cursorX < rows.get(cursorY).length()) cursorX++;
				updateCaretAnimation();

			// Clipboard operations
			} else if (input.getMappedKey("CTRL-X").isClicked()) {
				cursorX = 0;
				clipboard.setClipboardContents(rows.get(cursorY).toString());
				rows.set(cursorY, new StringBuilder());
				updateCaretAnimation();
			} else if (input.getMappedKey("CTRL-C").isClicked()) {
				clipboard.setClipboardContents(rows.get(cursorY).toString());
				updateCaretAnimation();
			} else if (input.getMappedKey("CTRL-V").isClicked()) {
				insertChars(clipboard.getClipboardContents());
			}
		}

		public void render(Screen screen) {
			Rectangle bounds = menus[0].getBounds();
			int yPos = bounds.getTop() + MinicraftImage.boxWidth; // Upper border
			int centeredX = bounds.getLeft() + bounds.getWidth() / 2;
			for (StringBuilder row : rows) {
				Font.drawCentered(row.toString(), screen, yPos, Color.WHITE);
				//noinspection SuspiciousNameCombination
				yPos += MinicraftImage.boxWidth;
			}

			// Cursor rendering
			if (caretShown) {
				int lineWidth = rows.get(cursorY).length() * MinicraftImage.boxWidth;
				int displayX = cursorX * MinicraftImage.boxWidth;
				int displayY = cursorY * MinicraftImage.boxWidth;
				int lineBeginning = centeredX - lineWidth / 2;
				int cursorX = lineBeginning + displayX;
				int cursorY = bounds.getTop() + MinicraftImage.boxWidth + displayY;
				if (this.cursorX == rows.get(this.cursorY).length()) { // Replace cursor
					screen.drawLineSpecial(cursorX, cursorY + MinicraftImage.boxWidth - 1, 0, MinicraftImage.boxWidth);
				} else { // Insert cursor
					screen.drawLineSpecial(cursorX, cursorY, 1, MinicraftImage.boxWidth);
				}
			}
		}

		private void updateCaretAnimation() {
			caretShown = true;
			caretFrameCountDown = 120;
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
				if (cursorX == 0) return true; // No effect; valid behaviour; handled
				else { // cursorX > 0
					rows.get(cursorY).deleteCharAt(--cursorX); // Remove the char in front of the cursor.
				}

				return true; // A backspace is always not limited by the line count limit, and it could reduce characters when appropriate.
			} else if (c == '\n') { // Line break
				return true; // No effect; the char is ignored; handled
			} else {
				// If the current line has spaces to expand, or else a new line would be required.
				if (rows.get(cursorY).length() < MAX_TEXT_LENGTH) {
					rows.get(cursorY).insert(cursorX++, c);
					return true;
				} else return false; // No more chars are accepted to expand at the current cursor position.
			}
		}
	}

	OnScreenKeyboardMenu onScreenKeyboardMenu;

	@Override
	public void render(Screen screen) {
		super.render(screen);
		Rectangle bounds = menus[0].getBounds();
		Font.drawCentered(Localization.getLocalized("Use SHIFT-ENTER to confirm input."), screen, bounds.getBottom() + 8, Color.GRAY);
		editor.render(screen);
		if (onScreenKeyboardMenu != null)
			onScreenKeyboardMenu.render(screen);
	}

	@Override
	public void tick(InputHandler input) {
		boolean acted = false; // Checks if typing action is needed to be handled.
		boolean mainMethod = false;
		if (onScreenKeyboardMenu == null || !onScreenKeyboardMenu.isVisible()) {
			if (input.inputPressed("exit") || input.getMappedKey("SHIFT-ENTER").isClicked()) {
				updateSign(levelDepth, x, y, editor.getLines());
				Game.exitDisplay();
				return;
			}

			mainMethod = true;
		} else {
			try {
				onScreenKeyboardMenu.tick(input);
			} catch (OnScreenKeyboardMenu.OnScreenKeyboardMenuTickActionCompleted |
					 OnScreenKeyboardMenu.OnScreenKeyboardMenuBackspaceButtonActed e) {
				acted = true;
			}

			if (acted)
				editor.tick(input);

			if (input.getMappedKey("exit").isClicked() || input.getMappedKey("SHIFT-ENTER").isClicked()) { // Should not listen button press
				updateSign(levelDepth, x, y, editor.getLines());
				Game.exitDisplay();
				return;
			}

			if (input.buttonPressed(ControllerButton.X)) { // Hide the keyboard.
				onScreenKeyboardMenu.setVisible(!onScreenKeyboardMenu.isVisible());
			}
		}

		if (mainMethod || !onScreenKeyboardMenu.isVisible())
			editor.tick(input);
	}
}

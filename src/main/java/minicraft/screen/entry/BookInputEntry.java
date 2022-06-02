package minicraft.screen.entry;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

public class BookInputEntry extends InputEntry {
	private int lineSpacing;
	private int maxLines = 20;

	private int curPage = 0;
	private List<BookPage> userInput;

	public BookInputEntry(String prompt, String initValue) {
		super(prompt, null, 28, initValue);

		userInput = new ArrayList<>();
		for (String l : initValue.split("\0")) {
			userInput.add(new BookPage(l));
		}
	}

	private BookPage curPage() {
		return userInput.get(curPage);
	}

	private class BookPage {
		private List<BookLine> lines;
		/** Current line pointed */
		private int pointer = 0;

		public BookPage(@NotNull String iniVal) {
			lines = Arrays.stream(iniVal.split("\n")).map(v -> new BookLine(v)).collect(Collectors.toList());
		}

		public int lineCount() {
			int c = 0;
			for (BookLine l : lines) {
				c += l.lineCount();
			}
			return c;
		}

		private Entry<BookLine, Integer> getCurLine() {
			int c = 0;
			int over = 0;
			BookLine ln = null;
			for (BookLine l : lines) {
				c += l.lineCount();
				if (c >= pointer) {
					over = c-pointer;
					break;
				}
			}
			return new AbstractMap.SimpleEntry<>(ln, over);
		}

		public void pointerUp() {
			BookLine curL = lines.get(pointer);
			if (pointer == 0) {
				curL.pointer = 0;
				return;
			}
			if (curL.pointer >= maxLength) {
				curL.pointer -= maxLength;
				return;
			}

			BookLine l = lines.get(pointer--);
			int p = curL.pointer % maxLength;
			int r = l.length() % maxLength;
			if (p >= r) {
				l.pointer = l.length();
			} else {
				l.pointer = l.lineCount() * maxLength + p;
			}
		}
		public void pointerDown() {
			BookLine curL = lines.get(pointer);
			if (pointer + 1 == maxLines) {
				curL.pointer = curL.length();
				return;
			}
			int p = lines.get(pointer).pointer % maxLength;
			if (curL.lineCount() > curL.getCurLineN()) {
				if (curL.pointer + maxLength >= curL.length()) {
					curL.pointer += maxLength;
				} else {
					curL.pointer = curL.length();
				}
				return;
			}

			BookLine l = lines.get(pointer++);
			if (l.length() >= p) {
				l.pointer = p;
			} else {
				l.pointer = l.length();
			}
		}

		public void pointerLeft() {
			BookLine curL = lines.get(pointer);
			if (curL.pointer > 0) {
				curL.pointer--;
			} else if (pointer > 0) {
				BookLine l = lines.get(pointer--);
				l.pointer = l.length();
			}
		}
		public void pointerRight() {
			BookLine curL = lines.get(pointer);
			if (curL.pointer < curL.length()) {
				curL.pointer++;
			} else if (pointer + 1 < lines.size()) {
				BookLine l = lines.get(pointer++);
				l.pointer = 0;
			}
		}

		public void pointerHome() {
			// TODO
		}
		public void pointerEnd() {
			// TODO
		}

		public void insert(@NotNull String str) {
			// TODO
		}
		public void backspace() {
			// TODO
		}
		public void deleteCurLine() {
			// TODO
		}

		/** This value counts in the line counts (Y of display cursor) <p>
		 * For X of display cursor: {@link BookLine#pointer} % maxLength
		*/
		public int getRelativePointer() {
			int p = 0;
			for (int i = 0; i < pointer; i++) {
				if (i == pointer -1) p += Math.ceilDiv(lines.get(i).pointer, maxLength);
				else
					p += lines.get(i).lineCount();
			}
			return p;
		}

		private class BookLine {
			private StringBuilder line;
			private int pointer = 0;

			public BookLine(@NotNull String iniVal) { line = new StringBuilder(iniVal); }

			public int lineCount() {
				// Is it just simple int / maxLength + 1?
				int n = Math.ceilDiv(line.length(), maxLength);
				if (line.length() % maxLength == 0) n++;
				return n;
			}
			public int length() { return line.length(); }
			public String getLine() { return line.toString(); }
			public int getCurLineN() {
				int n = Math.ceilDiv(pointer, maxLength);
				if (pointer % maxLength == 0) n++;
				return n;
			}

			public void insertVal(@NotNull String v) { line.insert(pointer, v); }
		}
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getKey("cursor-down").clicked) {
			curPage().pointerDown();
			return;
		} else if (input.getKey("cursor-up").clicked) {
			curPage().pointerUp();
			return;
		} else if (input.getKey("cursor-left").clicked) {
			curPage().pointerLeft();
			return;
		} else if (input.getKey("cursor-right").clicked) {
			curPage().pointerRight();
			return;
		} else if (input.getKey("end").clicked) {
			curPage().pointerEnd();
			return;
		} else if (input.getKey("home").clicked) {
			curPage().pointerHome();
			return;

		} else if (input.getKey("page-up").clicked) {
			if (curPage > 0) {
				curPage--;
				curPage().pointer = 0;
				curPage().lines.get(0).pointer = 0;
			}
			return;

		} else if (input.getKey("page-down").clicked) {
			if (curPage + 1 < userInput.size()) {
				curPage++;
				curPage().pointer = 0;
				curPage().lines.get(0).pointer = 0;
			}
			return;
		}

		String newChar = input.addKeyTyped("0", regex, false);
		boolean noAdd = true;
		if (newChar.length() > 1) {
			noAdd = false;
			newChar = newChar.substring(1, 2);
		}

		if (newChar.length() == 0) {
			curPage().backspace();
		} else if (!noAdd) {
			curPage().insert(newChar);
		}

		if (input.getKey("CTRL-V").clicked) {
			curPage().insert(clipboardHandler.getClipboardContents());
			noAdd = false;

		} else if (input.getKey("CTRL-C").clicked) {
			clipboardHandler.setClipboardContents(curPage().getCurLine().getKey().getLine());

		} else if (input.getKey("CTRL-X").clicked) {
			clipboardHandler.setClipboardContents(curPage().getCurLine().getKey().getLine());
			curPage().deleteCurLine();
			noAdd = false;
		} // SHIFT-CTRL-X and SHIFT-CTRL-C are not decided to be added (Getting whole page content)

		if (noAdd && listener != null)
			listener.onChange(null);
	}

	public int getCurrentPageNum() { return curPage + 1; }
	public int getPageNum() { return userInput.size(); }

	/** The book content */
	public String getUserInput() { return String.join("\0", userInput.stream().map(p -> String.join("\n", p.lines.stream().map(e -> e.getLine()).toList())).toList()); }

	public String[] getLines() {
		List<String> lns = new ArrayList<>();
		for (BookPage.BookLine line : curPage().lines) {
			String[] ln = Font.getLines(line.getLine(), maxLines*8, Short.MAX_VALUE, 0, true);
			if (ln.length>1 && ln[ln.length-1].length()==0) ln = Arrays.copyOfRange(ln, 0, ln.length-1);
			for (String ll : ln) lns.add(ll);
		}

		return lns.toArray(new String[0]);
	}

	public int getCursorX() { return curPage().getCurLine().getKey().pointer % maxLength; }
	public int getCursorY() { return curPage().getRelativePointer(); }

	public String getPrompt() { return prompt; }

	public void setPrompt(String n) { prompt = n; }

	/** Rendering the BookInputEntry */
	public void render(Screen screen, FontStyle style) {
		Font.drawParagraph(screen, style, lineSpacing, getLines());
	}

	public void setChangeListener(ChangeListener l) {
		listener = l;
	}
}

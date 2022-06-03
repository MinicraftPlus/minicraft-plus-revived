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
	private static final int maxLines = 16;

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
		public List<BookLine> lines;
		/** Current line pointed */
		public int pointer = 0;

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

		private BookLine getCurLine() {
			int c = 0;
			BookLine ln = null;
			int lc = lineCount();
			for (BookLine l : lines) {
				c += l.lineCount();
				if (c >= lc) {
					ln = l;
					break;
				}
			}
			return ln;
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
			lines.get(pointer).pointer = 0;
		}
		public void pointerEnd() {
			BookLine curL = lines.get(pointer);
			curL.pointer = curL.length();
		}

		public void insert(@NotNull String str) {
			String[] ins = str.split("\n", -1);
			BookLine curL = lines.get(pointer);
			int count = 0;
			for (String s : ins) {
				if (lineCount() == maxLines) {
					if (curL.length() % maxLength + s.length() >= maxLength) {
						curL.insertVal(s.substring(0, curL.length() % maxLength + s.length() - maxLength));
					} else {
						curL.insertVal(s);
					}
					return;
				}

				if (count != 0) {
					lines.add(pointer++ + 1, new BookLine(curL.getLine().substring(curL.pointer)));
					curL = lines.get(pointer);
				}

				curL.insertVal(s);
				count++;
			}
		}
		public void backspace() {
			BookLine curL = lines.get(pointer);
			if (curL.pointer > 0) {
				curL.line.deleteCharAt(curL.pointer-- - 1);
			} else if (pointer > 0) {
				BookLine l = lines.get(pointer--);
				pointerEnd();
				l.insertVal(curL.getLine());
				lines.remove(curL);
			}
		}
		public void deleteCurLine() {
			lines.remove(pointer);
			if (lines.size() == 0){
				lines.add(new BookLine(""));
			}

			if (pointer > 0) {
				pointer--;
				pointerEnd();
			}
		}

		/** This value counts in the line counts (Y of display cursor) <p>
		 * For X of display cursor: {@link BookLine#pointer} % maxLength
		*/
		public int getRelativePointer() {
			int p = 0;
			for (int i = 0; i <= pointer; i++) {
				if (i == pointer) {
					p += lines.get(i).pointer / maxLength;
				} else {
					p += lines.get(i).lineCount();
				}
			}
			return p;
		}

		public class BookLine {
			public StringBuilder line;
			public int pointer = 0;

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

			public void insertVal(@NotNull String v) {
				line.insert(pointer, v);
				pointer += v.length();
			}
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
				curPage().pointerHome();

				// Delete the last page if the last page is empty
				BookPage lastPage = userInput.get(userInput.size() - 1);
				if (lastPage.lines.size() == 1 && lastPage.lines.get(0).length() == 0) {
					userInput.remove(userInput.size() - 1);
				}
			}
			return;

		} else if (input.getKey("page-down").clicked) {
			// Adding a new page if the current page is the last page
			if (curPage + 1 == userInput.size()) {
				userInput.add(new BookPage(""));
			}

			curPage++;
			curPage().pointer = 0;
			curPage().pointerHome();
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
			clipboardHandler.setClipboardContents(curPage().getCurLine().getLine());

		} else if (input.getKey("CTRL-X").clicked) {
			clipboardHandler.setClipboardContents(curPage().getCurLine().getLine());
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
			String[] ln = Font.getLines(line.getLine(), maxLength*8, Short.MAX_VALUE, 0, true);
			if (ln.length>1 && ln[ln.length-1].length()==0) ln = Arrays.copyOfRange(ln, 0, ln.length-1);
			for (String ll : ln) lns.add(ll);
		}

		return lns.toArray(new String[0]);
	}

	public int getCursorX() { return curPage().getCurLine().pointer % maxLength; }
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

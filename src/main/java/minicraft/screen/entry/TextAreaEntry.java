package minicraft.screen.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.util.ClipboardSystem;

public class TextAreaEntry extends InputEntry {
	private String prompt;
	private String regex;
	private int maxLength;
	private int maxLine;
	private int lineSpacing;

	private int cursor = 0;
	private int renderCursor;
	
	private List<String> userInput;
	
	private ChangeListener listener;
	private ChangeListener pageUpListener;
	private ChangeListener pageDownListener;

	private ClipboardSystem clipboardSystem = new ClipboardSystem();
	
	public TextAreaEntry(String prompt) {
		this(prompt, null, 0, 0, 0);
	}
	public TextAreaEntry(String prompt, String regex, int maxLen, int maxLn, int lineSp) {
		this(prompt, regex, maxLen, maxLn, lineSp, "");
	}
	public TextAreaEntry(String prompt, String regex, int maxLen, int maxLn, int lineSp, String initValue) {
		super(prompt, regex, maxLen, initValue);
		this.prompt = prompt;
		this.regex = regex;
		this.maxLength = maxLen > Short.MAX_VALUE ? Short.MAX_VALUE : maxLen;
		this.maxLine = maxLn;
		this.lineSpacing = lineSp;
		
		userInput = Arrays.asList(initValue.split("\n"));
	}
	
	@Override
	public void tick(InputHandler input) {
		int maxLen = maxLength == 0 ? Short.MAX_VALUE : maxLength;
		if (input.getKey("cursor-down").clicked) {
			int cursorX = cursor % Short.MAX_VALUE;
			int cursorY = cursor / Short.MAX_VALUE;
			String curString = userInput.get(cursorY);
			if (cursorX+maxLen > curString.length()) {
				if (cursorY+1 == userInput.size()) {
					cursor = curString.length() + cursorY*Short.MAX_VALUE;
					return;
				}
				String nextString = userInput.get(cursorY+1);
				if (cursorX > nextString.length()) {
					cursor = nextString.length() + (cursorY+1)*Short.MAX_VALUE;
				} else {
					cursor += Short.MAX_VALUE;
				}
			} else cursor = cursorX + maxLen+cursorY*Short.MAX_VALUE;
			return;
		} else if (input.getKey("cursor-up").clicked) {
			int cursorX = cursor % Short.MAX_VALUE;
			int cursorY = cursor / Short.MAX_VALUE;
			if (cursorX-maxLen < 0) {
				if (cursorY == 0) {
					cursor = 0;
					return;
				}
				String nextString = userInput.get(cursorY - 1);
				if (cursorX > nextString.length()) {
					cursor = nextString.length() + (cursorY-1)*Short.MAX_VALUE;
				} else {
					cursor -= Short.MAX_VALUE;
				}
			} else cursor = cursorX - maxLen+cursorY*Short.MAX_VALUE;
			return;
		} else if (input.getKey("cursor-left").clicked) {
			int cursorX = cursor % Short.MAX_VALUE;
			int cursorY = cursor / Short.MAX_VALUE;
			if (cursorX == 0) {
				if (cursorY == 0) return;
				String nextString = userInput.get(cursorY - 1);
				cursor = nextString.length() + (cursorY - 1)*Short.MAX_VALUE;
			} else cursor--;
			return;
		} else if (input.getKey("cursor-right").clicked) {
			int cursorX = cursor % Short.MAX_VALUE;
			int cursorY = cursor / Short.MAX_VALUE;
			String curString = userInput.get(cursorY);
			if (cursorX == curString.length()) {
				if (cursorY+1 == userInput.size()) return;
				cursor = 0+(cursorY+1) * Short.MAX_VALUE;
			} else cursor++;
			return;
		} else if (input.getKey("end").clicked) {
			int cursorY = cursor / Short.MAX_VALUE;
			cursor = userInput.get(cursorY).length()-1 + cursorY*Short.MAX_VALUE;
			return;
		} else if (input.getKey("home").clicked) {
			int cursorY = cursor / Short.MAX_VALUE;
			cursor = 0+cursorY * Short.MAX_VALUE;
			return;
		} else if (input.getKey("page-up").clicked) {
			if (pageUpListener!=null) pageUpListener.onChange(null);
			return;
		} else if (input.getKey("page-down").clicked) {
			if (pageDownListener!=null) pageDownListener.onChange(null);
			return;
		}

		String newChar = input.addKeyTyped("0", regex, false);
		boolean noAdd = false;
		if (newChar.length() == 1) noAdd = true;
		if (newChar.length() > 1) newChar = newChar.substring(1, 2);
		if (newChar.equals("\n")) {
			int cursorY = cursor / Short.MAX_VALUE;
			userInput.add(cursorY + 1, "");
			cursor = 0+(cursorY + 1)*Short.MAX_VALUE;
		} else if (newChar.length() == 0) {
			int cursorX = cursor % Short.MAX_VALUE;
			int cursorY = cursor / Short.MAX_VALUE;
			String curString = userInput.get(cursorY);
			if (curString.length() == 0) {
				if (cursorY != 0) {
					userInput.remove(cursorY);
					cursor = userInput.get(cursorY-1).length() + (cursorY+1)*Short.MAX_VALUE;
				}
			} else {
				if (cursorX == 0) {
					if (cursorY != 0) {
						cursor = userInput.get(cursorY-1).length() + (cursorY-1)*Short.MAX_VALUE;
						userInput.set(cursorY-1, userInput.get(cursorY-1)+curString);
						userInput.remove(cursorY);
					}
				} else {
					userInput.set(cursorY, new StringBuilder(curString).deleteCharAt(cursorX-1).toString());
					cursor--;
				}
			}
		} else if (!noAdd) {
			int cursorX = cursor % Short.MAX_VALUE;
			int cursorY = cursor / Short.MAX_VALUE;
			String curString = userInput.get(cursorY);
			userInput.set(cursorY, new StringBuilder(curString).insert(cursorX, newChar).toString());
			cursor++;
		}
		if (input.getKey("CTRL-V").clicked) {
			String[] newLns = clipboardSystem.getClipboardContents().split("\n");
			int cursorX = cursor % Short.MAX_VALUE;
			int cursorY = cursor / Short.MAX_VALUE;
			String curString = userInput.get(cursorY);
			String afterString = curString.substring(cursorX);
			for (int i = 0; i < newLns.length; i++) {
				String ln = newLns[i];
				if (i == 0) {
					userInput.set(cursorY, curString.substring(0, cursorX)+ln);
					cursorX += ln.length();
				} else {
					cursorY++;
					userInput.add(cursorY, ln);
					cursorX = ln.length();
				}
				if (i+1 == newLns.length) {
					String newCurString = userInput.get(cursorY);
					userInput.set(cursorY, newCurString + afterString);
				}
			}
			cursor = cursorX + cursorY*Short.MAX_VALUE;
			noAdd = false;
		}
		if (input.getKey("CTRL-C").clicked) {
			clipboardSystem.setClipboardContents(userInput.get(cursor / Short.MAX_VALUE));
		} else if (input.getKey("CTRL-X").clicked) {
			int cursorY = cursor / Short.MAX_VALUE;
			clipboardSystem.setClipboardContents(userInput.get(cursorY));
			userInput.remove(cursorY);
			cursor = cursorY == 0 ? 0 : userInput.get(cursorY-1).length() + (cursorY-1)*Short.MAX_VALUE;
			noAdd = false;
		} else if (input.getKey("CTRL-SHIFT-C").clicked) {
			clipboardSystem.setClipboardContents(getUserInput());
		} else if (input.getKey("CTRL-SHIFT-X").clicked) {
			clipboardSystem.setClipboardContents(getUserInput());
			userInput.removeAll(userInput);
			userInput.add("");
			cursor = 0;
			noAdd = false;
		}
		if (noAdd && listener != null)
			listener.onChange(userInput);
	}
	
	public String getUserInput() { return String.join("\n", userInput); }
	
	public String[] getLines() {
		List<String> lns = new ArrayList<>();
		int newCursor = cursor;
		int lineCount = 0;
		int maxLen = maxLength == 0 ? Short.MAX_VALUE : maxLength;
		for (String l : userInput) {
			String[] ln = Font.getLines(l, maxLen, Short.MAX_VALUE, 0, true);
			for (String ll : ln) lns.add(ll);
			if (lineCount == (int)cursor / Short.MAX_VALUE) {
				newCursor = cursor % Short.MAX_VALUE % maxLen // X
				+ (lns.size()-ln.length+((int)(cursor % Short.MAX_VALUE) / maxLen)) // Y
				* Short.MAX_VALUE;
			}
			lineCount++;
		}
		renderCursor = newCursor;
		return lns.toArray(new String[0]);
	}

	public String toString() {
		return getUserInput();
	}

	public int getCursor() { return cursor; }

	public int getRenderCursor() { return renderCursor; }
	
	public String getPrompt() { return prompt; }

	public void setPrompt(String n) { prompt = n; }

	public void render(Screen screen, int x, int y, boolean isSelected) {
		Font.drawParagraph(screen, new FontStyle(isValid() ? isSelected ? Color.GREEN : COL_UNSLCT : Color.RED).setXPos(x).setYPos(y), lineSpacing, Arrays.copyOfRange(getLines(), 0, maxLine));
	}
	
	public boolean isValid() {
		boolean match = true;
		for (String l : userInput) if (!l.matches(regex)) {match = false; break;}
		return match;
	}
	
	public void setChangeListener(ChangeListener l) {
		listener = l;
	}

	public void setPageUpListener(ChangeListener l) {
		pageUpListener = l;
	}
	
	public void setPageDownListener(ChangeListener l) {
		pageDownListener = l;
	}
}

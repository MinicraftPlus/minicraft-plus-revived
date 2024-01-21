package minicraft.screen.entry;

import minicraft.core.io.ClipboardHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Screen;
import minicraft.screen.RelPos;
import org.jetbrains.annotations.Nullable;

public class InputEntry extends ListEntry {

	private String prompt;
	private String regex;
	private int maxLength;
	private IntRange bounds;
	private IntRange renderingBounds;
	private RelPos entryPos;

	private String userInput;

	private ChangeListener listener;

	private ClipboardHandler clipboardHandler = new ClipboardHandler();

	public InputEntry(String prompt) {
		this(prompt, null, 0);
	}

	public InputEntry(String prompt, String regex, int maxLen) {
		this(prompt, regex, maxLen, "");
	}

	public InputEntry(String prompt, String regex, int maxLen, String initValue) {
		this.prompt = prompt;
		this.regex = regex;
		this.maxLength = maxLen;

		userInput = initValue;
	}

	@Override
	public void tick(InputHandler input) {
		String prev = userInput;
		userInput = input.addKeyTyped(userInput, regex);
		if (!prev.equals(userInput)) {
			if (listener != null) {
				listener.onChange(userInput);
			}

			updateCursorDisplacement();
		}

		if (maxLength > 0 && userInput.length() > maxLength)
			userInput = userInput.substring(0, maxLength); // truncates extra
		if (input.getMappedKey("CTRL-V").isClicked()) {
			userInput = userInput + clipboardHandler.getClipboardContents();
		}
		if (!userInput.equals("")) {
			if (input.getMappedKey("CTRL-C").isClicked()) {
				clipboardHandler.setClipboardContents(userInput);
			}
			if (input.getMappedKey("CTRL-X").isClicked()) {
				clipboardHandler.setClipboardContents(userInput);
				userInput = "";
			}
		}
	}

	public String getUserInput() {
		return userInput;
	}

	public String toString() {
		return Localization.getLocalized(prompt) + (prompt.length() == 0 ? "" : ": ") + userInput;
	}

	public void render(Screen screen, int x, int y, boolean isSelected, @Nullable IntRange bounds) {
		Font.draw(toString(), screen, x, y, isValid() ? isSelected ? Color.GREEN : COL_UNSLCT : Color.RED, bounds);
	}

	private void updateCursorDisplacement() {
		IntRange renderingBounds = this.renderingBounds == null ? bounds : this.renderingBounds;
		if (bounds != null && entryPos != null) {
			int spaceWidth = renderingBounds.upper - bounds.lower;
			int width = getWidth();
			if (width <= spaceWidth) {
				xDisplacement = 0;
			} else {
				if (entryPos.xIndex == 0) { // Left
					xDisplacement = spaceWidth - width;
				} else if (entryPos.xIndex == 1) { // Center
					// Assume that the menu is rebuilt when updated.
					xDisplacement = (spaceWidth - width) / 2;
				} // xIndex == 2 is not handled.
			}
		}
	}

	public InputEntry setBounds(IntRange bounds) {
		this.bounds = bounds;
		return this;
	}

	public InputEntry setEntryPos(RelPos entryPos) {
		this.entryPos = entryPos;
		return this;
	}

	public InputEntry setRenderingBounds(IntRange bounds) {
		this.renderingBounds = bounds;
		return this;
	}

	public boolean isValid() {
		return userInput.matches(regex);
	}

	public void setChangeListener(ChangeListener l) {
		listener = l;
	}
}

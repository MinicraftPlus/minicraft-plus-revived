package minicraft.screen.entry;

import minicraft.core.io.ClipboardHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import org.intellij.lang.annotations.RegExp;

public class InputEntry extends ListEntry implements UserMutable {
	@RegExp
	public static final String regexNumber = "[0-9]+";
	@RegExp
	public static final String regexNegNumber = "[0-9-]+";
	@RegExp
	public static final String regexNegNumberOpt = "[0-9-]*";

	protected static final int DARK_RED = Color.tint(Color.RED, -1, true);

	private String prompt;
	private String regex;
	private int maxLength;

	private String userInput;

	protected ChangeListener listener;

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
		if (!prev.equals(userInput) && listener != null)
			listener.onChange(userInput);

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

	public void setUserInput(String text) {
		userInput = text;
		listener.onChange(text);
	}

	public String toString() {
		return Localization.getLocalized(prompt) + (prompt.length() == 0 ? "" : ": ") + userInput;
	}

	public void render(Screen screen, int x, int y, boolean isSelected) {
		Font.draw(toString(), screen, x, y, isValid() ? isSelected ? Color.GREEN : COL_UNSLCT : isSelected ? Color.RED : DARK_RED);
	}

	// TODO Review this, if userInput contains any unmatched char, it is either regex or InputHanlder#getKeyTyped is corrupted.
	public boolean isValid() {
		return regex == null || userInput.matches(regex);
	}

	@Override
	public void setChangeListener(ChangeListener l) {
		listener = l;
	}
}

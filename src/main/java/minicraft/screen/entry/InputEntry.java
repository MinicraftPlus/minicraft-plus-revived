package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.util.ClipboardSystem;

public class InputEntry extends ListEntry {
	
	private String prompt;
	private String regex;
	private int maxLength;
	
	private String userInput;
	
	private ChangeListener listener;

	private ClipboardSystem clipboardSystem = new ClipboardSystem();
	
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
		if (input.getKey("CTRL-V").clicked) {
			userInput = userInput + clipboardSystem.getClipboardContents();
		}
		if (!userInput.equals("")) {
			if (input.getKey("CTRL-C").clicked) {
				clipboardSystem.setClipboardContents(userInput);
			}
			if (input.getKey("CTRL-X").clicked) {
				clipboardSystem.setClipboardContents(userInput);
				userInput = "";
			}
		}
	}
	
	public String getUserInput() { return userInput; }
	
	public String toString() {
		return Localization.getLocalized(prompt) + (prompt.length() == 0 ? "" : ": ") + userInput;
	}
	
	public void render(Screen screen, int x, int y, boolean isSelected) {
		Font.draw(toString(), screen, x, y, isValid() ? isSelected ? Color.GREEN : COL_UNSLCT : Color.RED);
	}
	
	public boolean isValid() {
		return userInput.matches(regex);
	}
	
	public void setChangeListener(ChangeListener l) {
		listener = l;
	}
}

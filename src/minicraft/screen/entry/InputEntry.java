package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class InputEntry extends ListEntry {
	
	private String prompt;
	private String regex;
	private int maxLength;
	
	private String userInput;
	
	private ChangeListener listener;
	
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
		if(!prev.equals(userInput) && listener != null)
			listener.onChange(userInput);
		
		if(maxLength > 0 && userInput.length() > maxLength)
			userInput = userInput.substring(0, maxLength); // truncates extra
	}
	
	public String getUserInput() { return userInput; }
	
	public String toString() {
		return prompt+(prompt.length()==0?"":": ") + userInput;
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

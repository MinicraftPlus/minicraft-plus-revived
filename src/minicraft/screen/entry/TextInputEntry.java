package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

import java.util.regex.Pattern;

// This entry type will record the letters that the user presses while this entry is selected.
public class TextInputEntry extends ConfigEntry<String> {
	
	private String typing;
	private String label;
	
	private int maxLength;
	
	private static final Pattern control = Pattern.compile("\\p{Print}"); // should match only printable characters.
	private Pattern inputChecker;
	
	public TextInputEntry(String label) {
		this(label, null, -1);
	}
	public TextInputEntry(String label, Pattern pattern, int maxLength) {
		this.label = label;
		inputChecker = pattern;
		this.maxLength = maxLength;
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		if(input.lastKeyTyped.length() > 0 && (maxLength < 0 || typing.length() < maxLength)) {
			String letter = input.lastKeyTyped;
			input.lastKeyTyped = "";
			if(control.matcher(letter).matches() &&
					(inputChecker == null || inputChecker.matcher(letter).matches())
					) {
				typing += letter;
				onTextChange();
			}
		}
		
		if(input.getKey("backspace").clicked && typing.length() > 0) {
			// backspace counts as a letter itself, but we don't have to worry about it if it's part of the regex.
			typing = typing.substring(0, typing.length()-1);
		}
	}
	
	@Override
	public void render(Screen screen, FontStyle style) {
		style.draw(label + ": " + getValue(), screen);
	}
	
	/*private void checkKeyTyped(InputHandler input) {
		if(input.lastKeyTyped.length() > 0) {
			String letter = input.lastKeyTyped;
			input.lastKeyTyped = "";
			if(control.matcher(letter).matches() && (inputChecker == null || )) {
				typing += letter;
				onTextInput(letter);
			}
		}
	}*/
	
	/**
	 * This is an API method that is called whenever the "typing" text changes.
	 */
	protected void onTextChange() {
	}
	
	/**
	 * Sets the typing text.
	 * @param value
	 */
	@Override
	public void setValue(String value) {
		typing = value;
	}
	
	@Override
	public String getValue() {
		return typing;
	}
}

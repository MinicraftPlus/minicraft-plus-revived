package minicraft.screen.entry;

import minicraft.InputHandler;

public class InputEntry implements ListEntry {
	
	private String prompt;
	private String regex;
	private int maxLength;
	
	private String userInput;
	
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
		userInput = input.addKeyTyped(userInput, regex);
		
		if(maxLength > 0 && userInput.length() > maxLength)
			userInput = userInput.substring(0, maxLength); // truncates extra
	}
	
	public String getUserInput() { return userInput; }
	
	public String toString() {
		return prompt+": " + userInput;
	}
}

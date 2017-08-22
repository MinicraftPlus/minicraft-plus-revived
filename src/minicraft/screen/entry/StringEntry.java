package minicraft.screen.entry;

import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

public class StringEntry extends ListEntry {
	
	private String text;
	
	public StringEntry(String text) {
		this.text = text;
	}
	
	@Override
	public void render(Screen screen, FontStyle style) {
		style.draw(text, screen);
	}
	
	public String getText() { return text; }
	
	/**
	 * This converts an Array of {@link String}s into an array of {@link StringEntry}s, one for each original String.
	 * @param values an array of Strings
	 * @return the array of StringEntries
	 */
	public static StringEntry[] useStringArray(String... values) {
		StringEntry[] entries = new StringEntry[values.length];
		for(int i = 0; i < entries.length; i++)
			entries[i] = new StringEntry(values[i]);
		
		return entries;
	}
	
	public String toString() {
		return "StringEntry(\"" + text + "\")";
	}
}

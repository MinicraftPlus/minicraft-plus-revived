package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Font;

public class SelectEntry extends ListEntry {
	
	private Action onSelect;
	private String text;
	
	/**
	 * Creates a new entry which acts as a button. 
	 * Can do an action when it is selected.
	 * @param text Text displayed on this entry
	 * @param onSelect Action which happens when the entry is selected
	 */
	public SelectEntry(String text, Action onSelect) {
		this.onSelect = onSelect;
		this.text = text;
	}
	
	/**
	 * Changes the text of the entry.
	 * @param text new text
	 */
	void setText(String text) { this.text = text; }
	
	@Override
	public void tick(InputHandler input) {
		if(input.getKey("select").clicked && onSelect != null) {
			Sound.confirm.play();
			onSelect.act();
		}
	}
	
	@Override
	public int getWidth() {
		return Font.textWidth(text);
	}
	
	@Override
	public String toString() {
		return text;
	}
}

package minicraft.screen2.entry;

import minicraft.InputHandler;
import minicraft.gfx.Font;

public class SelectEntry implements ListEntry {
	
	private Action onSelect;
	private String text;
	
	public SelectEntry(String text, Action onSelect) {
		this.onSelect = onSelect;
		this.text = text;
	}
	
	@Override
	public void tick(InputHandler input) {
		if(input.getKey("select").clicked)
			onSelect.act();
	}
	
	@Override
	public int getWidth() {
		return Font.textWidth(text);
	}
	
	public String toString() {
		return text;
	}
}

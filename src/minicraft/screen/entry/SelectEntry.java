package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Font;

public class SelectEntry extends ListEntry {
	
	private Action onSelect;
	private String text;
	
	public SelectEntry(String text, Action onSelect) {
		this.onSelect = onSelect;
		this.text = text;
	}
	
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
	
	public String toString() {
		return text;
	}
}

package minicraft.screen.entry;

import minicraft.core.Action;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Font;

public class SelectEntry extends ListEntry {
	
	private Action onSelect;
	private String text;
	private boolean localize;
	
	public SelectEntry(String text, Action onSelect) { this(text, onSelect, true); }
	public SelectEntry(String text, Action onSelect, boolean localize) {
		this.onSelect = onSelect;
		this.text = text;
		this.localize = localize;
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
	public int getWidth() { return Font.textWidth(toString()); }
	
	@Override
	public String toString() { return localize ? Localization.getLocalized(text) : text; }
}

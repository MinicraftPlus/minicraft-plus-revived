package minicraft.screen.entry;

import minicraft.InputHandler;

// an unselectable line.
public class StringEntry implements ListEntry {
	
	private String text;
	
	public StringEntry(String text) {
		this.text = text;
	}
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public boolean isSelectable() { return false; }
	
	@Override
	public String toString() { return text; }
}

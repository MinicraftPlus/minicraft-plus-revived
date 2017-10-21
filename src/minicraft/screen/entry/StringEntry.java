package minicraft.screen.entry;

import minicraft.core.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

// an unselectable line.
public class StringEntry extends ListEntry {
	
	private static final int DEFAULT_COLOR = Color.WHITE;
	
	private String text;
	private int color;
	
	public static StringEntry[] useLines(String... lines) {
		return useLines(DEFAULT_COLOR, lines);
	}
	public static StringEntry[] useLines(int color, String... lines) {
		StringEntry[] entries = new StringEntry[lines.length];
		for(int i = 0; i < lines.length; i++)
			entries[i] = new StringEntry(lines[i], color);
		
		return entries;
	}
	
	public StringEntry(String text) {
		this(text, DEFAULT_COLOR);
	}
	public StringEntry(String text, int color) {
		setSelectable(false);
		this.text = text;
		this.color = color;
	}
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		Font.draw(toString(), screen, x, y, color);
	}
	
	@Override
	public String toString() { return text; }
}

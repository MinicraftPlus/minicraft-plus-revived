package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;

// an unselectable line.
public class StringEntry extends ListEntry {

	private static final int DEFAULT_COLOR = Color.WHITE;

	private String text;
	private int color;

	/**
	 *
	 */
	public static StringEntry[] useLines(String... lines) {
		return useLines(DEFAULT_COLOR, lines);
	}
	public static StringEntry[] useLines(int color, String... lines) {
		StringEntry[] entries = new StringEntry[lines.length];
		for (int i = 0; i < lines.length; i++)
			entries[i] = new StringEntry(lines[i], color);

		return entries;
	}

	public StringEntry(String text) {
		this(text, DEFAULT_COLOR);
	}
	public StringEntry(String text, int color) {
		setSelectable(false);
		this.text = Localization.getLocalized(text);
		this.color = color;
	}

	public void setText(String text) {
		this.text = Localization.getLocalized(text);
	}

	@Override
	public void tick(InputHandler input) {}

	@Override
	public int getColor(boolean isSelected) { return color; }

	@Override
	public String toString() { return text; }
}

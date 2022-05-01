package minicraft.screen.entry;

import java.util.ArrayList;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

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
	public static StringEntry[] useLines(int color, String... lines) { return useLines(color, false, lines); }
	public static StringEntry[] useLines(int color, boolean getLocalized, String... lines) {
		ArrayList<String> lns = new ArrayList<>();
		for (String l : lines) {
			for (String ll : Font.getLines(getLocalized? Localization.getLocalized(l): l, Screen.w-20, Screen.h*2, 0)) lns.add(ll);
		}
		StringEntry[] entries = new StringEntry[lns.size()];
		for (int i = 0; i < lns.size(); i++)
			entries[i] = new StringEntry(lns.get(i), color);

		return entries;
	}

	public StringEntry(String text) {
		this(text, DEFAULT_COLOR);
	}
	public StringEntry(String text, int color) { this(text, color, false); }
	public StringEntry(String text, int color, boolean getLocalized) {
		setSelectable(false);
		this.text = getLocalized? Localization.getLocalized(text): text;
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

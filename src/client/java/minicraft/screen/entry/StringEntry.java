package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

import java.util.ArrayList;

// an unselectable line.
public class StringEntry extends ListEntry {

	private static final int DEFAULT_COLOR = Color.WHITE;

	private String text;
	private int color;
	private boolean localize;

	/**
	 *
	 */
	public static StringEntry[] useLines(String... lines) {
		return useLines(DEFAULT_COLOR, lines);
	}

	public static StringEntry[] useLines(int color, String... lines) {
		return useLines(color, true, lines);
	}

	public static StringEntry[] useLines(int color, boolean localize, String... lines) {
		ArrayList<String> lns = new ArrayList<>();
		for (String l : lines) {
			for (String ll : Font.getLines(localize ? Localization.getLocalized(l) : l, Screen.w - 20, Screen.h * 2, 0))
				lns.add(ll);
		}
		StringEntry[] entries = new StringEntry[lns.size()];
		for (int i = 0; i < lns.size(); i++)
			entries[i] = new StringEntry(lns.get(i), color);

		return entries;
	}

	public StringEntry(String text) {
		this(text, DEFAULT_COLOR);
	}

	public StringEntry(String text, boolean localize) {
		this(text, DEFAULT_COLOR, localize);
	} // This might be false as the text might have been localized already.

	public StringEntry(String text, int color) {
		this(text, color, true);
	} // This should be always true with the new localization IDs.

	public StringEntry(String text, int color, boolean localize) {
		setSelectable(false);
		this.text = text;
		this.localize = localize;
		this.color = color;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void tick(InputHandler input) {
	}

	@Override
	public int getColor(boolean isSelected) {
		return color;
	}

	@Override
	public String toString() {
		return localize ? Localization.getLocalized(text) : text;
	}
}

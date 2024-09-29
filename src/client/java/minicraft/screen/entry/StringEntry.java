package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.util.DisplayString;

import java.util.ArrayList;
import java.util.Arrays;

// an unselectable line.
public class StringEntry extends SelectableStringEntry {

	private static final int DEFAULT_COLOR = Color.WHITE;

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
			lns.addAll(Arrays.asList(Font.getLines(localize ? Localization.getLocalized(l) : l, Screen.w - 20, Screen.h * 2, 0)));
		}
		StringEntry[] entries = new StringEntry[lns.size()];
		for (int i = 0; i < lns.size(); i++)
			entries[i] = new StringEntry(new DisplayString.StaticString(lns.get(i)), color);

		return entries;
	}

	public StringEntry(DisplayString text) {
		this(text, DEFAULT_COLOR);
	}

	public StringEntry(DisplayString text, int color) {
		super(text, color);
		setSelectable(false);
	}
}

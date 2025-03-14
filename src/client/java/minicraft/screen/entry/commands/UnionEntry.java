package minicraft.screen.entry.commands;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;

import java.util.Arrays;
import java.util.List;

public class UnionEntry<T extends ListEntry> extends ListEntry {
	private final List<T> entries;

	private int selection = 0;

	@SafeVarargs
	public UnionEntry(T... entries) {
		this.entries = Arrays.asList(entries);
	}

	public void setSelection(int selection) {
		this.selection = selection;
	}

	public int getSelection() {
		return selection;
	}

	public T getSelectedEntry() {
		return entries.get(selection);
	}

	public int size() {
		return entries.size();
	}

	@Override
	public void tick(InputHandler input) {
		entries.get(selection).tick(input);
	}

	@Override
	public void render(Screen screen, int x, int y, boolean isSelected, String contain, int containColor) {
		entries.get(selection).render(screen, x, y, isSelected, contain, containColor);
	}

	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		entries.get(selection).render(screen, x, y, isSelected);
	}

	@Override
	public int getColor(boolean isSelected) {
		return entries.get(selection).getColor(isSelected);
	}

	@Override
	public int getWidth() {
		return entries.get(selection).getWidth();
	}

	@Override
	public String toString() {
		return entries.get(selection).toString();
	}
}

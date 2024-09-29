package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.util.DisplayString;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

public class SelectableStringEntry extends ListEntry {

	private static final int DEFAULT_COLOR = Color.WHITE;

	private DisplayString text;
	private final int color;

	private EntryScrollingTicker ticker;

	public SelectableStringEntry(DisplayString text) {
		this(text, DEFAULT_COLOR);
	}
	public SelectableStringEntry(DisplayString text, int color) {
		this.text = text;
		this.color = color;
	}

	public void setExceedingAlternatingScrollingTicker() {
		ticker = new ExceedingHorizontallyAlternatingScrollingTicker();
	}

	public void setScrollerScrollingTicker() { setScrollerScrollingTicker(-1); }
	public void setScrollerScrollingTicker(@MagicConstant int direction) {
		ticker = new HorizontalScrollerScrollingTicker(direction);
	}

	public void setText(DisplayString text) {
		this.text = text;
	}

	@Override
	public void tick(InputHandler input) {}

	@Override
	public void tickScrollingTicker(@NotNull EntryXAccessor accessor) {
		if (ticker != null)
			ticker.tick(accessor);
	}

	@Override
	public boolean isScrollingTickerSet() {
		return ticker != null;
	}

	@Override
	public int getColor(boolean isSelected) { return color; }

	@Override
	public String toString() { return text.toString(); }
}

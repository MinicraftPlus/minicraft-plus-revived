package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

public class SelectableStringEntry extends ListEntry {

	private static final int DEFAULT_COLOR = Color.WHITE;

	private String text;
	private final int color;
	private final boolean localize;

	private EntryScrollingTicker ticker;

	public SelectableStringEntry(String text) {
		this(text, DEFAULT_COLOR);
	}
	public SelectableStringEntry(String text, boolean localize) { this(text, DEFAULT_COLOR, localize); } // This might be false as the text might have been localized already.
	public SelectableStringEntry(String text, int color) { this(text, color, true); } // This should be always true with the new localization IDs.
	public SelectableStringEntry(String text, int color, boolean localize) {
		this.text = text;
		this.localize = localize;
		this.color = color;
	}

	public void setExceedingAlternatingScrollingTicker() {
		ticker = new ExceedingHorizontallyAlternatingScrollingTicker();
	}

	public void setScrollerScrollingTicker() { setScrollerScrollingTicker(-1); }
	public void setScrollerScrollingTicker(@MagicConstant int direction) {
		ticker = new HorizontalScrollerScrollingTicker(direction);
	}

	public void setText(String text) {
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
	public String toString() { return localize? Localization.getLocalized(text): text; }
}

package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.MinicraftImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class SelectableStringEntry extends ListEntry {

	private static final int DEFAULT_COLOR = Color.WHITE;

	private String text;
	private final int color;
	private final boolean localize;

	private TextRenderTicker ticker;

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

	protected abstract static class TextRenderTicker {
		protected static final int DEFAULT_CYCLING_PERIOD = 90;

		protected int tick = 0;

		public abstract void tick();
	}

	private class HorizontalAlternatingScrollingTextRenderTicker extends TextRenderTicker {
		private final IntRange bounds;
		private final int originX;

		@Range(from = -1, to = 1)
		private int direction = 0; // Number line direction; text movement

		public HorizontalAlternatingScrollingTextRenderTicker(@NotNull IntRange bounds, int originX) {
			this.bounds = bounds;
			this.originX = originX;
		}

		@Override
		public void tick() {
			int width = getWidth();
			if (width > bounds.upper - bounds.lower) {
				if (direction != 0) {
					if (tick++ == 5) {
						xDisplacement += direction * MinicraftImage.boxWidth;
						if (originX + xDisplacement == bounds.lower || // Left side of text tips at left bound
							originX + xDisplacement + width == bounds.upper) { // Stop if destination is reached
							direction = 0;
						}

						tick = 0;
					}
				} else if (tick++ == DEFAULT_CYCLING_PERIOD) {
					if (originX + xDisplacement + width == bounds.upper) { // Right side of text tips at right bound
						direction = 1; // Right
					} else {
						direction = -1; // Left
					}

					tick = 0;
				}
			}
		}
	}

	private class HorizontalScrollingTextRenderTicker extends TextRenderTicker {
		private final IntRange bounds;
		private final int originX;

		private boolean moving = false;

		public HorizontalScrollingTextRenderTicker(@NotNull IntRange bounds, int originX) {
			this.bounds = bounds;
			this.originX = originX;
		}

		@Override
		public void tick() {
			int width = getWidth();
			if (width > bounds.upper - bounds.lower) {
				if (moving) {
					if (tick++ == 5) {
						if (originX + xDisplacement + width == bounds.lower) { // Right side of text tips at left bound
							xDisplacement = bounds.upper - originX; // Moves to the rightmost
						}

						xDisplacement -= MinicraftImage.boxWidth;
						if (xDisplacement == 0) { // Moves back to the original point
							moving = false; // Pauses the scrolling
						}

						tick = 0;
					}
				} else if (tick++ == DEFAULT_CYCLING_PERIOD) {
					moving = true;
					tick = 0;
				}
			}
		}
	}

	@Override
	public boolean hideWhenOverflow() {
		return true;
	}

	public void setAlternatingScrollingTextRenderTicker(@NotNull IntRange bounds, int originX) {
		ticker = new HorizontalAlternatingScrollingTextRenderTicker(bounds, originX);
	}

	public void setScrollingTextRenderTicker(@NotNull IntRange bounds, int originX) {
		ticker = new HorizontalScrollingTextRenderTicker(bounds, originX);
	}

	public void setRenderTicker(TextRenderTicker renderTicker) {
		ticker = renderTicker;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void tick(InputHandler input) {
		if (ticker != null)
			ticker.tick();
	}

	@Override
	public int getColor(boolean isSelected) { return color; }

	@Override
	public String toString() { return localize? Localization.getLocalized(text): text; }
}

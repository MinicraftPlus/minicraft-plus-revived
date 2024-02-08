package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.MinicraftImage;
import minicraft.screen.RelPos;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

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

	public interface EntryXAccessor {
		int getWidth();
		int getX(RelPos anchor);
		void setX(RelPos anchor, int x);
		void translateX(int displacement);
		void setAnchors(RelPos anchor); // This is recommended to be invoked first.
		int getLeftBound(RelPos anchor);
		int getRightBound(RelPos anchor);
	}

	protected abstract static class EntryScrollingTicker {
		protected static final int DEFAULT_CYCLING_PERIOD = 90; // in ticks

		protected int tick = 0;

		public abstract void tick(@NotNull EntryXAccessor accessor);
	}

	private static class ExceedingHorizontallyAlternatingScrollingTicker extends EntryScrollingTicker {
		@Range(from = -1, to = 1)
		private int direction = 0; // Number line direction; text movement

		@Override
		public void tick(@NotNull EntryXAccessor accessor) {
			RelPos refAnchor = RelPos.getPos(1 - direction, 0);
			accessor.setAnchors(refAnchor);
			int x = accessor.getX(refAnchor);
			// Proceeds when the entry is out of bounds.
			if (x < accessor.getLeftBound(refAnchor) || x > accessor.getRightBound(refAnchor)) {
				if (direction != 0) {
					if (tick++ == 5) {
						x += direction * MinicraftImage.boxWidth;
						if ((direction == 1 ? x - accessor.getLeftBound(refAnchor) :
							accessor.getRightBound(refAnchor) - x) >= 0) {
							// Alignment correction
							x = direction == 1 ? accessor.getLeftBound(refAnchor) : accessor.getRightBound(refAnchor);
							direction = 0; // Stops when destination is reached.
						}

						accessor.setX(refAnchor, x);
						tick = 0;
					}
				} else if (tick++ == DEFAULT_CYCLING_PERIOD) {
					if (x <= accessor.getRightBound(refAnchor))
						direction = 1; // Right
					else
						direction = -1; // Left
					tick = 0;
				}
			} else tick = 0;
		}
	}

	private static class HorizontalScrollerScrollingTicker extends EntryScrollingTicker {
		@MagicConstant(intValues = {-1, 1})
		private final int direction;

		public HorizontalScrollerScrollingTicker(@MagicConstant int direction) {
			switch (direction) {
				case -1: case 1:
					this.direction = direction; break;
				default:
					throw new IllegalArgumentException("direction; input: " + direction);
			}
		}

		private boolean moving = false;

		@Override
		public void tick(@NotNull EntryXAccessor accessor) {
			RelPos refAnchor = direction == 1 ? RelPos.LEFT : RelPos.RIGHT;
			accessor.setAnchors(refAnchor);
			int x = accessor.getX(refAnchor);
			int width = accessor.getWidth();
			int lw = direction == -1 ? -width : 0;
			int rw = direction == 1 ? width : 0;
			// Proceeds when the entry is out of bounds.
			if (x < accessor.getLeftBound(refAnchor) || x > accessor.getRightBound(refAnchor)) {
				if (moving) {
					if (tick++ == 5) {
						if (direction == 1 && x >= accessor.getRightBound(refAnchor) + rw) { // Left side reaches right bound
							x += accessor.getLeftBound(refAnchor) - x - width;
						} else if (direction == -1 && x <= accessor.getLeftBound(refAnchor) + lw) {
							x += accessor.getRightBound(refAnchor) - x + width;
						} else {
							x += direction * MinicraftImage.boxWidth;
						}

						int diff = direction == 1 ? accessor.getRightBound(refAnchor) - x :
							x - accessor.getLeftBound(refAnchor);
						if (diff >= 0 && diff < MinicraftImage.boxWidth) { // Moves back to the original point
							moving = false; // Pauses the scrolling
							// Alignment correction
							x = direction == 1 ? accessor.getRightBound(refAnchor) : accessor.getLeftBound(refAnchor);
						}

						accessor.setX(refAnchor, x);
						tick = 0;
					}
				} else if (tick++ == DEFAULT_CYCLING_PERIOD) {
					moving = true;
					tick = 0;
				}
			} else tick = 0;
		}
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

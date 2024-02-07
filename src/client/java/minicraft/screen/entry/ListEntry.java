package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Screen;
import org.jetbrains.annotations.Nullable;

public abstract class ListEntry {

	public static final int COL_UNSLCT = Color.GRAY;
	public static final int COL_SLCT = Color.WHITE;

	private boolean selectable = true, visible = true;

	protected int xDisplacement = 0; // Displacing to the left for negative values, right for positive values.

	/**
	 * Ticks the entry. Used to handle input from the InputHandler
	 *
	 * @param input InputHandler used to get player input.
	 */
	public abstract void tick(InputHandler input);

	public void render(Screen screen, int x, int y, boolean isSelected, @Nullable IntRange bounds, String contain, int containColor) {
		if (!visible) {
			return;
		}

		render(screen, x, y, isSelected, bounds);
		if (contain == null || contain.isEmpty()) {
			return;
		}

		String string = toString();

		Font.drawColor(string.replace(contain, String.format("%s%s%s", Color.toStringCode(isSelected ? containColor :
				Color.tint(containColor, -1, true)), contain, Color.WHITE_CODE)), screen, x, y);
	}

	public static class IntRange {
		public final int lower;
		public final int upper;
		public IntRange(int lower, int upper) {
			this.lower = lower;
			this.upper = upper;
		}
	}

	/**
	 * Renders the entry to the given screen.
	 * Coordinate origin is in the top left corner of the entry space.
	 *
	 * @param screen     Screen to render the entry to
	 * @param x          X coordinate
	 * @param y          Y coordinate
	 * @param isSelected true if the entry is selected, false otherwise
	 * @param bounds X rendering bounds
	 */
	public void render(Screen screen, int x, int y, boolean isSelected, @Nullable IntRange bounds) {
		if (visible) {
			String text = toString().replace(Color.WHITE_CODE + Color.GRAY_CODE, Color.toStringCode(getColor(isSelected)));
			if (text.contains(String.valueOf(Color.COLOR_CHAR)))
				Font.drawColor(Color.toStringCode(getColor(isSelected)) + text, screen, x, y, bounds);
			else
				Font.draw(text, screen, x, y, getColor(isSelected), bounds);
		}
	}

	/**
	 * Returns the current color depending on if the entry is selected.
	 *
	 * @param isSelected true if the entry is selected, false otherwise
	 * @return the current entry color
	 */
	public int getColor(boolean isSelected) {
		return isSelected ? COL_SLCT : COL_UNSLCT;
	}

	/**
	 * Calculates the width of the entry.
	 *
	 * @return the entry's width
	 */
	public int getWidth() {
		return Font.textWidth(toString());
	}

	/**
	 * Calculates the height of the entry.
	 *
	 * @return the entry's height
	 */
	public static int getHeight() {
		return Font.textHeight();
	}

	/**
	 * Displaces the entry to the left in rendering, with input right.
	 * @param boundsWidth the length of space in x-axis bounds
	 */
	public void displaceLeft(int boundsWidth) {
		int width = getWidth();
		if (boundsWidth < width)
			xDisplacement = Math.max(xDisplacement - MinicraftImage.boxWidth, boundsWidth - width);
	}

	/**
	 * Displaces the entry to the right in rendering, with input left.
	 * @param boundsWidth the length of space in x-axis bounds
	 */
	public void displaceRight(int boundsWidth) {
		int width = getWidth();
		if (boundsWidth < width)
			xDisplacement = Math.min(xDisplacement + MinicraftImage.boxWidth, 0);
	}

	/**
	 * Calculates the rendering displacement of the entry in the menu.
	 * @return the x displacement.
	 */
	public int getXDisplacement() {
		return xDisplacement;
	}

	/**
	 * Resets the rendering x displacement to the initial value.
	 * @return this instance itself
	 * @see #getXDisplacement()
	 */
	public ListEntry resetXDisplacement() {
		xDisplacement = 0;
		return this;
	}

	/**
	 * Returns whether to hide the entry content when the entry exceeds the rendering bounds of the menu.
	 * If the overflowed content is hidden, the cursor is not displaced the part is not rendered if available.
	 * @return {@code true} to hide the overflowed parts
	 */
	public boolean hideWhenOverflow() {
		return false;
	}

	/**
	 * Determines if this entry can be selected.
	 *
	 * @return true if it is visible and can be selected, false otherwise.
	 */
	public final boolean isSelectable() {
		return selectable && visible;
	}

	/**
	 * Returns whether the entry is visible or not.
	 *
	 * @return true if the entry is visible, false otherwise
	 */
	public final boolean isVisible() {
		return visible;
	}

	/**
	 * Changes if the entry can be selected or not.
	 *
	 * @param selectable true if the entry can be selected, false if not
	 */
	public final void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	/**
	 * Changes if the entry is visible or not.
	 *
	 * @param visible true if the entry should be visible, false if not
	 */
	public final void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public abstract String toString();
}

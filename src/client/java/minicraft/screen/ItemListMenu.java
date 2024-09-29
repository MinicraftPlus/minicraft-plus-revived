package minicraft.screen;

import minicraft.core.io.Localization;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ItemEntry;
import minicraft.util.DisplayString;
import org.intellij.lang.annotations.MagicConstant;

class ItemListMenu extends Menu {
	public static final int POS_LEFT = 0;
	public static final int POS_RIGHT = 1;

	private static final int PADDING = 10;
	private static final int DISPLAY_LENGTH = 9;

	static Builder getBuilder() {
		return getBuilder(POS_LEFT);
	}

	static Builder getBuilder(@MagicConstant(intValues = { POS_LEFT, POS_RIGHT }) int slot) {
		Builder builder = new Builder(true, 0, RelPos.LEFT)
			.setDisplayLength(DISPLAY_LENGTH)
			.setSelectable(true)
			.setScrollPolicies(1, false)
			.setSearcherBar(true)
			.setShouldRenderSelectionLevel(true);
		switch (slot) { // Padding exists at the center between 2 slots.
			case ItemListMenu.POS_LEFT:
				builder.setPositioning(new Point(PADDING, PADDING), RelPos.BOTTOM_RIGHT)
					.setSize(Screen.w / 2 - PADDING - PADDING / 2, // Occupy left half
						(2 + DISPLAY_LENGTH) * MinicraftImage.boxWidth);
				break;
			case ItemListMenu.POS_RIGHT:
				builder.setPositioning(new Point(Screen.w - PADDING, PADDING), RelPos.BOTTOM_LEFT)
					.setSize(Screen.w / 2 - PADDING - PADDING / 2, // Occupy right half
						(2 + DISPLAY_LENGTH) * MinicraftImage.boxWidth);
				break;
			default:
				throw new IllegalArgumentException("for input slot: " + slot);
		}
		return builder;
	}

	protected ItemListMenu(Builder b, ItemEntry[] entries, DisplayString title) {
		super(b
			.setEntries(entries)
			.setTitle(title)
			.createMenu()
		);
	}

	protected ItemListMenu(ItemEntry[] entries, DisplayString title) {
		this(getBuilder(), entries, title);
	}
}

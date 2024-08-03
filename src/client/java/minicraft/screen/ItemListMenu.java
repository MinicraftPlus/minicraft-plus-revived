package minicraft.screen;

import minicraft.gfx.Point;
import minicraft.screen.entry.ItemEntry;

class ItemListMenu extends Menu {

	static Builder getBuilder() {
		return getBuilder(RelPos.LEFT);
	}

	static Builder getBuilder(RelPos entryPos) {
		return new Builder(true, 0, entryPos)
			.setPositioning(new Point(9, 9), RelPos.BOTTOM_RIGHT)
			.setDisplayLength(9)
			.setSelectable(true)
			.setScrollPolicies(1, false)
			.setSearcherBar(true);
	}

	protected ItemListMenu(Builder b, ItemEntry[] entries, String title) {
		super(b
			.setEntries(entries)
			.setTitle(title)
			.createMenu()
		);
	}

	protected ItemListMenu(ItemEntry[] entries, String title) {
		this(getBuilder(), entries, title);
	}
}

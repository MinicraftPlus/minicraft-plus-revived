package minicraft.screen.entry;

import minicraft.item.Item;

public class ItemListing extends ItemEntry {

	private String info;

	public ItemListing(Item i, String text) {
		super(i);
		setSelectable(false);
		this.info = text;
	}

	public void setText(String text) {
		info = text;
	}

	@Override
	public String toString() {
		return " " + info;
	}
}

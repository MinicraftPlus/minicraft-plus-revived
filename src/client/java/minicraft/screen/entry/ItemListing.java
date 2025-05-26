package minicraft.screen.entry;

import minicraft.item.Item;
import minicraft.item.ItemStack;

public class ItemListing extends ItemEntry {

	private String info;

	public ItemListing(ItemStack i, String text) {
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

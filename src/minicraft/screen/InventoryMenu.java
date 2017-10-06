package minicraft.screen;

import java.util.List;

import minicraft.entity.Inventory;
import minicraft.item.Item;
import minicraft.screen.entry.ItemEntry;

public class InventoryMenu extends Display {
	
	private static ItemEntry[] getEntries(Inventory inv) {
		List<Item> items = inv.getItems();
		ItemEntry[] entries = new ItemEntry[items.size()];
		// to make space for the item icon.
		for(int i = 0; i < items.size(); i++) {
			entries[i] = new ItemEntry(items.get(i));
		}
		
		return entries;
	}
	
	public InventoryMenu(Inventory inv) {
		//super(data, 9, 1, frames);
		super(new Menu.Builder(0, getEntries(inv))
			.setAnchor(9, 9)
			.setCentering(RelPos.BOTTOM_RIGHT, RelPos.LEFT)
			.setScrollPolicies(9, 1, false)
			.setFrame(true)
			.createMenu()
		);
	}
	
	/* this should take control of:
	 * 
	 * - frames, but only defaults
	 * - allow extra rendering (done through render)
	 * 
	 */
}

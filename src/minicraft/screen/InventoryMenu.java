package minicraft.screen;

import java.util.List;

import minicraft.entity.Inventory;
import minicraft.gfx.Point;
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
		super(new Menu.Builder(true, 0, RelPos.LEFT, getEntries(inv))
			.setPositioning(new Point(9, 9), RelPos.BOTTOM_RIGHT)
			.setDisplayLength(9)
			.setScrollPolicies(1, false)
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

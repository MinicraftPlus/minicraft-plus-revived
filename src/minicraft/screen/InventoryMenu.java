package minicraft.screen;

import java.util.ArrayList;
import java.util.List;
import minicraft.entity.Inventory;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.StackableItem;

public class InventoryMenu extends ScrollingMenu {
	protected Inventory inv;
	protected String title;
	
	private static final List<String> getItemList(Inventory inv) {
		List<Item> items = inv.getItems();
		List<String> itemNames = new ArrayList<String>();
		// to make space for the item icon.
		for(int i = 0; i < items.size(); i++) {
			itemNames.add(getItemDisplayName(items.get(i)));
		}
		
		return itemNames;
	}
	
	public InventoryMenu(Inventory inv, String title) {
		super(getItemList(inv), 9, 2*8, 2*8, 0, Color.get(-1, 555), Color.get(-1, 555));
		this.inv = inv;
		this.title = title;
	}
	
	public void render(Screen screen) {
		renderFrame(screen, title, 1, 1, 22, 11); // renders the blue box for the inventory
		super.render(screen);
		List<Item> items = inv.getItems();
		for(int i = 0; i < dispSize; i++)
			items.get(offset+i).sprite.render(screen, 2*8, 8*(2+i));
	}
	
	public void removeSelectedItem() {
		inv.remove(selected);
		if(selected >= inv.invSize())
			selected = Math.max(0, inv.invSize()-1); // can't select -1...
		options = getItemList(inv);
	}
	
	/// updates the name of the item, in case it's changed due to stack size, or similar.
	public void updateSelectedItem() {
		options.set(selected, getItemDisplayName(inv.get(selected)));
	}
	
	private static final String getItemDisplayName(Item i) {
		String extra = "";
		if(i instanceof StackableItem) {
			StackableItem stack = (StackableItem) i;
			extra = (stack.count > 999 ? 999 : stack.count) + " ";
		}
		
		return " " + extra + i.name;
	}
}

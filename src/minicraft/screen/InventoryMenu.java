package minicraft.screen;

import java.util.List;
import minicraft.entity.Inventory;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.StackableItem;

public class InventoryMenu extends ScrollingMenu {
	protected Inventory inv;
	protected String title;
	
	public InventoryMenu(Inventory inv, String title) {
		super(inv.getItemNames(), 9, 2*8, 2*8, 0, Color.get(-1, 555), Color.get(-1, 555));
		this.inv = inv;
		this.title = title;
		
		// to make space for the item icon.
		for(int i = 0; i < options.size(); i++) {
			String extra = "";
			if(inv.get(i) instanceof StackableItem) {
				StackableItem stack = (StackableItem) inv.get(i);
				extra = (stack.count > 999 ? 999 : stack.count) + " ";
			}
			options.set(i, " "+extra+options.get(i));
		}
	}
	
	public void render(Screen screen) {
		renderFrame(screen, title, 1, 1, 22, 11); // renders the blue box for the inventory
		super.render(screen);
		List<Item> items = inv.getItems();
		for(int i = 0; i < dispSize; i++)
			items.get(offset+i).sprite.render(screen, 2*8, 8*(2+i));
	}
}

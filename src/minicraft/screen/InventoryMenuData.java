package minicraft.screen;

import java.awt.Point;
import java.util.List;

import minicraft.InputHandler;
import minicraft.entity.Inventory;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.screen.entry.ItemEntry;
import minicraft.screen.entry.ListEntry;

public class InventoryMenuData implements MenuData {
	
	private Inventory inv;
	private Frame[] frames;
	
	protected static ItemEntry[] getItemList(List<Item> items) {
		ItemEntry[] itemNames = new ItemEntry[items.size()];
		// to make space for the item icon.
		for(int i = 0; i < items.size(); i++) {
			itemNames[i] = new ItemEntry(items.get(i));
		}
		
		return itemNames;
	}
	
	
	public InventoryMenuData(Inventory inv, Frame... frames) {
		this.inv = inv;
		this.frames = frames;
	}
	
	public InventoryMenuData(Inventory inv, String title) {
		this(inv, new Frame(title, new Rectangle(1, 1, 22, 11, Rectangle.CORNERS)));
	}
	
	
	@Override
	public Menu getMenu() {
		return new ScrollingMenu(this, true, 9, 0, frames);
	}
	
	@Override
	public ListEntry[] getEntries() {
		return getItemList(inv.getItems());
	}
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public void render(Screen screen) {}
	
	@Override
	public Centering getCentering() {
		return Centering.make(new Point(9, 9), RelPos.BOTTOM_RIGHT, RelPos.LEFT);
	}
	
	@Override
	public int getSpacing() {
		return 0;
	}
}

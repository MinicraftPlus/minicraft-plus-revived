package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.screen.Menu;

public class ItemEntry implements ListEntry {
	
	private Item item;
	
	public ItemEntry(Item i) {
		this.item = i;
	}
	
	@Override
	public void tick(InputHandler input, Menu menu) {}
	
	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		ListEntry.super.render(screen, x, y, true);
		item.sprite.render(screen, x, y);
	}
	
	@Override
	public String toString() {
		return item.getDisplayName();
	}
}

package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.item.Item;

public class ItemEntry implements ListEntry {
	
	private Item item;
	
	public ItemEntry(Item i) {
		this.item = i;
	}
	
	public Item getItem() { return item; }
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		ListEntry.super.render(screen, x, y, true);
		item.sprite.render(screen, x+SpriteSheet.boxWidth, y);
	}
	
	// if you add to the length of the string, and therefore the width of the entry, then it will actually move the entry RIGHT in the inventory, instead of the intended left, because it is auto-positioned to the left side.
	// However, one space is necessary, for the icon.
	@Override
	public String toString() {
		return " "+item.getDisplayName();
	}
}

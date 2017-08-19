package minicraft.screen.entry;

import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.item.Item;

public class ItemEntry extends ListEntry {
	
	private Item item;
	
	public ItemEntry(Item i) {
		this.item = i;
	}
	
	@Override
	public void render(Screen screen, FontStyle style) {
		style.draw(item.getDisplayName(), screen);
		item.sprite.render(screen, style.getXPos(), style.getYPos());
	}
}

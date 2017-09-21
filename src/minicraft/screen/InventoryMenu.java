package minicraft.screen;

import java.util.List;

import minicraft.Game;
import minicraft.entity.Inventory;
import minicraft.entity.ItemEntity;
import minicraft.gfx.*;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.screen.entry.ItemEntry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class InventoryMenu extends ScrollingMenu {
	protected Inventory inv;
	//protected String title;
	
	protected static ItemEntry[] getItemList(Inventory inv) {
		List<Item> items = inv.getItems();
		ItemEntry[] itemNames = new ItemEntry[items.size()];
		// to make space for the item icon.
		for(int i = 0; i < items.size(); i++) {
			itemNames[i] = new ItemEntry(items.get(i));
		}
		
		return itemNames;
	}
	
	public InventoryMenu(Inventory inv, String title) {
		super(getItemList(inv), 9);
		//super(getItemList(inv), 9, 2*8, 2*8, 0, Color.get(-1, 555), Color.get(-1, 555));
		setFrames(new Frame(title, new Rectangle(1, 1, 22, 11, Rectangle.CORNERS)));
		setTextStyle(new FontStyle(Color.get(-1, 555)).setXPos(2* SpriteSheet.boxWidth));
		
		this.inv = inv;
	}
	
	/*public void render(Screen screen) {
		super.render(screen);
		List<Item> items = inv.getItems();
		for(int i = 0; i < dispSize; i++)
			items.get(offset+i).sprite.render(screen,, 8*(2+i));
	}*/
	
	/*public void renderLine(Screen screen, FontStyle style, int lineIndex) {
		super.renderLine(screen, style, lineIndex);
		inv.get(offset+lineIndex).sprite.render(screen, style.getXPos(), 8*(2+lineIndex));
	}*/
	
	protected boolean isHighlighted() {
		return true;
	}
	
	public void removeSelectedItem() {
		if(inv.invSize() > 0)
			inv.remove(selected);
		if(selected >= inv.invSize()) {
			int newselected = Math.max(0, inv.invSize() - 1); // can't select -1...
			dispSelected += newselected - selected;
			selected = newselected;
			//if(Game.debug) System.out.println("new selection: " + selected);
		}
		options = getItemList(inv);
	}
	
	/// updates the name of the item, in case it's changed due to stack size, or similar.
	public void updateSelectedItem() {
		options[selected] = new ItemEntry(inv.get(selected));
	}
	
	public void onInvUpdate(Inventory inv) {
		if(inv == this.inv)
			options = getItemList(inv);
	}
	
	/*
	@NotNull
	@Contract(pure = true)
	private static String getItemDisplayName(Item i) {
		String extra = "";
		if(i instanceof StackableItem) {
			StackableItem stack = (StackableItem) i;
			extra = (stack.count > 999 ? 999 : stack.count) + " ";
		}
		
		return " " + extra + i.name;
	}*/
}

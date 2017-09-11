package minicraft.screen;

import java.util.ArrayList;
import java.util.List;

import minicraft.Game;
import minicraft.entity.Inventory;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class InventoryMenu extends ScrollingMenu {
	protected Inventory inv;
	protected String title;
	
	private static List<String> getItemList(Inventory inv) {
		List<Item> items = inv.getItems();
		List<String> itemNames = new ArrayList<>();
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
		List<Item> items = inv.getItems();
		if(options.size() != items.size())
			options = getItemList(inv);
		super.render(screen);
		for(int i = 0; i < dispSize && i < items.size()-offset; i++)
			items.get(offset+i).sprite.render(screen, 2*8, 8*(2+i));
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
		options.set(selected, getItemDisplayName(inv.get(selected)));
	}
	
	@NotNull
	@Contract(pure = true)
	private static String getItemDisplayName(Item i) {
		String extra = "";
		if(i instanceof StackableItem) {
			StackableItem stack = (StackableItem) i;
			extra = (stack.count > 999 ? 999 : stack.count) + " ";
		}
		
		return " " + extra + i.name;
	}
}

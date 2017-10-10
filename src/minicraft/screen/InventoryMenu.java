package minicraft.screen;

import java.util.List;

import minicraft.gfx.Point;
import minicraft.item.Item;
import minicraft.item.Recipe;
import minicraft.screen.entry.ItemEntry;
import minicraft.screen.entry.RecipeEntry;

// TODO Perhaps I could make it "return" an item when it gets "selected" or "chosen", since that's really all it does... then I wouldn't have to listen for key presses in the Display class, I could just check if an Item has been "chosen", and then act when it has.

public class InventoryMenu extends Menu {
	
	private static ItemEntry[] getEntries(List<Item> items) {
		ItemEntry[] entries = new ItemEntry[items.size()];
		// to make space for the item icon.
		for(int i = 0; i < items.size(); i++) {
			entries[i] = new ItemEntry(items.get(i));
		}
		
		return entries;
	}
	
	private static RecipeEntry[] getRecipeEntries(List<Recipe> recipes) {
		RecipeEntry[] entries = new RecipeEntry[recipes.size()];
		for(int i = 0; i < recipes.size(); i++) {
			entries[i] = new RecipeEntry(recipes.get(i));
		}
		
		return entries;
	}
	
	private static Builder getBuilder() {
		return new Menu.Builder(true, 0, RelPos.LEFT)
			.setPositioning(new Point(9, 9), RelPos.BOTTOM_RIGHT)
			.setDisplayLength(9)
			.setTitle("Inventory")
			.setScrollPolicies(1, false);
	}
	
	public InventoryMenu(List<Item> items) {
		//super(data, 9, 1, frames);
		super(getBuilder()
			.setEntries(getEntries(items))
			.createMenu()
		);
	}
	
	public InventoryMenu(List<Item> items, String title) {
		//super(data, 9, 1, frames);
		super(getBuilder()
			.setEntries(getEntries(items))
			//.setPositioning(new Point(x, y), RelPos.BOTTOM_RIGHT)
			.setTitle(title)
			.createMenu()
		);
	}
	
	public InventoryMenu(List<Recipe> recipes, int fillCol, int edgeStrokeCol, int edgeFillCol) {
		//super(data, 9, 1, frames);
		super(getBuilder()
			.setEntries(getRecipeEntries(recipes))
			.setFrame(fillCol, edgeStrokeCol, edgeFillCol)
			.createMenu()
		);
	}
	
	public Item getSelectedItem() {
		return ((ItemEntry)getCurEntry()).getItem();
	}
}

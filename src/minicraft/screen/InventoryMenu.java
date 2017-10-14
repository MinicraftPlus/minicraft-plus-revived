package minicraft.screen;

import java.util.List;

import minicraft.entity.Player;
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
	
	private static RecipeEntry[] getRecipeEntries(Recipe[] recipes) {
		RecipeEntry[] entries = new RecipeEntry[recipes.length];
		for(int i = 0; i < recipes.length; i++) {
			entries[i] = new RecipeEntry(recipes[i]);
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
	
	private Recipe[] recipes = null;
	
	public InventoryMenu(Recipe[] recipes, String title) {
		super(getBuilder()
			.setEntries(getRecipeEntries(recipes))
			.setTitle(title)
			.createMenu()
		);
		this.recipes = recipes;
	}
	public InventoryMenu(Recipe[] recipes, int fillCol, int edgeStrokeCol, int edgeFillCol) {
		//super(data, 9, 1, frames);
		super(getBuilder()
			.setEntries(getRecipeEntries(recipes))
			.setTitle("Crafting")
			.setFrame(fillCol, edgeStrokeCol, edgeFillCol)
			.createMenu()
		);
		this.recipes = recipes;
	}
	
	public Item getSelectedItem() {
		return ((ItemEntry)getCurEntry()).getItem();
	}
	
	void refreshCanCraft(Player player) {
		if(recipes != null) {
			for (Recipe r: recipes) {
				r.checkCanCraft(player);
			}
		}
	}
}

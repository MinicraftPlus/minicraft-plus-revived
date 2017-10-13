package minicraft.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import minicraft.InputHandler;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.screen.entry.ItemListing;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.RecipeEntry;

public class CraftingMenu extends Display {
	
	private Player player;
	private Recipe[] recipes;
	
	private InventoryMenu recipeMenu;
	private Menu.Builder itemCountMenu, costsMenu;
	
	private static RecipeEntry[] getRecipeList(Recipe[] recipes) {
		RecipeEntry[] entries = new RecipeEntry[recipes.length];
		
		for(int i = 0; i < recipes.length; i++) {
			entries[i] = new RecipeEntry(recipes[i]);
		}
		
		return entries;
	}
	
	
	public CraftingMenu(List<Recipe> recipes, String title, Player player) {
		
		this.player = player;
		this.recipes = recipes.toArray(new Recipe[recipes.size()]);
		recipeMenu = new InventoryMenu(this.recipes);
		
		ItemListing itemCount = new ItemListing(this.recipes[0].getProduct(), "0");
		
		itemCountMenu = new Menu.Builder(true, 0, RelPos.LEFT, itemCount)
			.setTitle("Have:")
			.setPositioning(new Point(recipeMenu.getBounds().getRight()+SpriteSheet.boxWidth, recipeMenu.getBounds().getTop()), RelPos.BOTTOM_RIGHT);
		
		costsMenu = new Menu.Builder(true, 0, RelPos.LEFT, new ListEntry[0])
			.setPositioning(new Point(itemCountMenu.createMenu().getBounds().getLeft(), recipeMenu.getBounds().getBottom()), RelPos.TOP_RIGHT);
		
		menus = new Menu[] {recipeMenu, itemCountMenu.createMenu(), costsMenu.createMenu()};
		
		refreshData();
	}
	
	private void refreshData() {
		menus[2] = costsMenu
			.setEntries(getCurItemCosts())
			.createMenu();
		
		menus[1].updateSelectedEntry(new ItemListing(recipes[menus[0].getSelection()].getProduct(), String.valueOf(getCurItemCount())));
		
		recipeMenu.refreshCanCraft(player);
	}
	
	/*@Override
	public Menu getMenu() {
		return new ScrollingMenu(this, 9, 1, new Frame("Crafting", new Rectangle(1, 1, 22, 11, Rectangle.CORNERS)));
	}*/
	
	private int getCurItemCount() {
		return player.inventory.count(recipes[menus[0].getSelection()].getProduct());
	}
	
	private ItemListing[] getCurItemCosts() {
		ArrayList<ItemListing> costList = new ArrayList<>();
		HashMap<String, Integer> costMap = recipes[menus[0].getSelection()].costs;
		for(String itemName: costMap.keySet()) {
			Item cost = Items.get(itemName);
			costList.add(new ItemListing(cost, costMap.get(itemName)+"/"+player.inventory.count(cost)));
		}
		
		return costList.toArray(new ItemListing[costList.size()]);
	}
	
	//@Override
	public ListEntry[] getEntries() {
		return getRecipeList(recipes);
	}
	
	@Override
	public void tick(InputHandler input) {
		if(input.getKey("select").clicked && menus[selection].getSelection() >= 0) {
			// check the selected recipe
			Recipe r = recipes[menus[selection].getSelection()];
			if(r.canCraft) {
				r.craft(player);
				
				for(Recipe recipe: recipes)
					recipe.checkCanCraft(player);
			}
		}
	}
	
	@Override
	public void render(Screen screen) {
		int index = menus[selection].getSelection();
		if(index < 0) return;
		Recipe recipe = recipes[index];
		//int hasResultItems = player.inventory.count(recipe.getProduct()); // Counts the number of items to see if yo
		int xo = 16 * 9; // x coordinate of the items in the 'have' and 'cost' windows
		recipe.getProduct().sprite.render(screen, xo, 2 * 8); // Renders the sprite in the 'have' window
		Font.draw("" + player.inventory.count(recipe.getProduct()), screen, xo + SpriteSheet.boxWidth, 2 * 8, Color.get(1, 555)); // draws the a
		
		int yo = 5 * 8; // y coordinate of the cost item
		for (String costname : recipe.costs.keySet().toArray(new String[0])) {
			Item cost = Items.get(costname);
			if (cost == null) continue;
			cost.sprite.render(screen, xo, yo); // renders the cost item in the 'cost' window
			
			int has = player.inventory.count(cost); // This is the amount of the item you have in your inventory
			if (has > 99) has = 99; // display 99 max (for space)
			int reqAmt = recipe.costs.get(costname);
			int color = has < reqAmt ? Color.get(1, 222) : Color.get(1, 555); // color in the 'cost' window
			Font.draw(reqAmt + "/" + has, screen, xo + 8, yo, color); // Draw "#required/#has" text next to the ic
			yo += Font.textHeight();
		}
		
	}
	
	/*@Override
	public Centering getCentering() {
		return Centering.make(new Point(9, 9), RelPos.BOTTOM_RIGHT, RelPos.LEFT);
	}
	
	@Override
	public int getSpacing() {
		return 0;
	}*/
}

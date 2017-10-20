package minicraft.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.Player;
import minicraft.gfx.Point;
import minicraft.gfx.SpriteSheet;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.screen.entry.ItemListing;
import minicraft.screen.entry.RecipeEntry;

public class CraftingMenu extends Display {
	
	private Player player;
	private Recipe[] recipes;
	
	private RecipeMenu recipeMenu;
	private Menu.Builder itemCountMenu, costsMenu;
	
	private static RecipeEntry[] getRecipeList(Recipe[] recipes) {
		RecipeEntry[] entries = new RecipeEntry[recipes.length];
		
		for(int i = 0; i < recipes.length; i++) {
			entries[i] = new RecipeEntry(recipes[i]);
		}
		
		return entries;
	}
	
	
	/*public CraftingMenu(List<Recipe> recipes, String title, Player player) {
		this(recipes, title, player, false);
	}
	*/public CraftingMenu(List<Recipe> recipes, String title, Player player/*, boolean isPersonalMenu*/) {
		for(Recipe recipe: recipes)
			recipe.checkCanCraft(player);
		
		recipeMenu = new RecipeMenu(recipes, title);
		
		this.player = player;
		this.recipes = recipes.toArray(new Recipe[recipes.size()]);
		
		//ItemListing itemCount = new ItemListing(this.recipes[0].getProduct(), "0");
		
		itemCountMenu = new Menu.Builder(true, 0, RelPos.LEFT)
			.setTitle("Have:")
			.setTitlePos(RelPos.TOP_LEFT)
			.setPositioning(new Point(recipeMenu.getBounds().getRight()+SpriteSheet.boxWidth, recipeMenu.getBounds().getTop()), RelPos.BOTTOM_RIGHT);
		
		costsMenu = new Menu.Builder(true, 0, RelPos.LEFT)
			.setTitle("Cost:")
			.setTitlePos(RelPos.TOP_LEFT)
			.setPositioning(new Point(itemCountMenu.createMenu().getBounds().getLeft(), recipeMenu.getBounds().getBottom()), RelPos.TOP_RIGHT);
		
		menus = new Menu[] {recipeMenu, itemCountMenu.createMenu(), costsMenu.createMenu()};
		//menus = new Menu[3];
		//menus[0] = recipeMenu;
		
		refreshData();
	}
	
	private void refreshData() {
		Menu prev = menus[2];
		menus[2] = costsMenu
			.setEntries(getCurItemCosts())
			.createMenu();
		menus[2].setFrameColors(prev);
		
		menus[1] = itemCountMenu
			.setEntries(new ItemListing(recipes[recipeMenu.getSelection()].getProduct(), String.valueOf(getCurItemCount())))
			.createMenu();
		menus[1].setFrameColors(prev);
	}
	
	private int getCurItemCount() {
		return player.inventory.count(recipes[recipeMenu.getSelection()].getProduct());
	}
	
	private ItemListing[] getCurItemCosts() {
		ArrayList<ItemListing> costList = new ArrayList<>();
		HashMap<String, Integer> costMap = recipes[recipeMenu.getSelection()].getCosts();
		for(String itemName: costMap.keySet()) {
			Item cost = Items.get(itemName);
			costList.add(new ItemListing(cost, costMap.get(itemName)+"/"+player.inventory.count(cost)));
		}
		
		return costList.toArray(new ItemListing[costList.size()]);
	}
	
	@Override
	public void tick(InputHandler input) {
		if(input.getKey("menu").clicked) {
			Game.exitMenu();
			return;
		}
		
		int prevSel = recipeMenu.getSelection();
		super.tick(input);
		if(prevSel != recipeMenu.getSelection())
			refreshData();
		
		if((input.getKey("select").clicked || input.getKey("attack").clicked) && recipeMenu.getSelection() >= 0) {
			// check the selected recipe
			Recipe r = recipes[recipeMenu.getSelection()];
			if(r.getCanCraft()) {
				r.craft(player);
				
				refreshData();
				for(Recipe recipe: recipes)
					recipe.checkCanCraft(player);
			}
		}
	}
}

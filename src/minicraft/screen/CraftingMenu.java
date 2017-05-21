package minicraft.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Recipe;
import minicraft.sound.Sound;

public class CraftingMenu extends Menu {
	private Player player; // the player that opened this menu
	private int selected = 0; // current selected item

	private List<Recipe> recipes; // List of recipes used in this menu (workbench, anvil, oven, etc)

	public CraftingMenu(List<Recipe> recipes, Player player) {
		this.recipes = new ArrayList<Recipe>(recipes); // Assigns the recipes
		this.player = player;
		
		for (int i = 0; i < recipes.size(); i++) {
			this.recipes.get(i).checkCanCraft(player); // Checks if the player can craft the item(s)
		}
		
		/* This sorts the recipes so that the ones you can craft will appear on top */
		Collections.sort(this.recipes, new Comparator<Recipe>() {
				public int compare(Recipe r1, Recipe r2) {
					if (r1.canCraft && !r2.canCraft) return -1; // if the first item can be crafted while the second can't, the first one will go above in the list
					if (!r1.canCraft && r2.canCraft) return 1; // if the second item can be crafted while the first can't, the second will go over that one.
					return 0; // else don't change position
				}
		});
	}

	public void tick() {
		if (input.getKey("menu").clicked) game.setMenu(null); //menu exit condition
		
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;
		if (input.getKey("up").clicked) Sound.pickup.play();
		if (input.getKey("down").clicked) Sound.pickup.play();
		
		int len = recipes.size();
		if (len == 0) selected = 0;
		//wrap-around:
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		
		if (input.getKey("attack").clicked && len > 0) {
			Recipe r = recipes.get(selected); // The current recipe selected
			if(r.craft(player))
				for (int i = 0; i < recipes.size(); i++)
					recipes.get(i).checkCanCraft(player);// Refreshes the recipe list if the player can now craft a new item.
		}
	}

	public void render(Screen screen) {
		renderFrame(screen, "Have", 17, 1, 24, 3); // renders the 'have' items window
		renderFrame(screen, "Cost", 17, 4, 24, 11); // renders the 'cost' items window
		renderFrame(screen, "Crafting", 0, 1, 16, 11); // renders the main crafting window
		renderItemList(screen, 0, 1, 16, 11, recipes, selected); // renders all the items in the recipe list

		if (recipes.size() > 0) {
			Recipe recipe = recipes.get(selected);
			int hasResultItems = player.inventory.count(recipe.getProduct()); // Counts the number of items to see if you can craft the recipe
			int xo = 16 * 9; // x coordinate of the items in the 'have' and 'cost' windows
			recipe.getProduct().sprite.render(screen, xo, 2 * 8); // Renders the sprite in the 'have' window
			Font.draw("" + hasResultItems, screen, xo + 8, 2 * 8, Color.get(-1, 555)); // draws the amount in the 'have' menu
			
			int yo = 5 * 8; // y coordinate of the cost item
			for (Item cost: recipe.costs.keySet().toArray(new Item[0])) {
				cost.sprite.render(screen, xo, yo); // renders the cost item in the 'cost' window
				
				int has = player.inventory.count(cost); // This is the amount of the item you have in your inventory
				if (has > 99) has = 99; // display 99 max (for space)
				int reqAmt = recipe.costs.get(cost);
				int color = has < reqAmt ? Color.get(-1, 222) : Color.get(-1, 555); // color in the 'cost' window
				Font.draw(reqAmt + "/" + has, screen, xo + 8, yo, color); // Draw "#required/#has" text next to the icon
				yo += Font.textHeight();
			}
		}
	}
}

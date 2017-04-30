package minicraft.screen;

import minicraft.crafting.Recipe;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.ResourceItem;
import minicraft.sound.Sound;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
			r.checkCanCraft(player); // Checks if the player can craft this recipe
			if (r.canCraft) {
				r.deductCost(player);
				r.craft(player); // It will craft (add) the item into the player's inventory
				Sound.craft.play();
			}
			for (int i = 0; i < recipes.size(); i++) {
				recipes.get(i).checkCanCraft(player);// Refreshes the recipe list if the player can now craft a new item.
			}
		}
	}

	public void render(Screen screen) {
		renderFrame(screen, "Have", 17, 1, 24, 3); // renders the 'have' items window
		renderFrame(screen, "Cost", 17, 4, 24, 11); // renders the 'cost' items window
		renderFrame(screen, "Crafting", 0, 1, 16, 11); // renders the main crafting window
		renderItemList(screen, 0, 1, 16, 11, recipes, selected); // renders all the items in the recipe list

		if (recipes.size() > 0) {
			Recipe recipe = recipes.get(selected);
			int hasResultItems = player.inventory.count(recipe.resultTemplate); // Counts the number of items to see if you can craft the recipe
			int xo = 16 * 9; // x coordinate of the items in the 'have' and 'cost' windows
			screen.render(xo, 2 * 8, recipe.resultTemplate.getSprite(), recipe.resultTemplate.getColor(), 0); // Renders the sprites in the 'have' & 'cost' windows
			Font.draw("" + hasResultItems, screen, xo + 8, 2 * 8, Color.get(-1, 555, 555, 555)); // draws the amount in the 'have' menu
			
			List<Item> costs = recipe.costs;
			for (int i = 0; i < costs.size(); i++) {
				Item item = costs.get(i);
				int yo = (5 + i) * 8; // y coordinate of the cost item
				screen.render(xo, yo, item.getSprite(), item.getColor(), 0); // renders the cost item
				int requiredAmt = 1;
				if (item instanceof ResourceItem) {
					requiredAmt = ((ResourceItem) item).count;
				}
				int has = player.inventory.count(item); // This is the amount of the resource you have in your inventory
				int color = Color.get(-1, 555, 555, 555); // color in the 'cost' window
				if (has < requiredAmt) {
					color = Color.get(-1, 222, 222, 222); // change the color to gray.
				}
				if (has > 99) has = 99; //just display 99 (for space)
				Font.draw("" + requiredAmt + "/" + has, screen, xo + 8, yo, color); // Draw "#required/#has" text next to the icon
			}
		}
	}
}

package minicraft.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import minicraft.Sound;
import minicraft.entity.Player;
import minicraft.gfx.*;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.screen.entry.StringEntry;

public class CraftingMenu extends ScrollingMenu {
	private Player player; // the player that opened this menu
	private int selected = 0; // current selected item
	private boolean personal;
	
	private List<Recipe> recipes; // List of recipes used in this menu (workbench, anvil, oven, etc)
	
	private int invResultItemCount; // this stores how many of the item selected are in the player's inventory already. It changes whenever the currently selected item changes.
	
	private static String[] getRecipeList(List<Recipe> recipes) {
		String[] recipeNames = new String[recipes.size()];
		for(int i = 0; i < recipes.size(); i++) {
			Recipe r = recipes.get(i);
			if(r == null) continue;
			recipeNames[i] = r.getProduct().name+(r.amount > 1 ? " x"+r.amount : "");
		}
		
		return recipeNames;
	}
	
	public CraftingMenu(List<Recipe> recipes, Player player) {
		this(recipes, player, false);
	}
	public CraftingMenu(List<Recipe> recipes, Player player, boolean isPersonalFrame) {
		super(StringEntry.useStringArray(getRecipeList(recipes)));//, 9, Color.get(-1, 555), Color.get(-1, 222));
		setTextStyle(new FontStyle(Color.get(-1, 555)).setYPos(2*SpriteSheet.boxWidth).setXPos(2*SpriteSheet.boxWidth));
		Frame[] frames = new Frame[] {
			(new Frame("Have", new Rectangle(17, 1, 24, 3, Rectangle.CORNERS))), // renders the 'have' items window
			(new Frame("Cost", new Rectangle(17, 4, 24, 11, Rectangle.CORNERS))), // renders the 'cost' items window
			(new Frame("Crafting", new Rectangle(0, 1, 16, 11, Rectangle.CORNERS))) // renders the main crafting window
		};
		setFrames(frames);
		if(isPersonalFrame)
			setFrameColors(Color.get(300, 300, 300, 555), Color.get(300, 300), Color.get(-1, 1, 300, 400));
		// else, it's the default color.
		
		this.recipes = new ArrayList<Recipe>(recipes); // Assigns the recipes
		this.player = player;
		personal = isPersonalFrame;
		
		for (int i = 0; i < recipes.size(); i++) {
			this.recipes.get(i).checkCanCraft(player); // Checks if the player can craft the item(s)
		}
		
		/* This sorts the recipes so that the ones you can craft will appear on top */
		this.recipes.sort((r1, r2) -> {
			if (r1.canCraft && !r2.canCraft)
				return -1; // if the first item can be crafted while the second can't, the first one will go above in the list
			if (!r1.canCraft && r2.canCraft) return 1; // if the second item can be crafted while the first can't, the second will go over that one.
			return 0; // else don't change position
		});
	}

	public void tick() {
		if (input.getKey("menu").clicked || personal && input.getKey("craft").clicked) {
			Game.setMenu(null); //menu exit condition
			return;
		}
		
		super.tick();
		
		/*if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;
		if (input.getKey("up").clicked) Sound.pickup.play();
		if (input.getKey("down").clicked) Sound.pickup.play();
		
		int len = recipes.size();
		if (len == 0) selected = 0;
		//wrap-around:
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		*/
		if (input.getKey("attack").clicked && recipes.size() > 0) {
			Recipe r = recipes.get(selected); // The current recipe selected
			if(r.craft(player)) {
				for (int i = 0; i < recipes.size(); i++) {
					recipes.get(i).checkCanCraft(player); // Refreshes the recipe list if the player can now craft a new item.
					invResultItemCount += recipes.get(i).amount; // a bit of a cheat-y shortcut to avoid re-caculating the new amount. :P NOTE: this will fail if, for whatever reason, the result item is one of the costs. For example, repairing a tool, in the future.
				}
			}
		}
	}
	
	protected void onSelectionChange(int prevSel, int newSel) {
		invResultItemCount = player.inventory.count(recipes.get(newSel).getProduct());
	}
	
	protected boolean isHighlighted(int idx) {
		return recipes.get(idx).canCraft;
	}
	
	public void render(Screen screen) {
		/*renderFrame(screen, "Have", 17, 1, 24, 3); // renders the 'have' items window
		renderFrame(screen, "Cost", 17, 4, 24, 11); // renders the 'cost' items window
		renderFrame(screen, "Crafting", 0, 1, 16, 11); // renders the main crafting window
		renderItemList(screen, 0, 1, 16, 11, recipes, selected); // renders all the items in the recipe list
		*/
		super.render(screen); // renders the text
		// now, render the item sprites.
		for(int i = 0; i < recipes.size(); i++)
			recipes.get(i).getProduct().sprite.render(screen, 0, SpriteSheet.boxWidth*(i+2));
		
		if (recipes.size() > 0) {
			Recipe recipe = recipes.get(selected);
			//int hasResultItems = player.inventory.count(recipe.getProduct()); // Counts the number of items to see if you can craft the recipe
			int xo = 16 * 9; // x coordinate of the items in the 'have' and 'cost' windows
			recipe.getProduct().sprite.render(screen, xo, 2 * 8); // Renders the sprite in the 'have' window
			Font.draw(""+invResultItemCount, screen, xo + SpriteSheet.boxWidth, 2 * 8, Color.get(-1, 555)); // draws the amount in the 'have' menu
			
			int yo = 5 * 8; // y coordinate of the cost item
			for (String costname: recipe.costs.keySet().toArray(new String[0])) {
				Item cost = Items.get(costname);
				if(cost == null) continue;
				cost.sprite.render(screen, xo, yo); // renders the cost item in the 'cost' window
				
				int has = player.inventory.count(cost); // This is the amount of the item you have in your inventory
				if (has > 99) has = 99; // display 99 max (for space)
				int reqAmt = recipe.costs.get(costname);
				int color = has < reqAmt ? Color.get(-1, 222) : Color.get(-1, 555); // color in the 'cost' window
				Font.draw(reqAmt + "/" + has, screen, xo + 8, yo, color); // Draw "#required/#has" text next to the icon
				yo += Font.textHeight();
			}
		}
	}
	
	/*protected void renderFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		if(!personal) super.renderFrame(screen, title, x0, y0, x1, y1);
		else renderMenuFrame(screen, title, x0, y0, x1, y1, , , );
	}*/
}

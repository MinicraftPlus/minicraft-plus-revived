package minicraft.screen;

import java.awt.Point;

import minicraft.InputHandler;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.RecipeEntry;

public class CraftingMenu extends Display {
	
	private Player player;
	private Recipe[] recipes;
	
	private static RecipeEntry[] getRecipeList(Recipe[] recipes) {
		RecipeEntry[] entries = new RecipeEntry[recipes.length];
		
		for(int i = 0; i < recipes.length; i++) {
			entries[i] = new RecipeEntry(recipes[i]);
		}
		
		return entries;
	}
	
	
	public CraftingMenu(Recipe[] recipes, Player player) {
		this.recipes = recipes;
		this.player = player;
	}
	
	@Override
	public Menu getMenu() {
		return new ScrollingMenu(this, 9, 1, new Frame("Crafting", new Rectangle(1, 1, 22, 11, Rectangle.CORNERS)));
	}
	
	@Override
	public ListEntry[] getEntries() {
		return getRecipeList(recipes);
	}
	
	@Override
	public void tick(InputHandler input) {
		if(input.getKey("select").clicked && getMenu().getSelection() >= 0) {
			// check the selected recipe
			Recipe r = recipes[getMenu().getSelection()];
			if(r.canCraft) {
				r.craft(player);
				
				for(Recipe recipe: recipes)
					recipe.checkCanCraft(player);
			}
		}
	}
	
	@Override
	public void render(Screen screen) {
		int index = getMenu().getSelection();
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
	
	@Override
	public Centering getCentering() {
		return Centering.make(new Point(9, 9), RelPos.BOTTOM_RIGHT, RelPos.LEFT);
	}
	
	@Override
	public int getSpacing() {
		return 0;
	}
}

package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.item.Recipe;

public class RecipeEntry implements ListEntry {
	
	private Recipe recipe;
	
	
	public RecipeEntry(Recipe r) {
		this.recipe = r;
	}
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		ListEntry.super.render(screen, x, y, recipe.canCraft);
		recipe.getProduct().sprite.render(screen, x, y);
	}
	
	@Override
	public String toString() {
		return recipe.getProduct().name + (recipe.amount > 1 ? " x" + recipe.amount : "");
	}
}

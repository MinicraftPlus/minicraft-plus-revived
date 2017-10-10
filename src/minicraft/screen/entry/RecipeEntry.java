package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.item.Recipe;

public class RecipeEntry extends ItemEntry {
	
	private Recipe recipe;
	
	public RecipeEntry(Recipe r) {
		super(r.getProduct());
		this.recipe = r;
	}
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		super.render(screen, x, y, recipe.canCraft);
	}
	
	@Override
	public String toString() {
		return super.toString() + (recipe.amount > 1 ? " x" + recipe.amount : "");
	}
}

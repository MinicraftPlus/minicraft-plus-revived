package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.Font;
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
		if(isVisible()) {
			Font.draw(toString(), screen, x, y, recipe.getCanCraft() ? COL_SLCT : COL_UNSLCT);
			getItem().sprite.render(screen, x, y);
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + (recipe.getAmount() > 1 ? " x" + recipe.getAmount() : "");
	}
}

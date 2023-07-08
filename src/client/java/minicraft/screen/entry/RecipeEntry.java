package minicraft.screen.entry;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Recipe;

import java.util.List;

public class RecipeEntry extends ItemEntry {

	public static RecipeEntry[] useRecipes(List<Recipe> recipes) {
		RecipeEntry[] entries = new RecipeEntry[recipes.size()];
		for (int i = 0; i < recipes.size(); i++)
			entries[i] = new RecipeEntry(recipes.get(i));
		return entries;
	}

	private final Recipe recipe;

	public RecipeEntry(Recipe r) {
		super(r.getProduct());
		this.recipe = r;
	}

	@Override
	public void tick(InputHandler input) {}

	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {
		if (isVisible()) {
			Font.draw(toString(), screen, x, y, recipe.getCanCraft() ? COL_SLCT : COL_UNSLCT);
			screen.render(x, y, getItem().sprite);
		}
	}

	@Override
	public int getColor(boolean isSelected) {
		return recipe.checkCanCraft(Game.player) ? super.getColor(isSelected) :
			isSelected ? Color.tint(Color.RED, 1, true) : Color.RED;
	}

	@Override
	public String toString() {
		return super.toString() + (recipe.getAmount() > 1 ? " x" + recipe.getAmount() : "");
	}
}

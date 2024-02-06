package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeEntry extends ItemEntry {

	public static RecipeEntry[] useRecipes(List<Recipe> recipes) {
		RecipeEntry[] entries = new RecipeEntry[recipes.size()];
		for (int i = 0; i < recipes.size(); i++)
			entries[i] = new RecipeEntry(recipes.get(i));
		return entries;
	}

	private Recipe recipe;

	public RecipeEntry(Recipe r) {
		super(r.getProduct());
		this.recipe = r;
	}

	@Override
	public void tick(InputHandler input) {
	}

	@Override
	public void render(Screen screen, int x, int y, boolean isSelected, @Nullable IntRange bounds) {
		if (isVisible()) {
			Font.draw(toString(), screen, x + 16, y, recipe.getCanCraft() ? COL_SLCT : COL_UNSLCT, bounds);
			screen.render(x, y, getItem().sprite);
		}
	}

	@Override
	public String toString() {
		return super.toString() + (recipe.getAmount() > 1 ? " x" + recipe.getAmount() : "");
	}
}

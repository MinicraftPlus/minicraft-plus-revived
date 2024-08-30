package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
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
	public void render(Screen screen, @Nullable Screen.RenderingLimitingModel limitingModel, int x, int y, boolean isSelected) {
		if (isVisible()) {
			Font.draw(limitingModel, toString(), screen, x, y, recipe.getCanCraft() ? COL_SLCT : COL_UNSLCT);
		}
	}

	@Override
	public String toString() {
		return recipe.getAmount() > 1 ? Localization.getLocalized("minicraft.display.inventory.recipe_entry_multi",
			super.toString(), recipe.getAmount()) : super.toString();
	}
}

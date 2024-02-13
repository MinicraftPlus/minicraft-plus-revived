package minicraft.screen;

import minicraft.entity.mob.Player;
import minicraft.item.Recipe;
import minicraft.screen.entry.RecipeEntry;

import java.util.List;

class RecipeMenu extends ItemListMenu {

	private static RecipeEntry[] getAndSortRecipes(List<Recipe> recipes, Player player) {
		recipes.sort((r1, r2) -> {
			boolean craft1 = r1.checkCanCraft(player);
			boolean craft2 = r2.checkCanCraft(player);
			if (craft1 == craft2)
				return 0;
			if (craft1) return -1;
			if (craft2) return 1;

			return 0; // should never actually be reached
		});

		return RecipeEntry.useRecipes(recipes);
	}

	protected RecipeMenu(List<Recipe> recipes, String title, Player player) {
		super(getAndSortRecipes(recipes, player), title);
	}
}

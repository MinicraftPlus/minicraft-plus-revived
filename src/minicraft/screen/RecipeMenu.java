package minicraft.screen;

import java.util.List;

import minicraft.item.Recipe;
import minicraft.screen.entry.RecipeEntry;

public class RecipeMenu extends ItemListMenu {
	public RecipeMenu(List<Recipe> recipes, String title) {
		super(RecipeEntry.useRecipes(recipes), title);
	}
	
	public RecipeMenu(List<Recipe> recipes, String title, int fillCol, int edgeStrokeCol, int edgeFillCol) {
		super(
			ItemListMenu.getBuilder().setFrame(fillCol, edgeStrokeCol, edgeFillCol),
			RecipeEntry.useRecipes(recipes),
			title
		);
	}
}

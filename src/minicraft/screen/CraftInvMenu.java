package minicraft.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import minicraft.crafting.Recipe;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.ResourceItem;
import minicraft.sound.Sound;

/// this is the "z" personal crafting menu.
public class CraftInvMenu extends Menu {
	private Player player;
	private int selected = 0;
	
	private List<Recipe> recipes;

	public CraftInvMenu(List<Recipe> recipes, Player player) {
		this.recipes = new ArrayList<Recipe>(recipes);
		this.player = player;

		for (int i = 0; i < recipes.size(); i++) {
			this.recipes.get(i).checkCanCraft(player);
		}

		Collections.sort(
				this.recipes,
				new Comparator<Recipe>() {
					public int compare(Recipe r1, Recipe r2) {
						if (r1.canCraft && !r2.canCraft) return -1;
						if (!r1.canCraft && r2.canCraft) return 1;
						return 0;
					}
				});
	}

	public void tick() {
		if (input.getKey("craft").clicked) game.setMenu(null);
		
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;
		if (input.getKey("up").clicked) Sound.pickup.play();
		if (input.getKey("down").clicked) Sound.pickup.play();

		int len = recipes.size();
		if (len == 0) selected = 0;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		
		if (input.getKey("attack").clicked && len > 0) {
			Recipe r = recipes.get(selected);
			r.checkCanCraft(player);
			if (r.canCraft) {
				r.deductCost(player);
				r.craft(player);
				Sound.craft.play();
			}
			for (int i = 0; i < recipes.size(); i++) {
				recipes.get(i).checkCanCraft(player);
			}
		}
	}

	public void render(Screen screen) {
		renderFrame(screen, "Have", 15, 1, 22, 3);
		renderFrame(screen, "Cost", 15, 4, 22, 11);
		renderFrame(screen, "Crafting", 0, 1, 14, 11);
		
		renderItemList(screen, 0, 1, 14, 11, recipes, selected);
		
		if (recipes.size() > 0) {
			Recipe recipe = recipes.get(selected);
			int hasResultItems = player.inventory.count(recipe.resultTemplate);
			int xo = 16 * 8;
			screen.render(
					xo, 2 * 8, recipe.resultTemplate.getSprite(), recipe.resultTemplate.getColor(), 0);
			Font.draw("" + hasResultItems, screen, xo + 8, 2 * 8, Color.get(-1, 555, 555, 555));

			List<Item> costs = recipe.costs;
			for (int i = 0; i < costs.size(); i++) {
				Item item = costs.get(i);
				int yo = (5 + i) * 8;
				screen.render(xo, yo, item.getSprite(), item.getColor(), 0);
				int requiredAmt = 1;
				if (item instanceof ResourceItem) {
					requiredAmt = ((ResourceItem) item).count;
				}
				int has = player.inventory.count(item);
				int color = Color.get(-1, 555, 555, 555);
				if (has < requiredAmt) {
					color = Color.get(-1, 222, 222, 222);
				}
				if (has > 99) has = 99;
				Font.draw("" + requiredAmt + "/" + has, screen, xo + 8, yo, color);
			}
		}
	}
	
	protected void renderFrame(Screen screen, String title, int x0, int y0, int x1, int y1) {
		renderMenuFrame(screen, title, x0, y0, x1, y1, Color.get(-1, 1, 300, 400), Color.get(300, 300), Color.get(300, 300, 300, 555));
	}
}

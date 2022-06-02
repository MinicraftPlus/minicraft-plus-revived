package minicraft.screen;

import java.util.*;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.gfx.Point;
import minicraft.gfx.SpriteSheet;
import minicraft.item.*;
import minicraft.screen.entry.ItemListing;

public class CraftingDisplay extends Display {

	private Player player;
	private Recipe[] recipes;

	private RecipeMenu recipeMenu;
	private Menu.Builder itemCountMenu, costsMenu;

	private boolean isPersonalCrafter;

	public CraftingDisplay(List<Recipe> recipes, String title, Player player) { this(recipes, title, player, false); }
	public CraftingDisplay(List<Recipe> recipes, String title, Player player, boolean isPersonal) {
		for(Recipe recipe: recipes)
			recipe.checkCanCraft(player);

		this.isPersonalCrafter = isPersonal;

		if(!isPersonal)
			recipeMenu = new RecipeMenu(recipes, title, player);
		else
			recipeMenu = new RecipeMenu(recipes, title, player);

		this.player = player;
		this.recipes = recipes.toArray(new Recipe[recipes.size()]);

		itemCountMenu = new Menu.Builder(true, 0, RelPos.LEFT)
			.setTitle("Have:")
			.setTitlePos(RelPos.TOP_LEFT)
			.setPositioning(new Point(recipeMenu.getBounds().getRight()+SpriteSheet.boxWidth, recipeMenu.getBounds().getTop()), RelPos.BOTTOM_RIGHT);

		costsMenu = new Menu.Builder(true, 0, RelPos.LEFT)
			.setTitle("Cost:")
			.setTitlePos(RelPos.TOP_LEFT)
			.setPositioning(new Point(itemCountMenu.createMenu().getBounds().getLeft(), recipeMenu.getBounds().getBottom()), RelPos.TOP_RIGHT);

		menus = new Menu[] {recipeMenu, itemCountMenu.createMenu(), costsMenu.createMenu()};

		refreshData();
	}

	private void refreshData() {
		Menu prev = menus[2];
		menus[2] = costsMenu
			.setEntries(getCurItemCosts())
			.createMenu();
		menus[2].setColors(prev);

		menus[1] = itemCountMenu
			.setEntries(new ItemListing(recipes[recipeMenu.getSelection()].getProduct(), String.valueOf(getCurItemCount())))
			.createMenu();
		menus[1].setColors(prev);
	}

	private int getCurItemCount() {
		return player.getInventory().count(recipes[recipeMenu.getSelection()].getProduct());
	}

	private ItemListing[] getCurItemCosts() {
		ArrayList<ItemListing> costList = new ArrayList<>();
		HashMap<String, Integer> costMap = recipes[recipeMenu.getSelection()].getCosts();
		for(String itemName: costMap.keySet()) {
			Item cost = Items.get(itemName);
			costList.add(new ItemListing(cost, player.getInventory().count(cost) + "/" + costMap.get(itemName)));
		}

		return costList.toArray(new ItemListing[costList.size()]);
	}

	@Override
	public void tick(InputHandler input) {
		int previousSelection = recipeMenu.getSelection();
		super.tick(input);
		if (previousSelection != recipeMenu.getSelection()) {
			refreshData();
		}

		if (input.getKey("menu").clicked || (isPersonalCrafter && input.getKey("craft").clicked)) {
			Game.exitDisplay();
			return;
		}

		if ((input.getKey("select").clicked || input.getKey("attack").clicked) && recipeMenu.getSelection() >= 0) {
			// check the selected recipe
			Recipe selectedRecipe = recipes[recipeMenu.getSelection()];
			if (selectedRecipe.getCanCraft()) {
				if (selectedRecipe.getProduct().equals(Items.get("Workbench"))){
					AchievementsDisplay.setAchievement("minicraft.achievement.benchmarking",true);
				}
				if (selectedRecipe.getProduct().equals(Items.get("Plank"))){
					AchievementsDisplay.setAchievement("minicraft.achievement.planks",true);
				}
				if (selectedRecipe.getProduct().equals(Items.get("Wood Door"))){
					AchievementsDisplay.setAchievement("minicraft.achievement.doors",true);
				}
				if (selectedRecipe.getProduct().equals(Items.get("Rock Sword")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Pickaxe")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Axe")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Shovel")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Hoe")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Bow")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Claymore"))) {
					AchievementsDisplay.setAchievement("minicraft.achievement.upgrade", true);
				}
				if (selectedRecipe.getProduct().equals(Items.get("blue clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("green clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("yellow clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("black clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("orange clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("purple clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("cyan clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("reg clothes"))) {
					AchievementsDisplay.setAchievement("minicraft.achievement.clothes", true);
				}

				if (((ToolItem) selectedRecipe.getProduct()).type.equals(ToolType.Sword)) {
					QuestsDisplay.completeQuest("minicraft.quest.craft_first_sword");
				}

				selectedRecipe.craft(player);

				Sound.craft.play();

				refreshData();
				for (Recipe recipe : recipes) {
					recipe.checkCanCraft(player);
				}
			}
		}
	}
}

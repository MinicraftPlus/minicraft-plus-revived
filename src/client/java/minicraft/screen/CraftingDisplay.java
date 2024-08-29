package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.screen.entry.ItemListing;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CraftingDisplay extends Display {

	private final Player player;
	private final String title;
	private Recipe[] recipes;
	private final List<Recipe> availableRecipes = new ArrayList<>();

	private RecipeMenu recipeMenu;
	private final Menu.Builder itemCountMenu, costsMenu;

	private final boolean isPersonalCrafter;

	private static final HashSet<Recipe> unlockedRecipes = new HashSet<>();

	public CraftingDisplay(List<Recipe> recipes, String title, Player player) {
		this(recipes, title, player, false);
	}

	public CraftingDisplay(List<Recipe> recipes, String title, Player player, boolean isPersonal) {
		for (Recipe recipe : recipes)
			recipe.checkCanCraft(player);
		this.player = player;
		this.title = title;
		this.isPersonalCrafter = isPersonal;
		availableRecipes.addAll(recipes);

		itemCountMenu = new Menu.Builder(true, 0, RelPos.LEFT)
			.setTitle("minicraft.displays.crafting.container_title.have")
			.setTitlePos(RelPos.TOP_LEFT);

		costsMenu = new Menu.Builder(true, 0, RelPos.LEFT)
			.setTitle("minicraft.displays.crafting.container_title.cost")
			.setTitlePos(RelPos.TOP_LEFT);
		refreshDisplayRecipes();
	}

	private void refreshDisplayRecipes() {
		List<Recipe> recipes = availableRecipes.stream().filter(unlockedRecipes::contains).collect(Collectors.toList());
		recipeMenu = new RecipeMenu(recipes, title, player);
		this.recipes = recipes.toArray(new Recipe[0]);
		itemCountMenu.setPositioning(new Point(recipeMenu.getBounds().getRight() + MinicraftImage.boxWidth, recipeMenu.getBounds().getTop()), RelPos.BOTTOM_RIGHT);
		costsMenu.setPositioning(new Point(itemCountMenu.createMenu().getBounds().getLeft(), recipeMenu.getBounds().getBottom()), RelPos.TOP_RIGHT);

		menus = new Menu[] { recipeMenu, itemCountMenu.createMenu(), costsMenu.createMenu() };
		refreshData();

		onScreenKeyboardMenu = OnScreenKeyboardMenu.checkAndCreateMenu();
		if (onScreenKeyboardMenu != null)
			onScreenKeyboardMenu.setVisible(false);
	}

	private void refreshData() {
		if (recipes.length == 0) return;

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
		if (recipes.length == 0) return new ItemListing[0];

		Map<String, Integer> costMap = recipes[recipeMenu.getSelection()].getCosts();
		for (String itemName : costMap.keySet()) {
			Item cost = Items.get(itemName);
			costList.add(new ItemListing(cost, player.getInventory().count(cost) + "/" + costMap.get(itemName)));
		}

		return costList.toArray(new ItemListing[0]);
	}

	OnScreenKeyboardMenu onScreenKeyboardMenu;

	@Override
	public void render(Screen screen) {
		super.render(screen);
		if (onScreenKeyboardMenu != null)
			onScreenKeyboardMenu.render(screen);
	}

	@Override
	public void tick(InputHandler input) {
		boolean acted = false; // Checks if typing action is needed to be handled.
		boolean mainMethod = false;

		if (onScreenKeyboardMenu == null || !recipeMenu.isSearcherBarActive() && !onScreenKeyboardMenu.isVisible()) {
			if (input.inputPressed("menu") || (isPersonalCrafter && input.inputPressed("craft"))) {
				Game.exitDisplay();
				return;
			}

			mainMethod = true;
		} else {
			try {
				onScreenKeyboardMenu.tick(input);
			} catch (OnScreenKeyboardMenu.OnScreenKeyboardMenuTickActionCompleted |
			         OnScreenKeyboardMenu.OnScreenKeyboardMenuBackspaceButtonActed e) {
				acted = true;
			}

			if (!acted)
				recipeMenu.tick(input);

			if (input.getMappedKey("menu").isClicked() || (isPersonalCrafter && input.inputPressed("craft"))) {
				Game.exitDisplay();
				return;
			}

			if (recipeMenu.isSearcherBarActive()) {
				if (input.buttonPressed(ControllerButton.X)) { // Hide the keyboard.
					onScreenKeyboardMenu.setVisible(!onScreenKeyboardMenu.isVisible());
				}
			} else {
				onScreenKeyboardMenu.setVisible(false);
			}
		}

		if (mainMethod || !onScreenKeyboardMenu.isVisible()) {
			int previousSelection = recipeMenu.getSelection();
			super.tick(input);
			if (previousSelection != recipeMenu.getSelection()) {
				refreshData();
			}

			if ((input.inputPressed("select") || input.inputPressed("attack")) && recipeMenu.getSelection() >= 0) {
				// check the selected recipe
				if (recipes.length == 0) return;
				Recipe selectedRecipe = recipes[recipeMenu.getSelection()];
				if (selectedRecipe.getCanCraft()) {
					if (selectedRecipe.getProduct().equals(Items.get("Workbench"))) {
						AchievementsDisplay.setAchievement("minicraft.achievement.benchmarking", true);
					} else if (selectedRecipe.getProduct().equals(Items.get("Plank"))) {
						AchievementsDisplay.setAchievement("minicraft.achievement.planks", true);
					} else if (selectedRecipe.getProduct().equals(Items.get("Wood Door"))) {
						AchievementsDisplay.setAchievement("minicraft.achievement.doors", true);
					} else if (selectedRecipe.getProduct().equals(Items.get("Rock Sword")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Pickaxe")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Axe")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Shovel")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Hoe")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Bow")) ||
						selectedRecipe.getProduct().equals(Items.get("Rock Claymore"))) {
						AchievementsDisplay.setAchievement("minicraft.achievement.upgrade", true);
					} else if (selectedRecipe.getProduct().equals(Items.get("blue clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("green clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("yellow clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("black clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("orange clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("purple clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("cyan clothes")) ||
						selectedRecipe.getProduct().equals(Items.get("reg clothes"))) {
						AchievementsDisplay.setAchievement("minicraft.achievement.clothes", true);
					} else if (selectedRecipe.getProduct().equals(Items.get("Boat"))) {
						AchievementsDisplay.setAchievement("minicraft.achievement.boat", true);
					}

					selectedRecipe.craft(player);

					Sound.play("craft");

					refreshData();
					for (Recipe recipe : recipes) {
						recipe.checkCanCraft(player);
					}
				}
			}
		}

	}

	private static void refreshInstanceIfNeeded() {
		Display display = Game.getDisplay();
		if (display instanceof CraftingDisplay) {
			((CraftingDisplay) display).refreshDisplayRecipes();
		}
	}

	public static void resetRecipeUnlocks() {
		unlockedRecipes.clear();
	}

	public static void unlockRecipe(@NotNull Recipe recipe) {
		unlockedRecipes.add(recipe);
		refreshInstanceIfNeeded();
	}

	public static Set<Recipe> getUnlockedRecipes() {
		return new HashSet<>(unlockedRecipes);
	}

	public static void loadUnlockedRecipes(Collection<Recipe> recipes) {
		resetRecipeUnlocks();
		unlockedRecipes.addAll(recipes);
	}
}

package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Recipe;
import minicraft.item.Recipes;
import minicraft.screen.CraftingDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Crafter extends Furniture {

	public enum Type {
		Workbench(new LinkedSprite(SpriteType.Entity, "workbench"), new LinkedSprite(SpriteType.Item, "workbench"), 3, 2, Recipes.workbenchRecipes),
		Oven(new LinkedSprite(SpriteType.Entity, "oven"), new LinkedSprite(SpriteType.Item, "oven"), 3, 2, Recipes.ovenRecipes),
		Furnace(new LinkedSprite(SpriteType.Entity, "furnace"), new LinkedSprite(SpriteType.Item, "furnace"), 3, 2, Recipes.furnaceRecipes),
		Anvil(new LinkedSprite(SpriteType.Entity, "anvil"), new LinkedSprite(SpriteType.Item, "anvil"), 3, 2, Recipes.anvilRecipes),
		Enchanter(new LinkedSprite(SpriteType.Entity, "enchanter"), new LinkedSprite(SpriteType.Item, "enchanter"), 7, 2, Recipes.enchantRecipes),
		Loom(new LinkedSprite(SpriteType.Entity, "loom"), new LinkedSprite(SpriteType.Item, "loom"), 7, 2, Recipes.loomRecipes);

		public ArrayList<Recipe> recipes;
		protected LinkedSprite sprite;
		protected LinkedSprite itemSprite;
		protected int xr, yr;

		Type(LinkedSprite sprite, LinkedSprite itemSprite, int xr, int yr, ArrayList<Recipe> list) {
			this.sprite = sprite;
			this.itemSprite = itemSprite;
			this.xr = xr;
			this.yr = yr;
			recipes = list;
			Crafter.names.add(this.name());
		}
	}

	public static ArrayList<String> names = new ArrayList<>();

	public Crafter.Type type;

	/**
	 * Creates a crafter of a given type.
	 *
	 * @param type What type of crafter this is.
	 */
	public Crafter(Crafter.Type type) {
		super(type.name(), type.sprite, type.itemSprite, type.xr, type.yr);
		this.type = type;
	}

	public boolean use(Player player) {
		Game.setDisplay(new CraftingDisplay(type.recipes, type.name(), player));
		return true;
	}

	@Override
	public @NotNull Furniture copy() {
		return new Crafter(type);
	}

	@Override
	public String toString() {
		return type.name() + getDataPrints();
	}
}

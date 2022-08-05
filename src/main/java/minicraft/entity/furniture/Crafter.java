package minicraft.entity.furniture;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Recipe;
import minicraft.item.Recipes;
import minicraft.screen.CraftingDisplay;

public class Crafter extends Furniture {

	public enum Type {
		Workbench (new LinkedSpriteSheet(SpriteType.Entity, "workbench"), new LinkedSpriteSheet(SpriteType.Item, "workbench"), 3, 2, Recipes.workbenchRecipes),
		Oven (new LinkedSpriteSheet(SpriteType.Entity, "oven"), new LinkedSpriteSheet(SpriteType.Item, "oven"), 3, 2, Recipes.ovenRecipes),
		Furnace (new LinkedSpriteSheet(SpriteType.Entity, "furnace"), new LinkedSpriteSheet(SpriteType.Item, "furnace"), 3, 2, Recipes.furnaceRecipes),
		Anvil (new LinkedSpriteSheet(SpriteType.Entity, "anvil"), new LinkedSpriteSheet(SpriteType.Item, "anvil"), 3, 2, Recipes.anvilRecipes),
		Enchanter (new LinkedSpriteSheet(SpriteType.Entity, "enchanter"), new LinkedSpriteSheet(SpriteType.Item, "enchanter"), 7, 2, Recipes.enchantRecipes),
		Loom (new LinkedSpriteSheet(SpriteType.Entity, "loom"), new LinkedSpriteSheet(SpriteType.Item, "loom"), 7, 2, Recipes.loomRecipes);

		public ArrayList<Recipe> recipes;
		protected LinkedSpriteSheet sprite;
		protected LinkedSpriteSheet itemSprite;
		protected int xr, yr;

		Type(LinkedSpriteSheet sprite, LinkedSpriteSheet itemSprite, int xr, int yr, ArrayList<Recipe> list) {
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
	public Furniture clone() {
		return new Crafter(type);
	}

	@Override
	public String toString() { return type.name()+getDataPrints(); }
}

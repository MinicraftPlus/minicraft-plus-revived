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
		Workbench (new LinkedSprite.SpriteLinkBuilder(SpriteType.Entity, "workbench").createSpriteLink(),
			new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "workbench").createSpriteLink(), 3, 2, Recipes.workbenchRecipes),
		Oven (new LinkedSprite.SpriteLinkBuilder(SpriteType.Entity, "oven").createSpriteLink(),
			new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "oven").createSpriteLink(), 3, 2, Recipes.ovenRecipes),
		Furnace (new LinkedSprite.SpriteLinkBuilder(SpriteType.Entity, "furnace").createSpriteLink(),
			new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "furnace").createSpriteLink(), 3, 2, Recipes.furnaceRecipes),
		Anvil (new LinkedSprite.SpriteLinkBuilder(SpriteType.Entity, "anvil").createSpriteLink(),
			new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "anvil").createSpriteLink(), 3, 2, Recipes.anvilRecipes),
		Enchanter (new LinkedSprite.SpriteLinkBuilder(SpriteType.Entity, "enchanter").createSpriteLink(),
			new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "enchanter").createSpriteLink(), 7, 2, Recipes.enchantRecipes),
		Loom (new LinkedSprite.SpriteLinkBuilder(SpriteType.Entity, "loom").createSpriteLink(),
			new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "loom").createSpriteLink(), 7, 2, Recipes.loomRecipes);

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
	public String toString() { return type.name()+getDataPrints(); }
}

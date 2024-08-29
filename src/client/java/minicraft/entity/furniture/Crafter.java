package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteManager.SpriteLink;
import minicraft.gfx.SpriteManager.SpriteType;
import minicraft.item.Recipe;
import minicraft.item.Recipes;
import minicraft.screen.CraftingDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Crafter extends Furniture {

	public enum Type {
		Workbench(new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, "workbench").createSpriteLink(),
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "workbench").createSpriteLink(), 3, 2, Recipes.workbenchRecipes),
		Oven(new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, "oven").createSpriteLink(),
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "oven").createSpriteLink(), 3, 2, Recipes.ovenRecipes),
		Furnace(new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, "furnace").createSpriteLink(),
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "furnace").createSpriteLink(), 3, 2, Recipes.furnaceRecipes),
		Anvil(new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, "anvil").createSpriteLink(),
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "anvil").createSpriteLink(), 3, 2, Recipes.anvilRecipes),
		Enchanter(new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, "enchanter").createSpriteLink(),
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "enchanter").createSpriteLink(), 7, 2, Recipes.enchantRecipes),
		Loom(new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, "loom").createSpriteLink(),
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "loom").createSpriteLink(), 7, 2, Recipes.loomRecipes),
		DyeVat(new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, "dyevat").createSpriteLink(),
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "dyevat").createSpriteLink(), 0, 0, Recipes.dyeVatRecipes);

		public ArrayList<Recipe> recipes;
		protected SpriteLink sprite;
		protected SpriteLink itemSprite;
		protected int xr, yr;

		Type(SpriteLink sprite, SpriteLink itemSprite, int xr, int yr, ArrayList<Recipe> list) {
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
		super((type.name().equalsIgnoreCase("DyeVat") ? "Dye Vat" : type.name()), type.sprite, type.itemSprite, type.xr, type.yr);
		this.type = type;
	}

	public boolean use(Player player) {
		Game.setDisplay(new CraftingDisplay(type.recipes, (type.name().equalsIgnoreCase("DyeVat") ? "Dye Vat" : type.name()), player));
		return true;
	}

	@Override
	public @NotNull Furniture copy() {
		return new Crafter(type);
	}

	@Override
	public String toString() {
		return (type.name().equalsIgnoreCase("DyeVat") ? "Dye Vat" : type.name()) + getDataPrints();
	}
}

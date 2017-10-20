package minicraft.entity;

import java.util.ArrayList;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.Recipe;
import minicraft.item.Recipes;
import minicraft.screen.CraftingMenu;

public class Crafter extends Furniture {
	
	public enum Type {
		Workbench (new Sprite(8, 8, 2, 2, Color.get(-1, 100, 321, 431)), 3, 2, Recipes.workbenchRecipes),
		Oven (new Sprite(4, 8, 2, 2, Color.get(-1, 000, 332, 442)), 3, 2, Recipes.ovenRecipes),
		Furnace (new Sprite(6, 8, 2, 2, Color.get(-1, 000, 222, 333)), 3, 2, Recipes.furnaceRecipes),
		Anvil (new Sprite(0, 8, 2, 2, Color.get(-1, 000, 222, 333)), 3, 2, Recipes.anvilRecipes),
		Enchanter (new Sprite(12, 8, 2, 2, Color.get(-1, 623, 999, 111)), 7, 2, Recipes.enchantRecipes),
		Loom (new Sprite(18, 8, 2, 2, Color.get(-1, 100, 333, 211)), 7, 2, Recipes.loomRecipes);
		
		public ArrayList<Recipe> recipes;
		protected Sprite sprite;
		protected int xr, yr;
		
		
		Type(Sprite sprite, int xr, int yr, ArrayList<Recipe> list) {
			this.sprite = sprite;
			this.xr = xr;
			this.yr = yr;
			recipes = list;
			Crafter.names.add(this.name());
		}
	}
	public static ArrayList<String> names = new ArrayList<>();
	
	public Crafter.Type type;
	
	public Crafter(Crafter.Type type) {
		super(type.name(), type.sprite, type.xr, type.yr);
		this.type = type;
	}
	
	public boolean use(Player player, int attackDir) {
		Game.setMenu(new CraftingMenu(type.recipes, type.name(), player));
		return true;
	}
	
	public Furniture clone() {
		return new Crafter(type);
	}
	
	public String toString() {
		return super.toString().replace("Crafter", type.name());
	}
}

package minicraft.entity;

import minicraft.crafting.Crafting;
import minicraft.gfx.Color;
import minicraft.screen.CraftingMenu;

public class Oven extends Furniture {
	public Oven() {
		super("Oven");

		col0 = Color.get(-1, 000, 221, 331);
		col1 = Color.get(-1, 000, 332, 442);
		col2 = Color.get(-1, 000, 221, 331);
		col3 = Color.get(-1, 000, 110, 221);

		col = Color.get(-1, 000, 332, 442);
		sprite = 2;
		xr = 3;
		yr = 2;
	}
	
	/// opens the Oven menu.
	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new CraftingMenu(Crafting.ovenRecipes, player));
		return true;
	}
}

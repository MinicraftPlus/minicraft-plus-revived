package minicraft.entity;

import minicraft.crafting.Crafting;
import minicraft.gfx.Color;
import minicraft.screen.CraftingMenu;

// just another Furniture sub-class...
public class Workbench extends Furniture {
	public Workbench() {
		super("Workbench");

		col0 = Color.get(-1, 100, 211, 320);
		col1 = Color.get(-1, 211, 321, 431);
		col2 = Color.get(-1, 100, 211, 320);
		col3 = Color.get(-1, 000, 100, 210);

		col = Color.get(-1, 100, 321, 431);
		sprite = 4;
		xr = 3;
		yr = 2;
	}

	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new CraftingMenu(Crafting.workbenchRecipes, player));
		return true;
	}
}

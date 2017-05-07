package minicraft.entity;

import minicraft.crafting.Crafting;
import minicraft.gfx.Color;
import minicraft.screen.CraftingMenu;

public class Loom extends Furniture {
	public Loom() {
		super("Loom", Color.get(-1, 100, 333, 211), 9, 7, 2);
		/*
		col0 = Color.get(-1, 100, 333, 211);
		col1 = Color.get(-1, 211, 444, 322);
		col2 = Color.get(-1, 100, 333, 211);
		col3 = Color.get(-1, 000, 222, 100);
		*/
		/*col = Color.get(-1, 100, 333, 211);
		sprite = 9;
		xr = 7;
		yr = 2;*/
	}
	
	/// called when the player presses menu button with the loom in front of them.
	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new CraftingMenu(Crafting.loomRecipes, player)); // open the loom menu.
		return true;
	}
}

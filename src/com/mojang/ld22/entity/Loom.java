//new class, no comments.
package com.mojang.ld22.entity;

import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.screen.CraftingMenu;

public class Loom extends Furniture {
	public Loom() {
		super("Loom");

		col0 = Color.get(-1, 100, 333, 211);
		col1 = Color.get(-1, 211, 444, 322);
		col2 = Color.get(-1, 100, 333, 211);
		col3 = Color.get(-1, 000, 222, 100);

		col = Color.get(-1, 100, 333, 211);
		sprite = 9;
		xr = 7;
		yr = 2;
	}

	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new CraftingMenu(Crafting.loomRecipes, player));
		return true;
	}
}

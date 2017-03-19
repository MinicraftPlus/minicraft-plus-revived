//new class, no comments.
package com.mojang.ld22.entity;

import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.screen.CraftingMenu;

public class Enchanter extends Furniture {
	public Enchanter() {
		super("Enchanter");

		col0 = Color.get(-1, 613, 888, 111);
		col1 = Color.get(-1, 623, 999, 222);
		col2 = Color.get(-1, 613, 888, 111);
		col3 = Color.get(-1, 603, 777, 000);

		col = Color.get(-1, 623, 999, 111);
		sprite = 6;
		xr = 7;
		yr = 2;
	}

	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new CraftingMenu(Crafting.enchantRecipes, player));
		return true;
	}
}

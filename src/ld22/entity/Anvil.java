package com.mojang.ld22.entity;

import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.screen.CraftingMenu;

public class Anvil extends Furniture {
	public Anvil() {
		super("Anvil");

		col0 = Color.get(-1, 000, 222, 333);
		col1 = Color.get(-1, 000, 333, 444);
		col2 = Color.get(-1, 000, 222, 333);
		col3 = Color.get(-1, 000, 111, 222);

		col = Color.get(-1, 000, 222, 333);
		sprite = 0;
		xr = 3;
		yr = 2;
	}

	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new CraftingMenu(Crafting.anvilRecipes, player));
		return true;
	}
}

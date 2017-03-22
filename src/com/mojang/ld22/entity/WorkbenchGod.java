//new class, no comments.
package com.mojang.ld22.entity;

import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.screen.CraftingMenu;

public class WorkbenchGod extends Furniture {
	public WorkbenchGod() {
		super("God Workbench");

		col0 = Color.get(-1, 110, 330, 442);
		col1 = Color.get(-1, 220, 440, 553);
		col2 = Color.get(-1, 110, 330, 442);
		col3 = Color.get(-1, 000, 220, 331);

		col = Color.get(-1, 110, 440, 553);
		sprite = 4;
		xr = 3;
		yr = 2;
	}

	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new CraftingMenu(Crafting.godworkbenchRecipes, player));
		return true;
	}
}

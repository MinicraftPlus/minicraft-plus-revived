package com.mojang.ld22.entity;

import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.screen.CraftingMenu;
import com.mojang.ld22.Game;
import com.mojang.ld22.screen.PauseMenu;
import com.mojang.ld22.screen.SleepMenu;

public class bed extends Furniture {
	public static boolean hasBedSet;
	public static boolean hasBeenTrigged = false;
	public static int newSpawnY;
	public static int newSpawnX;
	public bed() {
		super("Bed");
		col0 = Color.get(-1, 211, 444, 400);
		 col1 = Color.get(-1, 211, 555, 500);
		 col2 = Color.get(-1, 100, 333, 300); 
		 col3 = Color.get(-1, 000, 222, 200);
		 col = Color.get(-1, 100, 444, 400);
	
		sprite = 8;
		xr = 3;
		yr = 2;
	}
	
	public boolean use(Player player, int attackDir) {
		hasBedSet = true;
		hasBeenTrigged = true;
		player.game.setMenu(new SleepMenu());
		return true;
}
}
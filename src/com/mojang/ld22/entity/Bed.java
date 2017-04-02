//new class, no comments
package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;

public class Bed extends Furniture {
	public static boolean hasBedSet = false;
	//public static boolean hasBeenTrigged = false;
	public int saveSpawnX, saveSpawnY;
	
	public Bed() {
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
		//hasBedSet = true;
		//hasBeenTrigged = true;
		if (!player.game.isDayNoSleep) {
			hasBedSet = true;
			Player.spawnx = x / 16;
			Player.spawny = y / 16;
			player.bedSpawn = true;
			if(Game.debug) System.out.println(this.x + "/" + this.y + "//" + Player.spawnx + "/" + Player.spawny);
		} else {
			int sec = ('\ua8c0' - Game.tickCount) / 60;
			Game.notifications.add(
					"Can\'t sleep! " + (sec / 60) + "Min " + (sec - sec % 60) + " Sec left!");
		}

		return true;
	}
}

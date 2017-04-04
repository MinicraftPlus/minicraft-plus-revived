package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;

public class Bed extends Furniture {
	public static boolean hasBedSet = false; // If the player (as there is only one) has set their home/bed.
	public int saveSpawnX, saveSpawnY; // the saved spawn locations... never used, though, I don't think...
	
	public Bed() {
		super("Bed");
		col0 = Color.get(-1, 211, 444, 400);
		col1 = Color.get(-1, 211, 555, 500);
		col2 = Color.get(-1, 100, 333, 300);
		col3 = Color.get(-1, 000, 222, 200);
		col = Color.get(-1, 100, 444, 400);

		sprite = 8;
		// set the x and y radius of the Bed.
		xr = 3;
		yr = 2;
	}
	
	/** Called when the player attempts to get in bed. */
	public boolean use(Player player, int attackDir) {
		if (Game.tickCount >= Game.sleepTime) { // if it is late enough in the day to sleep...
			hasBedSet = true; // the bed is now set.
			// set the player spawn coord. to here, in tile coords, hence "/ 16"
			Player.spawnx = x / 16;
			Player.spawny = y / 16;
			player.bedSpawn = true; // wait, but then... what's hasBedSet for??? The two should always go hand in hand!
			//if(Game.debug) System.out.println("bedPos: ("+(x/16)+","+(y/16)+"); spawnPos: ("+player.spawnx+","+player.spawny+")"); // debug to print the bed loc and spawn loc
		} else {
			// it is too early to sleep; display how much time is remaining.
			int sec = (Game.tickCount - Game.sleepTime) / Game.normSpeed; // gets the seconds until sleeping is allowed. // normSpeed is in tiks/sec.
			Game.notifications.add("Can't sleep! " + (sec / 60) + "Min " + (sec - sec % 60) + " Sec left!"); // add the notification displaying the time remaining in minutes and seconds.
		}
		
		return true;
	}
}

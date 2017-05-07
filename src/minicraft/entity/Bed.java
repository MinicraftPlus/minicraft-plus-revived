package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Color;

public class Bed extends Furniture {
	public static boolean inBed = false; // If the player (as there is only one) is in their bed.
	public int saveSpawnX, saveSpawnY; // the saved spawn locations... never used, though, I don't think...
	
	public Bed() {
		super("Bed", Color.get(-1, 100, 444, 400), 8, 3, 2);
		/*col0 = Color.get(-1, 211, 444, 400);
		col1 = Color.get(-1, 211, 555, 500);
		col2 = Color.get(-1, 100, 333, 300);
		col3 = Color.get(-1, 000, 222, 200);
		*///col = Color.get(-1, 100, 444, 400);
		
		//sprite = 8;
		// set the x and y radius of the Bed.
		//xr = 3;
		//yr = 2;
	}
	
	/** Called when the player attempts to get in bed. */
	public boolean use(Player player, int attackDir) {
		if (Game.tickCount >= Game.sleepTime) { // if it is late enough in the day to sleep...
			inBed = true;
			// set the player spawn coord. to here, in tile coords, hence " >> 4"
			Player.spawnx = x >> 4;
			Player.spawny = y >> 4;
			player.bedSpawn = true;  // the bed is now set as the player spawn point.
		} else {
			// it is too early to sleep; display how much time is remaining.
			int sec = (Game.sleepTime - Game.tickCount) / Game.normSpeed; // gets the seconds until sleeping is allowed. // normSpeed is in tiks/sec.
			Game.notifications.add("Can't sleep! " + (sec / 60) + "Min " + (sec % 60) + " Sec left!"); // add the notification displaying the time remaining in minutes and seconds.
		}
		
		return true;
	}
}

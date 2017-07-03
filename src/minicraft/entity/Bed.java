package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class Bed extends Furniture {
	public static boolean inBed = false; // If a player is in a bed.
	private static Player player = null; // the player that is in bed.
	private static Level playerLevel = null; // the player that is in bed.
	
	public Bed() {
		super("Bed", new Sprite(16, 8, 2, 2, Color.get(-1, 100, 444, 400)), 3, 2);
	}
	
	/** Called when the player attempts to get in bed. */
	public boolean use(Player player, int attackDir) {
		if (Game.tickCount >= Game.sleepStartTime || Game.tickCount < Game.sleepEndTime) { // if it is late enough in the day to sleep...
			// set the player spawn coord. to here, in tile coords, hence " >> 4"
			player.spawnx = x >> 4;
			player.spawny = y >> 4;
			//player.bedSpawn = true; // the bed is now set as the player spawn point.
			Bed.player = player;
			Bed.playerLevel = player.getLevel();
			player.remove();
			inBed = true;
		} else {
			// it is too early to sleep; display how much time is remaining.
			int sec = (int)Math.ceil((Game.sleepStartTime - Game.tickCount)*1.0 / Game.normSpeed); // gets the seconds until sleeping is allowed. // normSpeed is in tiks/sec.
			player.game.notifications.add("Can't sleep! " + (sec / 60) + "Min " + (sec % 60) + " Sec left!"); // add the notification displaying the time remaining in minutes and seconds.
		}
		
		return true;
	}
	
	public static Player restorePlayer() {
		if(Bed.playerLevel != null)
			Bed.playerLevel.add(Bed.player);
		else
			System.out.println("player was previously on null level before bed... can't restore player: " + Bed.player);
		Bed.playerLevel = null;
		Player p = player;
		Bed.player = null;
		Bed.inBed = false;
		return p;
	}
	
	//public static Player getPlayer() { return player; }
}

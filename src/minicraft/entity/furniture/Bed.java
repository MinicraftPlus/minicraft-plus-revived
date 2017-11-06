package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.Network;
import minicraft.core.Updater;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
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
	public boolean use(Player player) {
		if (checkCanSleep()) { // if it is late enough in the day to sleep...
			// set the player spawn coord. to their current position, in tile coords (hence " >> 4")
			player.spawnx = player.x >> 4;
			player.spawny = player.y >> 4;
			//player.bedSpawn = true; // the bed is now set as the player spawn point.
			Bed.player = player;
			Bed.playerLevel = player.getLevel();
			Bed.inBed = true;
			if (Game.debug) System.out.println(Network.onlinePrefix()+"player got in bed: " + player);
			if(Game.isConnectedClient() && player == Game.player) {
				Game.client.sendBedRequest(player, this);
			}
			else {
				player.remove();
				if(Game.isValidServer() && player instanceof RemotePlayer)
					Game.server.getAssociatedThread((RemotePlayer)player).sendEntityRemoval(player.eid);
			}
		}
		
		return true;
	}
	
	public static boolean checkCanSleep() {
		if(!(Updater.tickCount >= Updater.sleepStartTime || Updater.tickCount < Updater.sleepEndTime && Updater.pastDay1)) {
			// it is too early to sleep; display how much time is remaining.
			int sec = (int)Math.ceil((Updater.sleepStartTime - Updater.tickCount)*1.0 / Updater.normSpeed); // gets the seconds until sleeping is allowed. // normSpeed is in tiks/sec.
			String note = "Can't sleep! " + (sec / 60) + "Min " + (sec % 60) + " Sec left!";
			if(!Game.isValidServer())
				Game.notifications.add(note); // add the notification displaying the time remaining in minutes and seconds.
			else if(player instanceof RemotePlayer)
				Game.server.getAssociatedThread((RemotePlayer)player).sendNotification(note, 0);
			else
				System.out.println("WARNING: regular player found trying to get into bed on server; not a RemotePlayer: " + player);
			
			return false;
		}
		
		return true;
	}
	
	public static Player restorePlayer() {
		if(Bed.playerLevel != null) {
			Bed.playerLevel.add(Bed.player); // this adds the player to all the other clients' levels
			if(Game.isValidServer() && player instanceof RemotePlayer)
				Game.server.getAssociatedThread((RemotePlayer)player).sendEntityAddition(player);
		} else
			System.out.println("player was previously on null level before bed... can't restore player: " + Bed.player);
		Bed.playerLevel = null;
		Player p = player;
		Bed.player = null;
		Bed.inBed = false;
		return p;
	}
}

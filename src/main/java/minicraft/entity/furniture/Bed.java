package minicraft.entity.furniture;

import java.util.HashMap;

import minicraft.core.Game;
import minicraft.core.Network;
import minicraft.core.Updater;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class Bed extends Furniture {
	
	private static int playersAwake = 1;
	private static final HashMap<Player, Bed> sleepingPlayers = new HashMap<>();
	
	/**
	 * Creates a new furniture with the name Bed and the bed sprite and color.
	 */
	public Bed() {
		super("Bed", new Sprite(30, 26, 2, 2, 2), 3, 2);
	}
	
	/** Called when the player attempts to get in bed. */
	public boolean use(Player player) {
		if (checkCanSleep(player)) { // if it is late enough in the day to sleep...
			
			// set the player spawn coord. to their current position, in tile coords (hence " >> 4")
			player.spawnx = player.x >> 4;
			player.spawny = player.y >> 4;
			
			sleepingPlayers.put(player, this);
			if(Game.isConnectedClient() && player == Game.player) {
				Game.client.sendBedRequest(this);
				playersAwake = -1;
			}
			if (Game.debug) System.out.println(Network.onlinePrefix()+"player got in bed: " + player);
			player.remove();
			
			if(!Game.ISONLINE)
				playersAwake = 0;
			else if(Game.isValidServer()) {
				playersAwake = getPlayersAwake();
				Game.server.updateGameVars();
			}
		}
		
		return true;
	}
	
	public static int getPlayersAwake() {
		if(!Game.isValidServer())
			return playersAwake;
		
		int total = Game.server.getNumPlayers();
		return total - sleepingPlayers.size();
	}
	public static void setPlayersAwake(int count) {
		if(!Game.isValidClient())
			throw new IllegalStateException("Bed.setPlayersAwake() can only be called on a client runtime");
		
		playersAwake = count;
	}
	
	public static boolean checkCanSleep(Player player) {
		if(inBed(player)) return false;
		
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
	
	public static boolean sleeping() { return playersAwake == 0; }
	
	public static boolean inBed(Player player) { return sleepingPlayers.containsKey(player); }
	public static Level getBedLevel(Player player) {
		Bed bed = sleepingPlayers.get(player);
		if(bed == null)
			return null;
		return bed.getLevel();
	}
	
	// get the player "out of bed"; used on the client only.
	public static void removePlayer(Player player) {
		sleepingPlayers.remove(player);
	}
	
	public static void removePlayers() { sleepingPlayers.clear(); }
	
	// client should not call this.
	public static void restorePlayer(Player player) {
		Bed bed = sleepingPlayers.remove(player);
		if(bed != null) {
			if(bed.getLevel() == null)
				Game.levels[Game.currentLevel].add(player);
			else
				bed.getLevel().add(player);
			
			if(!Game.ISONLINE)
				playersAwake = 1;
			else if(Game.isValidServer()) {
				playersAwake = getPlayersAwake();
				Game.server.updateGameVars();
			}
		}
	}
	// client should not call this.
	public static void restorePlayers() {
		for(Player p: sleepingPlayers.keySet()) {
			Bed bed = sleepingPlayers.get(p);
			if(p instanceof RemotePlayer && Game.isValidServer() && !Game.server.getAssociatedThread((RemotePlayer)p).isConnected())
				continue; // forget about it, don't add it to the level
			bed.getLevel().add(p);
		}
		
		sleepingPlayers.clear();
		
		if(!Game.ISONLINE)
			playersAwake = 1;
		else if(Game.isValidServer()) {
			playersAwake = Game.server.getNumPlayers();
			Game.server.updateGameVars();
		}
	}
}

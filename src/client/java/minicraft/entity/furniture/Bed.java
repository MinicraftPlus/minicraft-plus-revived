package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Localization;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

import java.util.HashMap;

public class Bed extends Furniture {

	private static int playersAwake = 1;
	private static final HashMap<Player, Bed> sleepingPlayers = new HashMap<>();

	/**
	 * Creates a new furniture with the name Bed and the bed sprite and color.
	 */
	public Bed() {
		super("Bed", new LinkedSprite(SpriteType.Entity, "bed"), new LinkedSprite(SpriteType.Item, "bed"), 3, 2);
	}

	/**
	 * Called when the player attempts to get in bed.
	 */
	public boolean use(Player player) {
		if (checkCanSleep(player)) { // If it is late enough in the day to sleep...

			// Set the player spawn coord. to their current position, in tile coords (hence " >> 4")
			player.spawnx = player.x >> 4;
			player.spawny = player.y >> 4;

			sleepingPlayers.put(player, this);
			player.remove();

			playersAwake = 0;
		}

		return true;
	}

	public static boolean checkCanSleep(Player player) {
		if (inBed(player)) return false;

		if (!(Updater.tickCount >= Updater.sleepStartTime || Updater.tickCount < Updater.sleepEndTime && Updater.pastDay1)) {
			// It is too early to sleep; display how much time is remaining.
			int sec = (int) Math.ceil((Updater.sleepStartTime - Updater.tickCount) * 1.0 / Updater.normSpeed); // gets the seconds until sleeping is allowed. // normSpeed is in tiks/sec.
			String note = Localization.getLocalized("minicraft.notification.cannot_sleep", sec / 60, sec % 60);
			Game.notifications.add(note); // Add the notification displaying the time remaining in minutes and seconds.

			return false;
		}

		return true;
	}

	public static boolean sleeping() {
		return playersAwake == 0;
	}

	public static boolean inBed(Player player) {
		return sleepingPlayers.containsKey(player);
	}

	public static Level getBedLevel(Player player) {
		Bed bed = sleepingPlayers.get(player);
		if (bed == null)
			return null;
		return bed.getLevel();
	}

	// Get the player "out of bed"; used on the client only.
	public static void removePlayer(Player player) {
		sleepingPlayers.remove(player);
	}

	public static void removePlayers() {
		sleepingPlayers.clear();
	}

	// Client should not call this.
	public static void restorePlayer(Player player) {
		Bed bed = sleepingPlayers.remove(player);
		if (bed != null) {
			if (bed.getLevel() == null)
				Game.levels[Game.currentLevel].add(player);
			else
				bed.getLevel().add(player);

			playersAwake = 1;
		}
	}

	// Client should not call this.
	public static void restorePlayers() {
		for (Player p : sleepingPlayers.keySet()) {
			Bed bed = sleepingPlayers.get(p);
			bed.getLevel().add(p);
		}

		sleepingPlayers.clear();


		playersAwake = 1;
	}
}

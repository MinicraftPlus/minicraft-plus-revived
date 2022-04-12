package minicraft.core;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.furniture.Bed;
import minicraft.entity.mob.Player;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.saveload.Save;
import minicraft.screen.EndGameDisplay;
import minicraft.screen.LevelTransitionDisplay;
import minicraft.screen.PlayerDeathDisplay;
import minicraft.screen.WorldSelectDisplay;
import org.tinylog.Logger;

public class Updater extends Game {
	private Updater() {}
	
	// TIME AND TICKS
	
	public static final int normSpeed = 60; // Measured in ticks / second.
	public static float gamespeed = 1; // Measured in MULTIPLES OF NORMSPEED.
	public static boolean paused = true; // If the game is paused.

	public static int tickCount = 0; // The number of ticks since the beginning of the game day.
	static int time = 0; // Facilites time of day / sunlight.
	public static final int dayLength = 64800; // This value determines how long one game day is.
	public static final int sleepEndTime = dayLength/8; // This value determines when the player "wakes up" in the morning.
	public static final int sleepStartTime = dayLength/2+dayLength/8; // This value determines when the player allowed to sleep.
	//public static int noon = 32400; // This value determines when the sky switches from getting lighter to getting darker.

	public static int gameTime = 0; // This stores the total time (number of ticks) you've been playing your
	public static boolean pastDay1 = true; // Used to prevent mob spawn on surface on day 1.
	public static int scoreTime; // Time remaining for score mode

	/**
	 * Indicates if FullScreen Mode has been toggled.
	 */
	static boolean FULLSCREEN;

	// AUTOSAVE AND NOTIFICATIONS

	public static int notetick = 0; // "note"= notifications.

	private static final int astime = 7200; // tands for Auto-Save Time (interval)
	public static int asTick = 0; // The time interval between autosaves.
	public static boolean saving = false; // If the game is performing a save.
	public static int savecooldown; // Prevents saving many times too fast, I think.

	public enum Time {
		Morning (0),
		Day (dayLength/4),
		Evening (dayLength/2),
		Night (dayLength/4*3);

		public int tickTime;

		Time(int ticks) {
			tickTime = ticks;
		}
	}

	static void updateFullscreen() {
		// Dispose is needed to set undecorated value
		Initializer.frame.dispose();

		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
		if (Updater.FULLSCREEN) {
			Initializer.frame.setUndecorated(true);
			device.setFullScreenWindow(Initializer.frame);
		} else {
			Initializer.frame.setUndecorated(false);
			device.setFullScreenWindow(null);
		}

		// Show frame again
		Initializer.frame.setVisible(true);
		// When fullscreen is enabled, focus is lost
		Renderer.canvas.requestFocus();
	}

	// VERY IMPORTANT METHOD!! Makes everything keep happening.
	// In the end, calls menu.tick() if there's a menu, or level.tick() if no menu.
	public static void tick() {
		
		// Quick Level change: move the player for -1, or 1 levels
        	if (isMode("creative") && input.getKey("SHIFT-S").clicked ) {
        		Game.setDisplay(new LevelTransitionDisplay(-1));
        	
        	} else if (isMode("creative") && input.getKey("SHIFT-W").clicked ){
        		Game.setDisplay(new LevelTransitionDisplay(1));
        	
        	}

		if (input.getKey("FULLSCREEN").clicked) {
			Updater.FULLSCREEN = !Updater.FULLSCREEN;
			Updater.updateFullscreen();
		}

		if (newDisplay != display) {
			if (display != null && (newDisplay == null || newDisplay.getParent() != display))
				display.onExit();
			
			if (newDisplay != null && (display == null || newDisplay != display.getParent()))
				newDisplay.init(display);
			
			display = newDisplay;
		}
		
		Level level = levels[currentLevel];
		if (Bed.sleeping()) {
			// IN BED
			if (gamespeed != 20) {
				gamespeed = 20;
			}
			if (tickCount > sleepEndTime) {
				Logger.trace("Passing midnight in bed.");
				pastDay1 = true;
				tickCount = 0;
			}
			if (tickCount <= sleepStartTime && tickCount >= sleepEndTime) { // It has reached morning.
				Logger.trace("Reached morning, getting out of bed.");
				gamespeed = 1;
				Bed.restorePlayers();
			}
		}
		
		// Auto-save tick; marks when to do autosave.
		if(!paused)
			asTick++;
		if (asTick > astime) {
			if ((boolean) Settings.get("autosave") && !gameOver && player.health > 0) {

				new Save(WorldSelectDisplay.getWorldName());
			}
			
			asTick = 0;
		}
		
		// Increment tickCount if the game is not paused
		if (!paused) setTime(tickCount+1);
		
		// SCORE MODE ONLY
		
		if (isMode("score") && (!paused && !gameOver)) {
			if (scoreTime <= 0) { // GAME OVER
				gameOver = true;
				setDisplay(new EndGameDisplay(player));
			}
			
			scoreTime--;
		}

		// This is the general action statement thing! Regulates menus, mostly.
		if (!Renderer.canvas.hasFocus()) {
			input.releaseAll();
		}
		if (Renderer.canvas.hasFocus()) {
			gameTime++;

			input.tick(); // INPUT TICK; no other class should call this, I think...especially the *Menu classes.

			if (display != null) {
				// A menu is active.
				if (player != null)
					player.tick(); // It is CRUCIAL that the player is ticked HERE, before the menu is ticked. I'm not quite sure why... the menus break otherwise, though.
				display.tick(input);
				paused = true;
			} else {
				// No menu, currently.
				paused = false;

				// If player is alive, but no level change, nothing happens here.
				if (player.isRemoved() && Renderer.readyToRenderGameplay && !Bed.inBed(player)) {
					// Makes delay between death and death menu.
					World.playerDeadTime++;
					if (World.playerDeadTime > 60) {
						setDisplay(new PlayerDeathDisplay());
					}
				} else if (World.pendingLevelChange != 0) {
					setDisplay(new LevelTransitionDisplay(World.pendingLevelChange));
					World.pendingLevelChange = 0;
				}

				player.tick(); // Ticks the player when there's no menu.

				if (level != null) {
					level.tick(true);
					Tile.tickCount++;
				}
				
				if (display == null && input.getKey("F3").clicked) { // Shows debug info in upper-left
					Renderer.showDebugInfo = !Renderer.showDebugInfo;
				}
				
				// For debugging only
				if (debug) {
					
					if (input.getKey("ctrl-p").clicked) {
						// Print all players on all levels, and their coordinates.
						System.out.println("Printing players on all levels.");
						for (Level value : levels) {
							if (value == null) continue;
							value.printEntityLocs(Player.class);
						}
					}

					// Host-only cheats.
					if (input.getKey("Shift-r").clicked)
						World.initWorld(); // For single-player use only.

					if (input.getKey("1").clicked) changeTimeOfDay(Time.Morning);
					if (input.getKey("2").clicked) changeTimeOfDay(Time.Day);
					if (input.getKey("3").clicked) changeTimeOfDay(Time.Evening);
					if (input.getKey("4").clicked) changeTimeOfDay(Time.Night);

					String prevMode = (String)Settings.get("mode");
					if (input.getKey("creative").clicked) {
						Settings.set("mode", "creative");
						Items.fillCreativeInv(player.getInventory(), false);
					}
					if (input.getKey("survival").clicked) Settings.set("mode", "survival");
					if (input.getKey("shift-t").clicked) Settings.set("mode", "score");

					if (isMode("score") && input.getKey("ctrl-t").clicked) {
						scoreTime = normSpeed * 5; // 5 seconds
					}

					float prevSpeed = gamespeed;
					if (input.getKey("shift-0").clicked)
						gamespeed = 1;

					if (input.getKey("shift-equals").clicked) {
						if (gamespeed < 1) gamespeed *= 2;
						else if (normSpeed*gamespeed < 2000) gamespeed++;
					}
					if (input.getKey("shift-minus").clicked) {
						if (gamespeed > 1) gamespeed--;
						else if (normSpeed*gamespeed > 5) gamespeed /= 2;
					}


					// Client-only cheats, since they are player-specific.
					if (input.getKey("shift-g").clicked) // This should not be needed, since the inventory should not be altered.
						Items.fillCreativeInv(player.getInventory());

					if (input.getKey("ctrl-h").clicked) player.health--;
					if (input.getKey("ctrl-b").clicked) player.hunger--;

					if (input.getKey("0").clicked) player.moveSpeed = 1;
					if (input.getKey("equals").clicked) player.moveSpeed++;
					if (input.getKey("minus").clicked && player.moveSpeed > 1) player.moveSpeed--; // -= 0.5D;

					if (input.getKey("shift-u").clicked) {
						levels[currentLevel].setTile(player.x>>4, player.y>>4, Tiles.get("Stairs Up"));
					}
					if (input.getKey("shift-d").clicked) {
						levels[currentLevel].setTile(player.x>>4, player.y>>4, Tiles.get("Stairs Down"));
					}
				} // End debug only cond.
			} // End "menu-null" conditional
		} // End hasfocus conditional
	} // End tick()
	
	
	// This is the proper way to change the tickCount.
	public static void setTime(int ticks) {
		if (ticks < Time.Morning.tickTime) ticks = 0; // Error correct
		if (ticks < Time.Day.tickTime) time = 0; // Morning
		else if (ticks < Time.Evening.tickTime) time = 1; // Day
		else if (ticks < Time.Night.tickTime) time = 2; // Evening
		else if (ticks < dayLength) time = 3; // Night
		else { // Back to morning
			time = 0;
			ticks = 0;
			pastDay1 = true;
		}
		tickCount = ticks;
	}
	
	// This is the proper way to change the time of day.
	public static void changeTimeOfDay(Time t) {
		setTime(t.tickTime);
	}
	// This one works too.
	public static void changeTimeOfDay(int t) {
		Time[] times = Time.values();
		if (t > 0 && t < times.length)
			changeTimeOfDay(times[t]); // It just references the other one.
		else
			System.out.println("Time " + t + " does not exist.");
	}
	
	public static Time getTime() {
		Time[] times = Time.values();
		return times[time];
	}
	
	/** This adds a notification to all player games. */
	public static void notifyAll(String msg) {
		notifyAll(msg, 0);
	}
	public static void notifyAll(String msg, int notetick) {
		msg = Localization.getLocalized(msg);
		notifications.add(msg);
		Updater.notetick = notetick;
	}
}
